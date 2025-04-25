package webserver.container;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code Mapper} 는 우선순위에 따라 URL을 서블릿과 매핑하는 역할을 합니다.
 * <p>
 *     <h2>기능</h2>
 *     <ul>
 *         <li>URL를 서블릿과 매핑합니다.</li>
 *         <li>URL 매핑 시, 우선순위에 따라 서블릿을 선택합니다.</li>
 *         <li>서블릿 매핑은 다음과 같은 우선순위를 가집니다.
 *         <ul>
 *             <li>정확한 URL 매칭(Exact match)</li>
 *             <li>접두사 매칭(Prefix match) (예: /user/*)</li>
 *             <li>확장자 매칭(Extension Match) (예: *.jsp)</li>
 *             <li>기본 매칭(Default Match) (예: /)</li>
 *         </ul>
 *
 */
public class Mapper {
    private final Map<String, StandardWrapper> exactMappings;
    private final Map<String, StandardWrapper> prefixMappings;
    private final Map<String, StandardWrapper> extensionMappings;
    private StandardWrapper defaultMatch;

    public Mapper(Map<String, StandardWrapper> servletMappings) {
        this.exactMappings = new HashMap<>();
        this.prefixMappings = new HashMap<>();
        this.extensionMappings = new HashMap<>();
        initalizeMappings(servletMappings);
    }

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