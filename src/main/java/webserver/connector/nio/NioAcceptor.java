package webserver.connector.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

public class NioAcceptor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(NioAcceptor.class);

    private static final int INIT_ERR_DELAY = 50; // 초기 오류 지연 시간(ms)
    private static final int MAX_ERR_DELAY = 1_600; // 최대 오류 지연 시간(ms)

    private final Sleeper sleeper;
    private final NioEndpoint endpoint;
    private final String name;

    // CPU 캐시와 메인 메모리 간의 동기화 문제를 방지하기 위해 volatile 키워드를 사용
    private volatile boolean stopped = false;

    public NioAcceptor(NioEndpoint endpoint, String name, Sleeper sleeper) {
        this.endpoint = endpoint;
        this.name = name;
        this.sleeper = sleeper;
    }

    public NioAcceptor(NioEndpoint endpoint, String name) {
        this(endpoint, name, TimeUnit.MILLISECONDS::sleep);
    }

    void stop() {
        stopped = true;
        log.info("[{}] is stopping", name);
    }

    @Override
    public void run() {
        // 연속 오류 발생 시, Exponential back-off 지연 시간을 설정(ms)
        int backoff = 0;

        while (!stopped) {
            if (backoff > 0) {
                // 지수 백오프 지연 시간 동안 대기
                sleepSilently(backoff);
            }

            /*---------------- accept 처리 ---------------- */
            try {
                SocketChannel socketChannel = endpoint.serverSocketAccept();
                if (socketChannel == null) {
                    continue;
                }

                // socketOptions 설정 및 Poller 등록
                if (!endpoint.setSocketOptions(socketChannel)) {
                    endpoint.closeChannel(socketChannel);
                }

                // 성공 시 back-off 초기화
                backoff = 0;
            } catch (Exception ex) {
                log.info("[{}] accept failed - {}", name, ex.toString());
                // 지수 백오프 지연 시간을 처리
                backoff = (backoff == 0) ? INIT_ERR_DELAY
                        : Math.min(backoff * 2, MAX_ERR_DELAY);
            }
        }
    }

    private void sleepSilently(int backoff) {
        try {
            sleeper.sleep(backoff);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
