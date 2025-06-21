package webserver.connector.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NioSocketWrapper {
    private static final Logger log = LoggerFactory.getLogger(NioSocketWrapper.class);

    final SocketChannel channel;
    final ByteBuffer buffer = ByteBuffer.allocate(8192); // 8KB 버퍼, 읽기용
    final Queue<Object> writeQueue = new ConcurrentLinkedQueue<>(); // 쓰기용 버퍼 큐
    private final NioEndpoint endpoint;
    private final Poller poller;

    public NioSocketWrapper(SocketChannel channel, NioEndpoint endpoint, Poller poller) {
        this.channel = channel;
        this.endpoint = endpoint;
        this.poller = poller;
    }

    /**
     * Poller에 의해 호출되어 쓰기 큐를 처리합니다. <br>
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

    private boolean writeByteBuffer(ByteBuffer buf) throws IOException {
        channel.write(buf);
        return !buf.hasRemaining();
    }

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
}
