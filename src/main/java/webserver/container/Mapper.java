package webserver.container;

import java.util.Map;

/**
 * {@code Mapper} 는 우선순위에 따라 URI을 서블릿과 매핑하는 역할을 합니다.
 * <p>
 *     <h2>기능</h2>
 *     <ul>
 *         <li>URI를 서블릿과 매핑합니다.</li>
 *         <li>URI 매핑 시, 우선순위에 따라 서블릿을 선택합니다.</li>
 *         <li>서블릿 매핑은 다음과 같은 우선순위를 가집니다.
 *         <ul>
 *             <li>정확한 URI 매칭(Exact match)</li>
 *             <li>접두사 매칭(Prefix match) (예: /user/*)</li>
 *             <li>확장자 매칭(Extension Match) (예: *.jsp)</li>
 *             <li>기본 매칭(Default Match) (예: /)</li>
 *         </ul>
 *
 */
public class Mapper {
    private final Map<String, StandardWrapper> servletMappings;

    public Mapper(Map<String, StandardWrapper> servletMappings) {
        this.servletMappings = servletMappings;
    }

    public StandardWrapper getStandardWrapper(String uri) {
        // 1. Exact match
        StandardWrapper exactMatch = servletMappings.get(uri);
        if (exactMatch != null) return exactMatch;

        // 나머지 탐색 순회
        StandardWrapper prefixMatch = null;
        StandardWrapper extensionMatch = null;
        StandardWrapper defaultMatch = null;
        int longest = -1;

        for (Map.Entry<String, StandardWrapper> entry : servletMappings.entrySet()) {
            String pattern = entry.getKey();
            StandardWrapper wrapper = entry.getValue();

            // 2. Prefix match (가장 긴 prefix 우선)
            if (pattern.endsWith("/*")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                if (uri.startsWith(prefix) && prefix.length() > longest) {
                    prefixMatch = wrapper;
                    longest = prefix.length();
                }
            }

            // 3. Extension Match (*.jsp, 등등)
            if (pattern.startsWith("*.")) {
                String extension = pattern.substring(2);
                int lastDotIndex = uri.lastIndexOf('.');

                if (lastDotIndex != -1 && uri.substring(lastDotIndex + 1).equals(extension)) {
                    extensionMatch = wrapper;
                }
            }

            // 4. Default Match (fallback - 예: /)
            if (pattern.equals("/")) {
                defaultMatch = wrapper;
            }
        }

        if (prefixMatch != null) return prefixMatch;
        if (extensionMatch != null) return extensionMatch;
        return defaultMatch;
    }


}
