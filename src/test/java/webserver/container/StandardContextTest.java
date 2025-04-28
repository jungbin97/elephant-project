package webserver.container;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.servlet.DummyServlet;

import static org.assertj.core.api.Assertions.assertThat;

class StandardContextTest {
    private StandardContext standardContext;

    @BeforeEach
    void setUp() {
        DummyServlet.destroyed = false;
        standardContext = new StandardContext();
    }

    @Test
    @DisplayName("addChild로 서블릿을 등록할 수 있다.")
    void addChildRegisterServlet() {
        // given & when
        standardContext.addChild("/test", DummyServlet.class, 1);

        // then
        assertThat(standardContext.getServletMapings())
                .containsKey("/test")
                .satisfies(map -> assertThat(map.get("/test").getInstance()).isEqualTo(DummyServlet.class));
    }

    @Test
    @DisplayName("loadOnStartUp이 호출 시 load-on-startup이 0 또는 양수인 서블릿을 로드하고, Mapper를 초기화한다.")
    void loadOnStartUp() {
        // given
        standardContext.addChild("/test", DummyServlet.class, 1);

        // when
        standardContext.loadOnStartup();

        // then
        assertThat(standardContext.getServletMapings()).containsKey("/test");
        assertThat(standardContext.getMapper()).isNotNull();
    }

    @Test
    @DisplayName("destroyAll 호출 시 등록된 서블릿들이 소멸된다.")
    void destorytAll() {
        // given
        DummyServlet.destroyed = true;
        standardContext.addChild("/test", DummyServlet.class, 1);
        standardContext.loadOnStartup();

        // when
        standardContext.destroyAll();

        // then
        assertThat(DummyServlet.destroyed).isTrue();
    }
}
