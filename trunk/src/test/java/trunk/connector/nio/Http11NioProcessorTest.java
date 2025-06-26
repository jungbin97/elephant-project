package trunk.connector.nio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import trunk.connector.Http11Processor;
import trunk.container.StandardContext;
import trunk.http11.NioHttpRequestParser;
import trunk.http11.request.HttpRequest;
import trunk.http11.response.HttpResponse;
import trunk.http11.response.ResponseSender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class Http11NioProcessorTest {
    StandardContext ctx = mock(StandardContext.class);
    SelectionKey key = mock(SelectionKey.class);
    Poller poller = mock(Poller.class);

    @Test
    @DisplayName("EOF일 때는 closeChannel만 호출된다")
    void eof_closesChannelOnly() throws Exception {
        // given
        SocketChannel channel = mock(SocketChannel.class);
        NioEndpoint endpoint = mock(NioEndpoint.class);
        NioSocketWrapper wrapper = spy(new NioSocketWrapper(channel, endpoint, poller));
        when(wrapper.channel.read(any(ByteBuffer.class))).thenReturn(-1);

        Http11NioProcessor processor = new Http11NioProcessor(wrapper, ctx, key, poller);

        // when
        processor.run();

        // then
        verify(wrapper).closeChannel();
        assertThat(wrapper.writeQueue).isEmpty();
    }

    @Test
    @DisplayName("요청이 완성 되지 않으면 requestSwitchToRead가 호출된다")
    void requestSwitchToReadWhenRequestNotComplete() throws Exception {
        // given
        SocketChannel channel = mock(SocketChannel.class);
        NioEndpoint endpoint = mock(NioEndpoint.class);
        NioSocketWrapper wrapper = spy(new NioSocketWrapper(channel, endpoint, poller));

        try (MockedConstruction<NioHttpRequestParser> ctor =
                     mockConstruction(NioHttpRequestParser.class, (inst, context) -> when(inst.parse(any(ByteBuffer.class))).thenReturn(null))) {

            Http11NioProcessor processor = new Http11NioProcessor(wrapper, ctx, key, poller);

            // when
            processor.run();

            // then
            verify(poller).requestSwitchToRead(key);
            verify(wrapper, never()).closeChannel();
            assertThat(wrapper.writeQueue).isEmpty();
        }
    }

    @Test
    @DisplayName("read 중 에러 발생 시 closeChannel이 호출된다")
    void readErrorClosesChannel() throws Exception {
        // given
        SocketChannel channel = mock(SocketChannel.class);
        NioEndpoint endpoint = mock(NioEndpoint.class);
        NioSocketWrapper wrapper = spy(new NioSocketWrapper(channel, endpoint, poller));
        when(wrapper.channel.read(any(ByteBuffer.class))).thenThrow(new IOException());

        Http11NioProcessor processor = new Http11NioProcessor(wrapper, ctx, key, poller);

        // when
        processor.run();

        // then
        verify(wrapper).closeChannel();
    }

    @Test
    @DisplayName("완성 된 요청은 응답을 생성하고 쓰기 큐에 추가한다")
    void processCompleteRequestAddWriteQueue() throws Exception {
        // given
        SocketChannel channel = mock(SocketChannel.class);
        NioEndpoint endpoint = mock(NioEndpoint.class);
        NioSocketWrapper wrapper = spy(new NioSocketWrapper(channel, endpoint, poller));

        HttpRequest fakeRequest = mock(HttpRequest.class);
        ByteBuffer fakeBuf = ByteBuffer.allocate(0);

        try (
            MockedConstruction<NioHttpRequestParser> ctorParser =
                 mockConstruction(NioHttpRequestParser.class,
                         (inst, ctx2) -> when(inst.parse(any(ByteBuffer.class))).thenReturn(fakeRequest));
             // Http11Processor.process() 감시
             MockedConstruction<Http11Processor> ctorProc =
                     mockConstruction(Http11Processor.class,
                             (inst, ctx2) -> doNothing().when(inst).process(any(), any()));
             // ResponseSender.sendResponseNIO()
             MockedStatic<ResponseSender> rs = mockStatic(ResponseSender.class)) {

            rs.when(() -> ResponseSender.sendResponseNIO(any(HttpResponse.class))).thenReturn(fakeBuf);

            Http11NioProcessor processor = new Http11NioProcessor(wrapper, ctx, key, poller);
            // when
            processor.run();

            // then
            verify(poller).requestSwitchToWrite(key);
            assertThat(wrapper.writeQueue).containsExactly(fakeBuf);
        }
    }

}