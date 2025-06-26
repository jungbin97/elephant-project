package trunk.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trunk.container.Mapper;
import trunk.container.StandardContext;
import trunk.container.StandardWrapper;
import trunk.http11.request.HttpRequest;
import trunk.http11.response.HttpResponse;

import java.io.IOException;

/**
 * Connector와 Container 사이를 연결하는 어댑터(Adapter)입니다.
 * <p>
 * 이 클래스는 톰캣의 CoyoteAdapter와 유사하게, 순수한 HTTP 요청/응답 객체를
 * 서블릿 컨테이너가 이해하고 처리할 수 있도록 중개하는 역할을 담당합니다.
 * {@link Http11Processor}로부터 요청을 받아, {@link Mapper}를 통해 적절한 서블릿을 찾아
 * 실행을 위임합니다.
 *
 * @author jungbin97
 * @see Http11Processor
 * @see StandardContext
 * @see Mapper
 */
public class CoyoteAdapter {
    private static final Logger log = LoggerFactory.getLogger(CoyoteAdapter.class);

    private final StandardContext context;

    /**
     * 어댑터를 생성합니다.
     *
     * @param context 이 어댑터가 요청을 전달할 서블릿 컨테이너
     */
    public CoyoteAdapter(StandardContext context) {
        this.context = context;
    }

    /**
     * HTTP 요청을 서블릿 컨테이너로 전달하여 처리합니다.
     * <p>
     * {@link StandardContext}의 {@link Mapper}를 사용하여 요청 URI에 해당하는 서블릿({@link StandardWrapper})을 찾습니다.
     * 매핑되는 서블릿이 없으면, 404 Not Found 응답을 설정합니다.
     *
     * @param request  처리할 HTTP 요청 객체
     * @param response 채워나갈 HTTP 응답 객체
     * @throws IOException 서블릿 처리 중 I/O 오류 발생 시
     */
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        // 요청 URI에 해당하는 서블릿을 찾기 위해 매핑을 확인
        Mapper mapper = context.getMapper();
        StandardWrapper wrapper = mapper.getStandardWrapper(request.getStartLine().getRequestUri());

        if (wrapper != null) {
            wrapper.service(request, response);
        } else {
            response.setStatusCode(404);
            response.setBody("Not Found".getBytes());
            log.warn("No servlet found for URI: {}", request.getStartLine().getRequestUri());
        }
    }
}
