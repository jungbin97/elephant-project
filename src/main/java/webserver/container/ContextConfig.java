package webserver.container;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import webserver.servlet.HttpServlet;
import webserver.servlet.Servlet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 웹 애플리케이션의 web.xml를 파싱하여 서블릿 정보를 {@link StandardContext}에 등록하는 역할을 담당합니다.
 * <p>
 * 서버 시작 시점에 `web.xml`을 읽어 그 안에 정의된 서블릿과 URL 매핑 규칙을 해석하고, 컨테이너인
 * {@code StandardContext}에 해당 정보를 추가합니다.
 *
 * @author jungbin97
 * @see StandardContext
 */
public class ContextConfig {
    private static final String SERVLET = "servlet";
    private static final String SERVLET_NAME = "servlet-name";
    private static final String SERVLET_CLASS = "servlet-class";
    private static final String LOAD_ON_STARTUP = "load-on-startup";
    private static final String URL_PATTERN = "url-pattern";
    private static final String SERVLET_MAPPING = "servlet-mapping";

    private final StandardContext standardContext;

    /**
     * 지정된 {@link StandardContext}를 설정하는 생성자.
     * 이 {@code ContextConfig}는 파싱 결과를 이 컨텍스트에 등록합니다.
     *
     * @param standardContext 서블릿 정보를 등록할 컨텍스트
     */
    public ContextConfig(StandardContext standardContext) {
        this.standardContext = standardContext;
    }

    /**
     * 지정된 경로의 web.xml 파일을 파싱하고, 그 내용을 {@link StandardContext}에 적용합니다.
     *
     * @param path web.xml 파일의 전체 경로
     * @throws RuntimeException 파일 파싱 또는 서블릿 클래스 로딩 중 심각한 오류가 발생했을 경우
     */
    public void parseWebXml(String path) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(path));
            document.getDocumentElement().normalize();

            Map <String, List<String>> servletNameToClass = new HashMap<>();
            NodeList servletNodes = document.getElementsByTagName(SERVLET);

            for (int i = 0; i < servletNodes.getLength(); i++) {
                Element servletElement = (Element) servletNodes.item(i);
                String servletName = servletElement.getElementsByTagName(SERVLET_NAME).item(0).getTextContent();
                String servletClass = servletElement.getElementsByTagName(SERVLET_CLASS).item(0).getTextContent();

                // Default value lazy loading
                String loadOnStartUp = "-1";
                NodeList loadOnStartupNode = servletElement.getElementsByTagName(LOAD_ON_STARTUP);
                if (loadOnStartupNode.getLength() > 0) {
                    loadOnStartUp = loadOnStartupNode.item(0).getTextContent();
                }

                servletNameToClass.put(servletName, List.of(servletClass, loadOnStartUp));
            }

            NodeList mappingNodes = document.getElementsByTagName(SERVLET_MAPPING);
            for (int i = 0; i < mappingNodes.getLength(); i++) {
                Element mappingElement = (Element) mappingNodes.item(i);
                String servletName = mappingElement.getElementsByTagName(SERVLET_NAME).item(0).getTextContent();
                String urlPattern = mappingElement.getElementsByTagName(URL_PATTERN).item(0).getTextContent();
                String servletClass = servletNameToClass.get(servletName).get(0);
                int loadOnStartUp = Integer.parseInt(servletNameToClass.get(servletName).get(1));

                Class<?> clazz = Class.forName(servletClass);

                if (HttpServlet.class.isAssignableFrom(clazz)) {
                    Class<? extends Servlet> servletClazz = clazz.asSubclass(Servlet.class);
                    standardContext.addChild(urlPattern, servletClazz, loadOnStartUp);
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to parse web.xml", e);
        }

    }
}
