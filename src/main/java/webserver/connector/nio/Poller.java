package webserver.connector.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.container.StandardContext;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

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

    public Poller(ExecutorService workerPool, StandardContext context, NioEndpoint endpoint) throws IOException {
        this.selector = Selector.open();
        this.workerPool = workerPool;
        this.context = context;
        this.endpoint = endpoint;
    }

    // Acceptor가 새 소켓을 넘길 때, 호출
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
                    wrapper.flushWriteBuffer(key);
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

    public void requestSwitchToRead(SelectionKey key) {
        toRead .offer(key);
        selector.wakeup();
    }
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
