package webserver.connector.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * {@link SocketChannel}을 래핑하여, 특정 클라이언트의 연결에 대한 상태와 동작을 관리하는 클래스입니다.
 * <p>
 * 각 클라이언트 연결마다 하나의 {@code NioSocketWrapper} 인스턴스가 생성되며, 이 클래스는 {@link SelectionKey}에
 * 'attachment'로 등록되어, Poller가 이벤트를 처리할 때 해당 인스턴스를 참조합니다.
 * <h2>주요 책임</h2>
 * <ul>
 * <li>읽기용 {@link ByteBuffer}를 관리합니다.</li>
 * <li>비동기 쓰기 작업을 위한 작업 큐({@code writeQueue})를 관리합니다. 이 큐에는 {@link ByteBuffer}나 {@link FileSendEvent} 등 다양한 쓰기 이벤트가 저장될 수 있습니다.</li>
 * <li>{@link Poller}에 의해 호출되는 비동기 쓰기 처리 로직({@link #processWriteQueue(SelectionKey)})을 제공합니다.</li>
 * </ul>
 *
 * @author jungbin97
 * @see Poller
 * @see FileSendEvent
 */
public class NioSocketWrapper {
    private static final Logger log = LoggerFactory.getLogger(NioSocketWrapper.class);

    final SocketChannel channel;
    final ByteBuffer buffer = ByteBuffer.allocate(8192); // 8KB 버퍼, 읽기용
    final Queue<Object> writeQueue = new ConcurrentLinkedQueue<>(); // 쓰기용 버퍼 큐
    private final NioEndpoint endpoint;
    private final Poller poller;

    /**
     * 지정된 소켓 채널과 상위 컴포넌트들로 NioSocketWrapper를 생성합니다.
     *
     * @param channel  이 래퍼가 관리할 소켓 채널
     * @param endpoint 이 래퍼를 관리하는 엔드포인트
     * @param poller   이 래퍼의 I/O 이벤트를 처리할 Poller
     */
    public NioSocketWrapper(SocketChannel channel, NioEndpoint endpoint, Poller poller) {
        this.channel = channel;
        this.endpoint = endpoint;
        this.poller = poller;
    }

    /**
     * Poller에 의해 {@link SelectionKey#OP_WRITE} 이벤트가 발생했을 때 호출되어 쓰기 큐를 처리합니다.
     * <p>
     * 큐에 있는 작업(ByteBuffer, FileSendEvent 등)을 순서대로 처리합니다. 만약 하나의 작업이
     * 한 번에 완료되지 않으면(예, TCP 송신 버퍼가 가득 찬 경우), OP_WRITE 관심사를 유지한 채로
     * 즉시 반환하여 다음 I/O 이벤트를 기다립니다. 큐가 모두 비워지면 OP_WRITE 관심사를 제거하고
     * 다시 OP_READ를 등록하도록 Poller에게 요청합니다.
     *
     * @param key 이 소켓 채널에 대한 SelectionKey
     */
    public void processWriteQueue(SelectionKey key) {
        try {
            while (!writeQueue.isEmpty()) {
                Object event = writeQueue.peek();
                boolean completed;

                if (event instanceof ByteBuffer) {
                    completed = writeByteBuffer((ByteBuffer) event);
                } else if (event instanceof FileSendEvent) {
                    completed = ((FileSendEvent) event).write(channel);
                } else {
                    // 알수 없는 타입의 이벤트가 큐에 있다면 로그를 남기고 제거
                    writeQueue.poll();
                    log.error("Unknown event type in writeQueue: {}", event.getClass().getName());
                    continue;
                }

                if (completed)  {
                    // 작업 완료 시 큐에서 제거
                    Object completedEvent = writeQueue.poll();
                    if (completedEvent instanceof FileSendEvent) {
                        ((FileSendEvent) completedEvent).close(); // FileChannel 리소스 해제
                    }
                } else {
                    // 작업이 다 끝나지 않았으면, OP_WRITE를 유지하고 다음 기회를 기다림
                    key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                    return;
                }
            }

            // 모든 쓰기 작업이 완료되면, 다시 읽기 모드로 전환
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            poller.requestSwitchToRead(key);
        } catch (IOException e) {
            log.error("Error during processing write queue", e);
            closeChannel();
        }

    }

    /**
     * 이 래퍼와 관련된 모든 리소스를 안전하게 닫습니다.
     * 큐에 남아있을 수 있는 {@link FileSendEvent}의 파일 채널을 닫아 리소스 누수를 방지하고,
     * 마지막으로 소켓 채널을 닫습니다.
     */
    public void closeChannel() {
        try {
            // 큐에 남은 FileSendEvent 리소스 정리
            for (Object event : writeQueue) {
                if (event instanceof FileSendEvent) {
                    ((FileSendEvent) event).close();
                }
            }
            channel.close();
        } catch (IOException ignore) {
            // Ignore
        }
    }

    private boolean writeByteBuffer(ByteBuffer buf) throws IOException {
        channel.write(buf);
        return !buf.hasRemaining();
    }
}
