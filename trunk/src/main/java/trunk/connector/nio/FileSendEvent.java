package trunk.connector.nio;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Zero-Copy 파일 전송 작업을 캡슐화하는 상태 기반(stateful) 이벤트 객체입니다.
 * <p>
 * 이 클래스는 전송해야 할 파일의 채널({@link FileChannel})과 전송 진행 상황({@code writePosition})을
 * 상태로써 관리합니다. {@link NioSocketWrapper}의 쓰기 큐에 저장되어 {@link Poller} 스레드에 의해
 * 비동기적으로 처리됩니다.
 * <p>
 * 네트워크 버퍼의 상태에 따라 {@link #write(SocketChannel)} 메서드가 여러 번 호출될 수 있으며,
 * 호출될 때마다 중단된 지점부터 전송을 재개합니다.
 *
 * @author jungbin97
 * @see NioSocketWrapper
 * @see Poller
 */
public class FileSendEvent {
    private final FileChannel fileChannel;
    private long writePosition;
    private final long length;

    /**
     * 지정된 {@link FileChannel}로 새로운 파일 전송 이벤트를 생성합니다.
     *
     * @param fileChannel 전송할 파일에 대한 채널
     * @throws IOException 파일 크기를 얻는 중 오류가 발생할 경우
     */
    public FileSendEvent(FileChannel fileChannel) throws IOException {
        this.fileChannel = fileChannel;
        this.writePosition = 0;
        this.length = fileChannel.size();
    }

    /**
     * Poller 스레드에 의해 호출되어 실제 파일 전송을 수행합니다.
     * <p>
     * {@link FileChannel#transferTo(long, long, java.nio.channels.WritableByteChannel)}를 사용하여
     * OS 커널 레벨에서 Zero-Copy 전송을 시도합니다. 한 번의 호출로 전체 파일이 전송되지 않을 수 있으며,
     * 그 경우 내부 상태({@code writePosition})만 업데이트하고 {@code false}를 반환합니다.
     *
     * @param socketChannel 데이터를 쓸 대상 소켓 채널
     * @return 파일 전송이 완료되었으면 {@code true}, 아직 보낼 데이터가 남아있으면 {@code false}
     * @throws IOException I/O 에러 발생 시
     */
    public boolean write(SocketChannel socketChannel) throws IOException {
        long written = fileChannel.transferTo(writePosition, length - writePosition, socketChannel);
        writePosition += written;
        return writePosition >= length;
    }

    /**
     * 파일 전송 작업이 모두 완료된 후, 열려 있던 {@link FileChannel} 리소스를 안전하게 해제합니다.
     *
     * @throws IOException I/O 에러 발생 시
     */
    public void close() throws IOException {
        fileChannel.close();
    }
}
