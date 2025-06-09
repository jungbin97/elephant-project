package webserver.connector.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NioSocketWrapper {
    final SocketChannel channel;
    final ByteBuffer buffer = ByteBuffer.allocate(8192); // 8KB 버퍼, 읽기용
    final Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>(); // 쓰기용 버퍼 큐
    private final NioEndpoint endpoint;
    private final Poller poller;

    public NioSocketWrapper(SocketChannel channel, NioEndpoint endpoint, Poller poller) {
        this.channel = channel;
        this.endpoint = endpoint;
        this.poller = poller;
    }

    public void flushWriteBuffer(SelectionKey key) {
        try {
            ByteBuffer buf;
            while ((buf = writeQueue.peek()) != null) {
                channel.write(buf);
                if (buf.hasRemaining()) {
                    key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                    return;
                }
                writeQueue.poll(); // 버퍼가 다 쓰여지면 큐에서 제거
            }

            // 모든 write 완료, 읽기 관심 복구는 poller에게 위임
            poller.requestSwitchToRead(key);
        } catch (IOException e) {
            closeChannel();
        }

    }

    public void closeChannel() {
        try {
            channel.close();
        } catch (IOException ignore) {
            // Ignore the exception
        }
    }
}
