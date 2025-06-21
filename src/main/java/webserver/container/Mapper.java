package webserver.container;

import java.util.HashMap;
import java.util.Map;

/**
 * 요청 URI를 서블릿 매핑 규칙에 따라 적절한 서블릿 래퍼({@link StandardWrapper})에 매핑하는 역할을 수행합니다.
 * <p>
 * 톰캣의 매핑 규칙과 유사하게, 다음과 같은 우선순위에 따라 서블릿을 결정합니다.
 * <ol>
 * <li>정확한 URL 매칭 (Exact Match)</li>
 * <li>가장 긴 경로의 접두사 매칭 (Longest Path Prefix Match)</li>
 * <li>확장자 매칭 (Extension Match)</li>
 * <li>기본 서블릿 매칭 (Default Servlet Match)</li>
 * </ol>
 * 이 클래스는 생성 시점에 서블릿 매핑 정보를 내부적으로 파싱하여,
 * 런타임 시 빠른 조회가 가능하도록 구조화합니다.
 *
 * @author jungbin97
 * @see StandardWrapper
 * @see StandardContext
 */
public class Mapper {
    private final Map<String, StandardWrapper> exactMappings;
    private final Map<String, StandardWrapper> prefixMappings;
    private final Map<String, StandardWrapper> extensionMappings;
    private StandardWrapper defaultMatch;

    /**
     * Mapper를 생성하고, 제공된 서블릿 매핑 정보를 기반으로 내부 매핑 규칙을 초기화합니다.
     * @param servletMappings URL 패턴을 키로, {@link StandardWrapper}를 값으로 갖는 서블릿 매핑 정보
     */
    public Mapper(Map<String, StandardWrapper> servletMappings) {
        this.exactMappings = new HashMap<>();
        this.prefixMappings = new HashMap<>();
        this.extensionMappings = new HashMap<>();
        initalizeMappings(servletMappings);
    }

    /**
     * 서블릿 매핑 정보를 파싱하여 각 매칭 유형에 따라 내부 맵에 저장합니다.
     * @param servletMappings 원본 서블릿 매핑 정보
     */
    private void initalizeMappings(Map<String, StandardWrapper> servletMappings) {
        for (Map.Entry<String, StandardWrapper> entry : servletMappings.entrySet()) {
            String pattern = entry.getKey();
            StandardWrapper wrapper = entry.getValue();

            if (pattern.equals("/")) {
                defaultMatch = wrapper;
            } else if (pattern.endsWith("/*")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                prefixMappings.put(prefix, wrapper);
            } else if (pattern.startsWith("*.")) {
                String extension = pattern.substring(2);
                extensionMappings.put(extension, wrapper);
            } else {
                exactMappings.put(pattern, wrapper);
            }
        }
    }

    /**
     * 주어진 URI에 가장 적합한 서블릿 래퍼({@link StandardWrapper})를 찾아서 반환합니다.
     * 매핑 우선순위(정확, 접두사, 확장자, 기본)에 따라 일치하는 첫 번째 서블릿을 반환합니다.
     *
     * @param uri 처리할 요청의 URI
     * @return 매핑된 {@code StandardWrapper}. 일치하는 서블릿이 없을 경우 기본 서블릿 래퍼를 반환하며,
     * 기본 서블릿조차 없으면 {@code null}을 반환할 수 있습니다.
     */
    public StandardWrapper getStandardWrapper(String uri) {
        // 1. Exact match
        if (exactMappings.containsKey(uri)) return exactMappings.get(uri);

        // 2. Prefix match (가장 긴 prefix 우선)
        StandardWrapper prefixMatch = null;
        int longest = -1;
        for (Map.Entry<String, StandardWrapper> entry : prefixMappings.entrySet()) {
            String prefix = entry.getKey();
            if (uri.startsWith(prefix) && prefix.length() > longest) {
                prefixMatch = entry.getValue();
                longest = prefix.length();
            }
        }
        if (prefixMatch != null) return prefixMatch;

        // 3. Extension Match (*.jsp, 등등)
        int lastDotIndex = uri.lastIndexOf('.');
        if (lastDotIndex != -1) {
            String extension = uri.substring(lastDotIndex + 1);
            if (extensionMappings.containsKey(extension)) {
                return extensionMappings.get(extension);
            }
        }

        return defaultMatch;
    }
}