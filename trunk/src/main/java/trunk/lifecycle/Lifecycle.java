package trunk.lifecycle;

public interface Lifecycle {
    void init() throws Exception;
    void start() throws Exception;
    void stop() throws Exception;
    void destroy() throws Exception;
}
