package webserver.connector.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.connector.endpoint.AbstractEndpoint;
import webserver.container.StandardContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioEndpoint extends AbstractEndpoint {
    private static final Logger log = LoggerFactory.getLogger(NioEndpoint.class);

    private ServerSocketChannel serverSocketChannel;
    private final ExecutorService workerPool = Executors.newFixedThreadPool(200);
    private final ExecutorService pollerPool = Executors.newSingleThreadExecutor(); // Poller를 위한 스레드

    private NioAcceptor acceptor;
    private Poller poller;

    @Override
    protected void bindInternal() throws Exception {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(true); // 블로킹 모드로 설정
        serverSocketChannel.bind(new InetSocketAddress(port));

        log.info("NioEndpoint bind to port {}", port);
    }

    @Override
    protected void startInternal(StandardContext context) throws Exception {
        poller = new Poller(workerPool, context, this);
        pollerPool.submit(poller);

        // Acceptor 시작
        acceptor = new NioAcceptor(this, "Aceptor-1");
        new Thread(acceptor).start();
    }

    @Override
    protected void stopInternal() throws Exception {
        log.info("Stopping NioEndpoint on port {}", port);
        acceptor.stop();
        poller.stop();
        pollerPool.shutdown();
        workerPool.shutdown();
        serverSocketChannel.close();
    }

    public SocketChannel serverScoketAccept() throws IOException {
        // OS 커널 플랫폼 별 버그 처리로직이 필요할 수 있습니다.
        return serverSocketChannel.accept();
    }

    // 신규 소켓 채널을 PollerEventQueue에 등록합니다.
    public boolean setSocketOptions(SocketChannel channel) {
        try {
            channel.configureBlocking(false);
            poller.register(channel);
            return true;
        } catch (Exception e) {
            log.error("setSocketOptions error: {}", e.getMessage());
            return false;
        }
    }


    public void closeChannel(SocketChannel channel) {
        try {
            channel.close();
        } catch (IOException ignore) {
        }
    }
}
