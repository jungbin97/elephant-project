package webserver.connector.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.connector.Http11Processor;
import webserver.container.StandardContext;
import webserver.http11.NioHttpRequestParser;
import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;
import webserver.http11.response.ResponseSender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

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

            // 경합 테스트 로그
            if (buffer.position() > 0) {
                log.warn("Buffer reused: position={}, limit={}", buffer.position(), buffer.limit());
            }

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
            ByteBuffer responseBuffer = ResponseSender.sendResponseNIO(response);

            wrapper.writeQueue.offer(responseBuffer);
            // 쓰기 큐에 응답 버퍼 추가 후, poller에게 WRITE 요청
            poller.requestSwitchToWrite(key);
        } catch (IOException e) {
            wrapper.closeChannel();
        }
    }
}
