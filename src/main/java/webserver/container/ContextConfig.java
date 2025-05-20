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
 * {@code ContextConfig}는 웹 애플리케이션의 설정을 파싱하는 클래스입니다.
 * web.xml 파일을 읽어 서블릿과 URL 매핑 정보를 추출하고,
 * StandardContext에 추가합니다.
 *
 * <h2>기능</h2>
 * <ul>
 *     <li> &lt;servlet&gt; 태그를 통해 servlet-name, servlet-class, load-on-startup 정보를 수집합니다.</li>
 *     <li> &lt;servlet-mapping&gt; 태그를 통해 servlet-name ,url-pattern을 정보를 수집합니다.</li>
 *     <li>서블릿을 StandardContext에 등록합니다.</li>
 *     <li>load-on-startup 에 따른 동적 클래스 로딩을 위해 StandardWrapper를 사용합니다.</li>
 *     <li>서블릿 클래스 메타 정보를 StandardWrapper에 저장합니다.</li>
 * </ul>
 *
 * @author jungbin97
 * @see StandardContext
 * @see StandardWrapper
 * @see HttpServlet
 */
public class ContextConfig {
    public static final String SERVLET = "servlet";
    public static final String SERVLET_NAME = "servlet-name";
    public static final String SERVLET_CLASS = "servlet-class";
    public static final String LOAD_ON_STARTUP = "load-on-startup";
    public static final String URL_PATTERN = "url-pattern";
    public static final String SERVLET_MAPPING = "servlet-mapping";

    private final StandardContext standardContext;

    public ContextConfig(StandardContext standardContext) {
        this.standardContext = standardContext;
    }

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
