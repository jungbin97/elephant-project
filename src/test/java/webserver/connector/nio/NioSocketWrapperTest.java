package webserver.connector.nio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NioSocketWrapperTest {
    @Test
    @DisplayName("빈 WriteQueue를 플러시할 때 requestSwitchToRead가 호출되어야 한다.")
    void flushEmptyWriteQueue() throws Exception {
        // given
        SocketChannel channel = mock(SocketChannel.class);
        Poller poller = mock(Poller.class);
        NioSocketWrapper wrapper = new NioSocketWrapper(channel, mock(NioEndpoint.class), poller);
        SelectionKey key = mock(SelectionKey.class);

        // when
        wrapper.processWriteQueue(key);

        // then
        verify(poller).requestSwitchToRead(key);
    }

    @Test
    @DisplayName("IOException 발생 시 channel.close 호출되어야 한다.")
    void flush_ioException_closesChannel() throws IOException {
        // given
        SocketChannel channel = mock(SocketChannel.class);
        doThrow(new IOException("test"))
                .when(channel).write(any(ByteBuffer.class));

        NioSocketWrapper wrapper = new NioSocketWrapper(channel, mock(NioEndpoint.class), mock(Poller.class));
        SelectionKey key = mock(SelectionKey.class);

        // when
        wrapper.writeQueue.offer(ByteBuffer.allocate(1));
        wrapper.processWriteQueue(key);

        // then
        verify(channel).close();
    }

    @Test
    @DisplayName("완전한 WriteQueue를 플러시할 때는 writeQueue가 비워지고, requestSwitchToRead가 호출되어야 한다.")
    void flushFullWriteQueue() throws Exception {
        // given
        SocketChannel channel = mock(SocketChannel.class);
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.put(new byte[4]).flip();

        doAnswer(inv -> {
            ByteBuffer b = inv.getArgument(0);
            int toWrite = b.remaining();
            b.position(b.limit());
            return toWrite;
        }).when(channel).write(any(ByteBuffer.class));

        Poller poller = mock(Poller.class);
        NioSocketWrapper wrapper = new NioSocketWrapper(channel, mock(NioEndpoint.class), poller);
        wrapper.writeQueue.offer(buf);
        SelectionKey key = mock(SelectionKey.class);

        // when
        wrapper.processWriteQueue(key);

        // then
        assertThat(wrapper.writeQueue).isEmpty();
        verify(channel).write(any(ByteBuffer.class));
        verify(poller).requestSwitchToRead(key);
    }

    @Test
    @DisplayName("부분 전송된 버퍼는 interestOps를 Write 설정하고, writeQueue에 남아있어야 한다.")
    void flush_partialWrite_setsInterestOpsAndKeepsBuffer() throws IOException {
        // given
        SocketChannel channel = mock(SocketChannel.class);
        // writeQueue에 넣을 버퍼
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.put(new byte[4]).flip();  // remaining == 4

        // 항상 1바이트만 쓰고 남기는 stub
        doAnswer(inv -> {
            ByteBuffer b = inv.getArgument(0);
            int toWrite = Math.min(b.remaining(), 1); // 1바이트만 쓰기
            b.position(b.position() + toWrite);
            return toWrite;
        }).when(channel).write(any(ByteBuffer.class));

        Poller poller = mock(Poller.class);
        NioSocketWrapper wrapper = new NioSocketWrapper(channel, mock(NioEndpoint.class), poller);
        wrapper.writeQueue.offer(buf);

        SelectionKey key = mock(SelectionKey.class);
        // 기존 ops 값이 0이었다고 가정
        when(key.interestOps()).thenReturn(0);

        // when
        wrapper.processWriteQueue(key);

        // then
        assertThat(wrapper.writeQueue).isNotEmpty();
        verify(key).interestOps(SelectionKey.OP_WRITE);
        verify(poller, never()).requestSwitchToRead(any());
    }
}