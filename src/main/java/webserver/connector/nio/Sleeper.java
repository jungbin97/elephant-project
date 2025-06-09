package webserver.connector.nio;

@FunctionalInterface
public interface Sleeper {
    void sleep(int ms) throws InterruptedException;
}
