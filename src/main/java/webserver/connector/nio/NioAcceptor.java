package webserver.connector.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * 새로운 클라이언트 연결을 수락하는 작업을 전담하는 {@link Runnable} 클래스입니다.
 * <p>
 * 이 클래스는 별도의 스레드에서 실행되어, 무한 루프를 돌면서 {@link NioEndpoint}로 부터
 * 새롱누 소켓 연결을 수락합니다. 가져온 연결은 소켓 옵션을 설정한 후,
 * I/O 처리를 위해 {@link Poller}에 전달됩니다.
 *
 * <h2>오류 처리 (지수 백오프)</h2>
 * 연결 수락 과정에서 일시적인 오류(예., OS의 파일 디스크립터 고갈)가 발생했을 때,
 * CPU를 100% 사용하며 재시도하는 것을 방지하기 위해 '지수 백오프' 전략을 사용합니다.
 * 오류 발생 시 짧은 시간 대기하고, 오류가 계속되면 대기 시간을 점차 늘려가며(최대 1.6초)
 * 시스템에 가해지는 부하를 줄입니다.
 *
 * @author jungbin97
 * @see NioEndpoint
 * @see Poller
 */
public class NioAcceptor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(NioAcceptor.class);

    private static final int INIT_ERR_DELAY = 50; // 초기 오류 지연 시간(ms)
    private static final int MAX_ERR_DELAY = 1_600; // 최대 오류 지연 시간(ms)

    private final Sleeper sleeper;
    private final NioEndpoint endpoint;
    private final String name;

    /**
     * 다른 스레드에 의한 변경 사항이 즉시 보이도록 보장하는 volatile 키워드.
     * Acceptor 루프의 실행 상태를 제어합니다.
     */
    private volatile boolean stopped = false;

    /**
     * NioAcceptor를 생성합니다. 테스트 용이성을 위해 대기 로직을 {@link Sleeper}로 주입받습니다.
     * @param endpoint 이 Acceptor를 소유하는 상위 엔드포인트
     * @param name     Acceptor 스레드의 이름
     * @param sleeper  대기(sleep) 로직을 제공하는 함수형 인터페이스
     */
    public NioAcceptor(NioEndpoint endpoint, String name, Sleeper sleeper) {
        this.endpoint = endpoint;
        this.name = name;
        this.sleeper = sleeper;
    }

    /**
     * 실제 운영 환경에서 사용할 NioAcceptor 생성자.
     * @param endpoint 이 Acceptor를 소유하는 상위 엔드포인트
     * @param name     Acceptor 스레드의 이름
     */
    public NioAcceptor(NioEndpoint endpoint, String name) {
        this(endpoint, name, TimeUnit.MILLISECONDS::sleep);
    }

    /**
     * 외부에서 Acceptor 스레드를 안전하게 종료시키기 위해 호출합니다.
     */
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

    /**
     * 스레드를 지정된 시간 동안 대기시키는 헬퍼 메서드.
     * <p>
     * 스레드가 sleep 상태에서 {@code interrupt()} 신호를 받아 {@link InterruptedException}이 발생하면,
     * 해당 스레드의 'interrupted' 상태 플래그는 자동으로 초기화됩니다.
     * 이 메서드는 {@link Thread#interrupt()}를 다시 호출하여, 중단 신호가 무시되지 않고
     * 상위 호출자에게 전파될 수 있도록 상태 플래그를 복원합니다.
     *
     * @param backoff 대기할 시간 (밀리초)
     */
    private void sleepSilently(int backoff) {
        try {
            sleeper.sleep(backoff);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
