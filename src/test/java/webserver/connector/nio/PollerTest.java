package webserver.connector.nio;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import webserver.container.StandardContext;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

class PollerTest {
    ExecutorService pool = mock(ExecutorService.class);
    StandardContext context = mock(StandardContext.class);
    NioEndpoint endpoint = mock(NioEndpoint.class);

    @Test
    @DisplayName("채널 등록 요청이 오면 EventQueue에 등록하고, Selector를 깨워야 한다.")
    void registerEventQueueAndWakeupSelector() throws Exception {
        // given
        Selector selector = mock(Selector.class);
        try (MockedStatic<Selector> selectorMock = mockStatic(Selector.class)) {
            selectorMock.when(Selector::open).thenReturn(selector);
            Poller poller = new Poller(pool, context, endpoint);

            //  한 사이클만 실행되도록 설정
            when(selector.select()).thenAnswer(inv -> {
                poller.stop(); // wakeup() 호출 1회
                return 0;
            });

            SocketChannel socketChannel = mock(SocketChannel.class);
            SelectionKey key = mock(SelectionKey.class);

            // when
            poller.register(socketChannel);  // 이벤트 큐에 쌓고 wakeup()
            poller.run();                    // 한 사이클만 처리

            // then
            verify(socketChannel, times(1)).register(eq(selector), eq(SelectionKey.OP_READ), any());
            verify(selector, times(2)).wakeup();
        }
    }

    @Test
    @DisplayName("읽기 이벤트 시 OP_READ 제거하고 Http11NioProcessor를 workerPool에 제출한다")
    void dispatchReadable() throws Exception {
        // given
        Selector selector = mock(Selector.class);
        try (MockedStatic<Selector> selectorMock = mockStatic(Selector.class)) {
            selectorMock.when(Selector::open).thenReturn(selector);
            Poller poller = new Poller(pool, context, endpoint);

            when(selector.select()).thenAnswer(inv -> {
                poller.stop(); // wakeup() 호출 1회
                return 0;
            });
            SelectionKey key = mock(SelectionKey.class);
            when(selector.selectedKeys()).thenReturn(new HashSet<>(Set.of(key)));

            when(key.isValid()).thenReturn(true);
            when(key.isReadable()).thenReturn(true);
            when(key.isWritable()).thenReturn(false);
            when(key.interestOps()).thenReturn(SelectionKey.OP_READ);
            NioSocketWrapper wrapper = mock(NioSocketWrapper.class);
            when(key.attachment()).thenReturn(wrapper);

            // when
            poller.run();

            // then
            verify(key).interestOps(key.interestOps() & ~SelectionKey.OP_READ);
            verify(pool).submit(any(Http11NioProcessor.class));
        }
    }

    @Test
    @DisplayName("쓰기 이벤트 시 NioSocketWrapper의 flushWriteBuffer 메서드를 호출한다")
    void dispatchWritable() throws Exception {
        // given
        Selector selector = mock(Selector.class);
        try (MockedStatic<Selector> selectorMock = mockStatic(Selector.class)) {
            selectorMock.when(Selector::open).thenReturn(selector);
            Poller poller = new Poller(pool, context, endpoint);

            when(selector.select()).thenAnswer(inv -> {
                poller.stop(); // wakeup() 호출 1회
                return 0;
            });
            SelectionKey key = mock(SelectionKey.class);
            when(selector.selectedKeys()).thenReturn(new HashSet<>(Set.of(key)));

            when(key.isValid()).thenReturn(true);
            when(key.isReadable()).thenReturn(false);
            when(key.isWritable()).thenReturn(true);
            NioSocketWrapper wrapper = mock(NioSocketWrapper.class);
            when(key.attachment()).thenReturn(wrapper);

            // when
            poller.run();

            // then
            verify(wrapper).processWriteQueue(key);
            verify(pool, never()).submit(any(Http11NioProcessor.class));
        }
    }

    @Test
    @DisplayName("읽기/쓰기 큐에서 키를 꺼내고, OP_READ/OP_WRITE 비트만 설정한다")
    void switchQueues() throws Exception {
        // given
        Selector selector = mock(Selector.class);
        try (MockedStatic<Selector> selectorMock = mockStatic(Selector.class)) {
            selectorMock.when(Selector::open).thenReturn(selector);
            Poller poller = new Poller(pool, context, endpoint);

            SelectionKey rk = mock(SelectionKey.class);
            when(rk.isValid()).thenReturn(true);
            when(rk.interestOps()).thenReturn(0);
            poller.requestSwitchToRead(rk);

            SelectionKey wk = mock(SelectionKey.class);
            when(wk.isValid()).thenReturn(true);
            when(wk.interestOps()).thenReturn(0);
            poller.requestSwitchToWrite(wk);

            when(selector.select()).thenAnswer(inv -> {
                poller.stop(); // wakeup() 호출 1회
                return 0;
            });
            when(selector.selectedKeys()).thenReturn(Collections.emptySet());

            // when
            poller.run();

            // then
            // 읽기 큐에서 꺼낸 키는 OP_READ 비트만
            verify(rk).interestOps(SelectionKey.OP_READ);
            // 쓰기 큐에서 꺼낸 키는 OP_WRITE 비트만
            verify(wk).interestOps(SelectionKey.OP_WRITE);
        }
    }

    @Test
    @DisplayName("유효하지 않은 SelectionKey는 무시하고, 채널을 닫아야 한다")
    void invalidKeyClosedChannel() throws Exception {
        // given
        Selector selector = mock(Selector.class);
        try (MockedStatic<Selector> selectorMock = mockStatic(Selector.class)) {
            selectorMock.when(Selector::open).thenReturn(selector);
            Poller poller = new Poller(pool, context, endpoint);

            when(selector.select()).thenAnswer(inv -> {
                poller.stop(); // wakeup() 호출 1회
                return 0;
            });
            SelectionKey key = mock(SelectionKey.class);
            when(selector.selectedKeys()).thenReturn(new HashSet<>(Set.of(key)));
            when(key.isValid()).thenReturn(false);

            NioSocketWrapper wrapper = mock(NioSocketWrapper.class);
            when(key.attachment()).thenReturn(wrapper);

            // when
            poller.run();

            // then
            verify(wrapper).closeChannel();
            verify(pool, never()).submit(any(Http11NioProcessor.class));
        }
    }

    @Test
    @DisplayName("stop 메서드가 호출되면 Poller가 중지되어야 한다")
    void stop() throws Exception {
        // given
        Selector selector = mock(Selector.class);
        try (MockedStatic<Selector> selectorMock = mockStatic(Selector.class)) {
            selectorMock.when(Selector::open).thenReturn(selector);
            Poller poller = new Poller(pool, context, endpoint);

            when(selector.select()).thenReturn(10);

            // when
            Thread t = new Thread(poller);
            t.start();

            poller.stop();
            t.join(100);

            // then
            verify(selector).wakeup(); // Poller가 중지되면 Selector를 깨워야 함
            Assertions.assertFalse(t.isAlive());
        }
    }
}