package webserver.connector.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.connector.Http11Processor;
import webserver.container.StandardContext;
import webserver.http11.NioHttpRequestParser;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.http11.response.ResponseSender;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.file.Path;

public class Http11NioProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Http11NioProcessor.class);

    private final NioHttpRequestParser nioParser = new NioHttpRequestParser();
    private final NioSocketWrapper wrapper;
    private final StandardContext context;
    private final SelectionKey key;
    private final Poller poller;

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
     * 메모리 버퍼로부터 응답을 전송합니다. (동적 컨텐츠용)
     */
    private void sendResponseFromBuffer(HttpResponse response) throws IOException {
        ByteBuffer responseBuffer = ResponseSender.sendResponseNIO(response);
        wrapper.writeQueue.offer(responseBuffer);
        // 쓰기 큐에 응답 버퍼 추가 후, poller에게 WRITE 요청
        poller.requestSwitchToWrite(key);
    }

    /**
     * 제로 카피 방식으로 응답을 전송합니다. (정적 컨텐츠용)
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
