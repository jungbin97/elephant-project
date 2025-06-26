package trunk.connector.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trunk.connector.Http11Processor;
import trunk.container.StandardContext;
import trunk.http11.NioHttpRequestParser;
import trunk.http11.request.HttpRequest;
import trunk.http11.response.HttpResponse;
import trunk.http11.response.ResponseSender;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.file.Path;

/**
 * NIO 기반의 HTTP 요청을 처리하는 {@link Runnable} task 클래스 입니다.
 * <p>
 * 이 클래스는 {@link NioEndpoint}의 워커 스레드 풀에 의해 실행되며, 하나의 인스턴스는
 * 하나의 클라이언트 연결에 대한 요청-응답 사이클을 처리합니다.
 * <h2>주요 흐름</h2>
 * <ol>
 * <li>소켓 채널로부터 데이터를 읽고 {@link NioHttpRequestParser}를 통해 HTTP 요청 객체로 파싱합니다.</li>
 * <li>완성된 요청을 {@link Http11Processor}에 전달하여 서블릿 비즈니스 로직을 실행하고 {@link HttpResponse}를 생성합니다.</li>
 * <li>생성된 {@code HttpResponse}가 파일 본문({@link Path})을 가졌는지 확인하여, Zero-Copy 방식 또는 메모리 버퍼 방식으로 응답을 보낼지 결정합니다.</li>
 * <li>결정된 방식에 따라 쓰기 작업(들)을 {@link NioSocketWrapper}의 쓰기 큐에 등록하고, {@link Poller}에게 쓰기 이벤트 처리를 요청합니다.</li>
 * </ol>
 *
 * @author jungbin97
 * @see NioEndpoint
 * @see NioSocketWrapper
 * @see Poller
 */
public class Http11NioProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Http11NioProcessor.class);

    private final NioHttpRequestParser nioParser = new NioHttpRequestParser();
    private final NioSocketWrapper wrapper;
    private final StandardContext context;
    private final SelectionKey key;
    private final Poller poller;

    /**
     * 요청 처리에 필요한 모든 컴포넌트를 주입받아 새로운 Processor를 생성합니다.
     *
     * @param wrapper 이 프로세서가 처리할 클라이언트 연결에 대한 래퍼
     * @param context 서블릿 컨테이너의 컨텍스트
     * @param key     이 연결에 대한 SelectionKey
     * @param poller  I/O 이벤트를 감시하고 스케줄링하는 Poller
     */
    public Http11NioProcessor(NioSocketWrapper wrapper, StandardContext context, SelectionKey key, Poller poller) {
        this.wrapper = wrapper;
        this.context = context;
        this.key = key;
        this.poller = poller;
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = wrapper.buffer;

            int bytesRead = wrapper.channel.read(buffer);
            if (bytesRead == -1) {
                wrapper.closeChannel();
                return;
            }

            HttpRequest request = nioParser.parse(buffer);
            if (request == null) {
                poller.requestSwitchToRead(key);
                return; // 요청 누적 중
            }

            HttpResponse response = new HttpResponse();
            new Http11Processor(context).process(request, response);

            if (response.hasFileBody()) {
                sendResponseWithZeroCopy(response);
            } else {
                sendResponseFromBuffer(response);
            }
        } catch (IOException e) {
            wrapper.closeChannel();
        }
    }

    /**
     * 메모리 버퍼로부터 응답을 전송하기 위해 쓰기 task를 큐에 등록합니다. (동적 콘텐츠용)
     *
     * @param response 전송할 HttpResponse 객체
     * @throws IOException I/O 오류 발생 시
     */
    private void sendResponseFromBuffer(HttpResponse response) throws IOException {
        ByteBuffer responseBuffer = ResponseSender.sendResponseNIO(response);
        wrapper.writeQueue.offer(responseBuffer);
        // 쓰기 큐에 응답 버퍼 추가 후, poller에게 WRITE 요청
        poller.requestSwitchToWrite(key);
    }

    /**
     * Zero-Copy 방식으로 응답을 전송하기 위해 쓰기 task들을 큐에 등록합니다. (정적 파일용)
     * <p>
     * 이 메서드는 헤더 전송 작업과 파일 전송 작업을 각각 큐에 등록하고,
     * 실제 I/O는 Poller 스레드에게 위임합니다.
     *
     * @param response 전송할 HttpResponse 객체 (파일 본문을 포함해야 함)
     * @throws IOException I/O 오류 발생 시
     */
    private void sendResponseWithZeroCopy(HttpResponse response) throws IOException {
        ByteBuffer headerBuffer = ResponseSender.createHeaderBuffer(response);
        wrapper.writeQueue.offer(headerBuffer);

        // 파일 전송 작업을 큐에 추가
        Path filePath = response.getFileBody();
        FileChannel fileChannel = new FileInputStream(filePath.toFile()).getChannel();
        wrapper.writeQueue.offer(new FileSendEvent(fileChannel));

        poller.requestSwitchToWrite(key);
    }
}
