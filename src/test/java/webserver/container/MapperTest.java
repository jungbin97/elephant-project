package webserver.container;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.servlet.HttpServlet;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {
    private static Mapper mapper;
    private static StandardWrapper userWrapper;
    private static StandardWrapper userWildcardWrapperShort;
    private static StandardWrapper userWildcardWrapperLong;
    private static StandardWrapper WildcardWrapperExtantion;
    private static StandardWrapper defaultWrapper;

    @BeforeAll
    static void setUp() {
        userWrapper = new StandardWrapper(HttpServlet.class);
        userWildcardWrapperShort = new StandardWrapper(HttpServlet.class);
        userWildcardWrapperLong = new StandardWrapper(HttpServlet.class);
        WildcardWrapperExtantion = new StandardWrapper(HttpServlet.class);
        defaultWrapper = new StandardWrapper(HttpServlet.class);

        Map<String, StandardWrapper> map = new HashMap<>();
        map.put("/user", userWrapper);
        map.put("/user/*", userWildcardWrapperShort);
        map.put("/user/test/*", userWildcardWrapperLong);
        map.put("*.jsp", WildcardWrapperExtantion);
        map.put("/", defaultWrapper);

        mapper = new Mapper(map);
    }

    @Test
    @DisplayName("Exact match(정확한 매칭) URI에 대해 서블릿을 반환한다.")
    void exactMatch() {
        // given
        String uri = "/user";

        // when
        StandardWrapper result = mapper.getStandardWrapper(uri);

        // then
        assertThat(result).isSameAs(userWrapper);
    }

    @Test
    @DisplayName("Prefix match(접두사 매칭)은 가장 긴 prefix와 매핑된 서블릿을 반환한다.")
    void prefixMatch () {
        // given
        String uri = "/user/test/1";
        // when
        StandardWrapper result = mapper.getStandardWrapper(uri);

        // then
        assertThat(result).isSameAs(userWildcardWrapperLong);
    }

    @Test
    @DisplayName("Extension match(확장자 매칭)은 일치하는 확장자와 매핑된 서블릿을 반환한다.")
    void ExtensionMatch() {
        // given
        String uri = "/test.jsp";
        // when
        StandardWrapper result = mapper.getStandardWrapper(uri);

        // then
        assertThat(result).isSameAs(WildcardWrapperExtantion);
    }

    @Test
    @DisplayName("일치하는 URI가 없을 경우, default 매칭을 반환한다.")
    void defatultMatch() {
        // given
        String uri = "/not/exist";

        // when
        StandardWrapper result = mapper.getStandardWrapper(uri);

        // then
        assertThat(result).isSameAs(defaultWrapper);
    }

    @Test
    @DisplayName("Prefix match(접두사 매칭)이 Extension match(확장자 매칭)보다 우선한다.")
    void prefixWinsOverExtension() {
        // given
        String uri = "/user/test.jsp";

        // when
        StandardWrapper result = mapper.getStandardWrapper(uri);

        // then
        assertThat(result).isSameAs(userWildcardWrapperLong);
    }
}
