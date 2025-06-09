package webserver.connector.nio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NioAcceptorTest {

    static class FakeSleeper implements Sleeper {
        final List<Integer>delays = new ArrayList<>();

        @Override
        public void sleep(int ms) {
            delays.add(ms);
        }
    }

    @Test
    @DisplayName("지수 백오프가 50 -> 100 -> 200 으로 증가해야 한다.")
    void exponentialBackoff() throws Exception {
        // given
        NioEndpoint endpoint = mock(NioEndpoint.class);
        when(endpoint.setSocketOptions(any())).thenReturn(true);

        FakeSleeper sleeper = new FakeSleeper();
        NioAcceptor acceptor = new NioAcceptor(endpoint, "test", sleeper);

        AtomicInteger callCount = new AtomicInteger();
        // 3번은 IOException을 발생시키고, 4번째는 성공적으로 SocketChannel을 반환한다.
        when(endpoint.serverSocketAccept()).thenAnswer(
                invovation -> {
                    int n = callCount.getAndIncrement();
                    if (n < 3) throw new IOException();
                    acceptor.stop();
                    return mock(SocketChannel.class);
                }
        );

        // when
        Thread t = new Thread(acceptor, "acceptor-test");
        t.start();
        t.join();

        // then
        assertThat(sleeper.delays).containsExactly(50, 100, 200);
    }

    @Test
    @DisplayName("지연이 1600ms를 초과하지 않아야 한다.")
    void backoffClampedToMax() throws Exception {
        // given
        NioEndpoint endpoint = mock(NioEndpoint.class);
        when(endpoint.setSocketOptions(any())).thenReturn(true);

        FakeSleeper sleeper = new FakeSleeper();
        NioAcceptor acceptor = new NioAcceptor(endpoint, "test", sleeper);
        // when
        when(endpoint.serverSocketAccept()).thenAnswer(i -> {
            if (sleeper.delays.size() >= 7) {
                acceptor.stop();
                return mock(SocketChannel.class);
            }
            throw new IOException();
        });
        Thread t = new Thread(acceptor, "acceptor-test");
        t.start();
        t.join();

        // then
        assertThat(sleeper.delays).containsExactly(50, 100, 200, 400, 800, 1600, 1600);
    }

    @Test
    @DisplayName("setSocketOptions가 실패하면 채널을 닫아야 한다.")
    void closesChannelWhenSetSocketOptionsFalse() throws Exception {
        // given
        NioEndpoint endpoint = mock(NioEndpoint.class);
        FakeSleeper sleeper = new FakeSleeper();
        SocketChannel ch = mock(SocketChannel.class);
        NioAcceptor acceptor = new NioAcceptor(endpoint, "test", sleeper);

        when(endpoint.serverSocketAccept()).thenReturn(ch)
                .thenAnswer(invocationOnMock -> {
                    acceptor.stop();
                    return null;
                });
        when(endpoint.setSocketOptions(ch)).thenReturn(false);

        // when
        Thread t = new Thread(acceptor, "acceptor-test");
        t.start();
        t.join();

        // then
        verify(endpoint).closeChannel(ch);
    }
}