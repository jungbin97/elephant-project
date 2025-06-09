package webserver.connector.nio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NioEndpointTest {

    @Test
    @DisplayName("serverSocketChannel을 바인딩해야 한다.")
    void bindInternal() throws Exception {
        // given
        ServerSocketChannel mockServerSocketChannel = mock(ServerSocketChannel.class);
        try (MockedStatic<ServerSocketChannel> mockedStatic = mockStatic(ServerSocketChannel.class)) {
            mockedStatic.when(ServerSocketChannel::open).thenReturn(mockServerSocketChannel);

            // when
            NioEndpoint endpoint = new NioEndpoint();
            endpoint.bind(8080);

            // then
            verify(mockServerSocketChannel).configureBlocking(true);
            verify(mockServerSocketChannel).bind(new InetSocketAddress(8080));
        }
    }

    @Test
    @DisplayName("accept()하고, 소켓 채널을 반환 해야한다.")
    void serverSocketAccept() throws Exception {
        // given
        ServerSocketChannel mockServerSocketChannel = mock(ServerSocketChannel.class);
        SocketChannel socketChannel = mock(SocketChannel.class);
        when(mockServerSocketChannel.accept()).thenReturn(socketChannel);

        // when
        NioEndpoint endpoint = new NioEndpoint();
        inject(endpoint, "serverSocketChannel", mockServerSocketChannel);
        SocketChannel acceptedChannel = endpoint.serverSocketAccept();

        // then
        assertThat(acceptedChannel).isSameAs(socketChannel);
        verify(mockServerSocketChannel).accept();
    }

    @Test
    @DisplayName("setSocketOptions()는 논블로킹 설정 후 PollerEventQueue에 등록해야 한다.")
    void setSocketOptions() throws Exception {
        // given
        SocketChannel mockSocketChannel = mock(SocketChannel.class);
        Poller poller = mock(Poller.class);

        NioEndpoint endpoint = new NioEndpoint();
        inject(endpoint, "poller", poller);

        // when
        boolean ok = endpoint.setSocketOptions(mockSocketChannel);

        // then
        assertThat(ok).isTrue();
        verify(mockSocketChannel).configureBlocking(false);
        verify(poller).register(mockSocketChannel);
    }

    @Test
    @DisplayName("setSocketOptions()가 실패하면 false를 반환하고, poller에 등록하지 않아야 한다.")
    void setSocketOptionsReturnFalse() throws Exception {
        // given
        SocketChannel mockSocketChannel = mock(SocketChannel.class);
        when(mockSocketChannel.configureBlocking(false)).thenThrow(new IOException("test"));
        Poller poller = mock(Poller.class);

        NioEndpoint endpoint = new NioEndpoint();
        inject(endpoint, "poller", poller);

        // when
        boolean ok = endpoint.setSocketOptions(mockSocketChannel);

        // then
        assertThat(ok).isFalse();
        verify(poller, never()).register(any());
    }

    @Test
    @DisplayName("closeChannel()는 채널을 닫아야 한다.")
    void closeChannel() throws Exception {
        // given
        SocketChannel mockSocketChannel = mock(SocketChannel.class);
        doThrow(new IOException("test")).when(mockSocketChannel).close();
        NioEndpoint endpoint = new NioEndpoint();

        // when
        endpoint.closeChannel(mockSocketChannel);

        // then
        verify(mockSocketChannel).close();
    }


    private static void inject(Object target, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}