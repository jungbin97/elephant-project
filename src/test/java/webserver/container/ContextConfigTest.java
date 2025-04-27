package webserver.container;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import webserver.servlet.Servlet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ContextConfigTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("web.xml을 정상 파싱하여 서블릿을 등록한다.")
    void parseXmlSuccess() throws IOException {
        // given
        File xmlFile = tempDir.resolve("web.xml").toFile();
        try (FileWriter fileWriter = new FileWriter(xmlFile)) {
            fileWriter.write(
                    """
                    <web-app>
                        <servlet>
                            <servlet-name>testServlet</servlet-name>
                            <servlet-class>webserver.servlet.HttpServlet</servlet-class>
                            <load-on-startup>1</load-on-startup>
                        </servlet>
                        <servlet-mapping>
                            <servlet-name>testServlet</servlet-name>
                            <url-pattern>/test</url-pattern>
                        </servlet-mapping>
                    </web-app>
                    """);
        }

        StandardContext mockContext = mock(StandardContext.class);
        ContextConfig contextConfig = new ContextConfig(mockContext);

        // when
        contextConfig.parseWebXml(xmlFile.getPath());

        // then
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Class<? extends Servlet>> classCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<Integer> loadOnStartupCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(mockContext).addChild(urlCaptor.capture(), classCaptor.capture(), loadOnStartupCaptor.capture());

        assertThat(urlCaptor.getValue()).isEqualTo("/test");
        assertThat(classCaptor.getValue().getName()).isEqualTo("webserver.servlet.HttpServlet");
        assertThat(loadOnStartupCaptor.getValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("web.xml에 정상적인 서블릿 클래스가 없을 경우 예외를 발생시킨다.")
    void parseXmlinavlidClass() throws Exception {
        // given
        File xmlFile = tempDir.resolve("web.xml").toFile();
        try (FileWriter fileWriter = new FileWriter(xmlFile)) {
            fileWriter.write(
                    """
                    <web-app>
                        <servlet>
                            <servlet-name>testServlet</servlet-name>
                            <servlet-class>webserver.servlet.NonExistServlet</servlet-class>
                            <load-on-startup>1</load-on-startup>
                        </servlet>
                        <servlet-mapping>
                            <servlet-name>testServlet</servlet-name>
                            <url-pattern>/test</url-pattern>
                        </servlet-mapping>
                    </web-app>
                    """);
        }

        StandardContext mockContext = mock(StandardContext.class);
        ContextConfig config = new ContextConfig(mockContext);

        // then
        assertThatThrownBy(() -> config.parseWebXml(xmlFile.getPath()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse web.xml");
    }
}
