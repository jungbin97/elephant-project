package trunk.connector.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trunk.connector.endpoint.AbstractEndpoint;
import trunk.container.StandardContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NIO 기반 네트워크 엔드포인트를 구현하는 클래스입니다.
 * <p>
 * 이 클래스는 서버 소켓채널을 열고, 연결 수락(accept) 및 I/O 이벤트 처리(Poller),
 * 요청 처리를 위한 워커 스레드 풀과 핵심 컴포넌트를 생성하고 생명주기를 관리합니다.
 * <h2>주요 컴포넌트</h2>
 * <ul>
 * <li><b>Acceptor</b>: 단일 스레드에서 실행되며 새로운 클라이언트 연결을 수락하는 역할.</li>
 * <li><b>Poller</b>: 단일 스레드에서 실행되며, {@link java.nio.channels.Selector}를 이용해
 * 모든 연결의 I/O 이벤트를 감지하고 분배하는 역할.</li>
 * <li><b>Worker Pool</b>: 수락된 요청의 실제 비즈니스 로직을 처리하는 스레드 풀.</li>
 * </ul>
 *
 * @author jungbin97
 * @see AbstractEndpoint
 * @see NioAcceptor
 * @see Poller
 * */
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
        acceptor = new NioAcceptor(this, "Acceptor-1");
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

    /**
     * {@link NioAcceptor}에 의해 호출되어, 블로킹 방식으로 새로운 클라이언트 연결을 수락합니다.
     * @return 새로 연결된 클라이언트의 {@link SocketChannel}
     * @throws IOException 연결 수락 중 I/O 오류 발생 시
     */
    public SocketChannel serverSocketAccept() throws IOException {
        // OS 커널 플랫폼 별 버그 처리로직이 필요할 수 있습니다.
        return serverSocketChannel.accept();
    }

    /**
     * 수락된 새로운 소켓 채널의 옵션을 설정하고, I/O 처리를 위해 {@link Poller}에 등록합니다.
     * 이 메서드는 채널을 논블로킹 모드로 전환하는 중요한 역할을 합니다.
     *
     * @param channel 새로 수락된 소켓 채널
     * @return 작업 성공 여부
     */
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

    /**
     * 지정된 소켓 채널을 닫습니다.
     * @param channel 닫을 소켓 채널
     */
    public void closeChannel(SocketChannel channel) {
        try {
            channel.close();
        } catch (IOException ignore) {
        }
    }
}
