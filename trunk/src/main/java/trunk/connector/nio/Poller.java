package trunk.connector.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trunk.container.StandardContext;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * {@link Selector}를 중심으로 한 I/O 이벤트 루프를 실행하는 {@link Runnable}입니다.
 * <p>
 * 이 클래스는 이벤트 루프를 구현하여, 단일 스레드에서 다수의 채널(클라이언트 연결)로 부터
 * 발생하는 I/O 이벤트를 다중화(multiplexing)하여 처리 합니다.
 * <h2>주요 역할</h2>
 * <ol>
 * <li>{@link Selector#select()}를 호출하여 I/O 준비가 된 채널들을 기다립니다.</li>
 * <li>READ 이벤트가 발생하면, 실제 데이터 읽기와 처리를 워커 스레드 풀({@code workerPool})의
 * {@link Http11NioProcessor} 태스크로 위임합니다.</li>
 * <li>WRITE 이벤트가 발생하면, {@link NioSocketWrapper}의 쓰기 큐에 있는 데이터를 직접 소켓에 씁니다.</li>
 * <li>외부 스레드로부터의 채널 등록 및 관심사 변경 요청을 동기화 큐를 통해 처리합니다.</li>
 * </ol>
 *
 * @author jungbin97
 * @see Selector
 * @see NioEndpoint
 * @see NioSocketWrapper
 */
public class Poller implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Poller.class);

    private final NioEndpoint endpoint;
    private final Selector selector;
    private final Queue<PollerEvent> pollerEventQueue = new ConcurrentLinkedQueue<>();
    private final Queue<SelectionKey> toRead  = new ConcurrentLinkedQueue<>();
    private final Queue<SelectionKey> toWrite = new ConcurrentLinkedQueue<>();
    private final ExecutorService workerPool;
    private final StandardContext context;

    private volatile boolean running = true;

    /**
     * 지정된 컴포넌트들로 Poller를 생성합니다.
     *
     * @param workerPool 요청 처리 태스크를 실행할 워커 스레드 풀
     * @param context    서블릿 컨텍스트
     * @param endpoint   이 Poller를 소유하는 엔드포인트
     * @throws IOException Selector를 여는 데 실패할 경우
     */
    public Poller(ExecutorService workerPool, StandardContext context, NioEndpoint endpoint) throws IOException {
        this.selector = Selector.open();
        this.workerPool = workerPool;
        this.context = context;
        this.endpoint = endpoint;
    }

    /**
     * 외부 스레드(주로 Acceptor)에서 새로운 소켓 채널을 이 Poller의 Selector에 등록하도록 요청합니다.
     * <p>
     * 실제 등록 작업은 스레드 안전성을 위해 이벤트 큐에 작업을 추가하고 {@link Selector#wakeup()}을 호출하여
     * Poller의 이벤트 루프 내에서 실행되도록 합니다.
     *
     * @param ch 새로 등록할 소켓 채널
     */
    public void register(SocketChannel ch) {
        pollerEventQueue.offer(new PollerEventImpl(ch, endpoint, this));
        selector.wakeup();
    }

    // poller 중지 시 호출
    void stop() {
        running = false;
        log.info("Poller is stopping");
        selector.wakeup(); // Selector를 깨워서 대기 상태에서 벗어나게 함.
    }

    /* ======================== 이벤트 루프 ====================== */
    @Override
    public void run() {
        log.info("Poller started");
        while (running) {
            try {
                // 이벤트 큐에서 이벤트를 가져와서 처리
                processEventQueue();
                // 읽기/쓰기 큐를 처리하여 관심사 변경
                processSwitchQueues();
                // 블로킹 대기
                selector.select();
                // 이벤트 키 처리
                dispatchSelectedKeys();
            } catch (Exception ioe) {
                log.error("Poller error", ioe);
            }
        }
    }


    // 이벤트 큐에서 이벤트를 가져와서 처리
    private void processEventQueue() throws IOException {
        PollerEvent event;
        while ((event = pollerEventQueue.poll()) != null) {
            event.execute(selector);
        }
    }

    // 이벤트 키 처리
    private void dispatchSelectedKeys() {
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> it = keys.iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            it.remove();

            NioSocketWrapper wrapper = (NioSocketWrapper) key.attachment();

            if (!key.isValid()) {
                if(wrapper != null) wrapper.closeChannel();
                continue;
            }

            try {
                if (key.isReadable()) {
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                    workerPool.submit(new Http11NioProcessor(wrapper, context, key, this));
                }

                if (key.isWritable()) {
                    wrapper.processWriteQueue(key);
                }
            } catch (Exception e) {
                if (wrapper != null) wrapper.closeChannel();
            }
        }
    }

    private void processSwitchQueues() {
        SelectionKey key;
        while ((key = toRead.poll())  != null) {
            if (key.isValid()) {
                key.interestOps((key.interestOps() & ~SelectionKey.OP_WRITE) | SelectionKey.OP_READ);
            }
        }

        while ((key = toWrite.poll()) != null) {
            if (key.isValid()) {
                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            }
        }
    }

    /**
     * 외부 스레드에서 특정 채널의 관심사를 READ로 변경하도록 스레드 안전하게 요청합니다.
     * @param key 관심사를 변경할 채널의 SelectionKey
     */
    public void requestSwitchToRead(SelectionKey key) {
        toRead .offer(key);
        selector.wakeup();
    }

    /**
     * 외부 스레드에서 특정 채널의 관심사를 WRITE로 변경하도록 스레드 안전하게 요청합니다.
     * @param key 관심사를 변경할 채널의 SelectionKey
     */
    public void requestSwitchToWrite(SelectionKey key) {
        toWrite.offer(key);
        selector.wakeup();
    }

    /* ============= PollerEvnet 계층 ================ */
    private interface PollerEvent {
        void execute(Selector selector) throws IOException;
    }

    private static class PollerEventImpl implements PollerEvent {
        private final SocketChannel channel;
        private final NioEndpoint endpoint;
        private final Poller poller;

        public PollerEventImpl(SocketChannel channel, NioEndpoint endpoint, Poller poller) {
            this.channel = channel;
            this.endpoint = endpoint;
            this.poller = poller;
        }

        @Override
        public void execute(Selector selector) throws IOException {
            // channel 캡슐화
            NioSocketWrapper wrapper = new NioSocketWrapper(channel, endpoint, poller);
            channel.register(selector, SelectionKey.OP_READ, wrapper);
        }
    }
}
