package webserver.connector.nio;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Zero-Copy 파일 전송 작업을 캡슐화하는 이벤트 객체. <br>
 * NioSocketWrapper의 쓰기 큐에 저장되어 Poller 스레드에 의해 처리됩니다.
 */
public class FileSendEvent {
    private final FileChannel fileChannel;
    private long writePosition;
    private final long length;

    public FileSendEvent(FileChannel fileChannel) throws IOException {
        this.fileChannel = fileChannel;
        this.writePosition = 0;
        this.length = fileChannel.size();
    }

    /**
     * Poller 스레드에 의해 호출되어 실제 파일 전송을 수행.
     * @param socketChannel 데이터를 쓸 소켓 채널
     * @return 파일 전송이 완료되었으면 true, 아직 남아있으면 false
     * @throws IOException I/O 에러 발생 시
     */
    public boolean write(SocketChannel socketChannel) throws IOException {
        long written = fileChannel.transferTo(writePosition, length - writePosition, socketChannel);
        writePosition += written;
        return writePosition >= length;
    }

    /**
     * 전송 완료 후 FileChannel 리소스를 해제.
     * @throws IOException I/O 에러 발생 시
     */
    public void close() throws IOException {
        fileChannel.close();
    }
}
