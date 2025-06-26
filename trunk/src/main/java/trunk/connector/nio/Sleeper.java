package trunk.connector.nio;

/**
 * 스레드의 대기(sleep) 동작을 추상화하는 함수형 인터페이스입니다.
 * <p>
 * 이 인터페이스는 {@link NioAcceptor}와 같이 테스트 중에 실제 시간을 대기하기 어려운 클래스의
 * 테스트 용이성(Testability)을 높이기 위해 사용됩니다.
 * <p>
 * 의존성 주입(Dependency Injection)을 통해, 실제 운영 코드에서는
 * {@code TimeUnit.MILLISECONDS::sleep}과 같은 실제 대기 메서드를 주입하고,
 * 단위 테스트 코드에서는 시간을 기다리지 않고 호출 여부만 검증하는 가짜(Fake) 구현체를
 * 주입할 수 있습니다.
 *
 * @see NioAcceptor
 */
@FunctionalInterface
public interface Sleeper {
    void sleep(int ms) throws InterruptedException;
}
