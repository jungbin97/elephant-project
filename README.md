```
   _____   _                  _                       _   
  |  ___| | |                | |                     | |  
  | |__   | |   ___   _ __   | |__     __ _   _ __   | |_ 
  |  __|  | |  / _ \ | '_ \  | '_ \   / _` | | '_ \  | __|
  | |___  | | |  __/ | |_) | | | | | | (_| | | | | | | |_ 
  \____/  |_|  \___| | .__/  |_| |_|  \__,_| |_| |_|  \__|
                     | |                                  
                     |_|
```

> _『어린 왕자』 코끼리를 삼킨 보아뱀 — 겉보기엔 단순한 모자처럼 보이지만, 그 안에는 거대한 코끼리가 숨어있다. 코끼리를 꺼내어 탐구하는 여정_
---
### 프로젝트 개요
**Elephant Project**는 웹 애플리케이션 서버(WAS)와 Spring 프레임워크의 내부 원리를 깊이 이해하고자 하는 목표에서 시작되었습니다. 복잡한 추상화 위에서 돌아가는 현대의 웹 애플리케이션 구조의 추상화를 걷어내고 **바닥부터 직접 구현**함으로써, 그 원리를 깊이 이해하는 것을 목적으로 합니다.
이를 통해 **프레임워크의 소비자가 아닌, 설계자이자 이해자로 성장**하는 것을 지향합니다.

본 프로젝트는 두 개의 주요 서브시스템으로 구성됩니다:

---
### 🐘 Trunk - Web Application Server
> _Inspired by the elephant's trunk — the organ of interaction, communication, and information processing._

**Trunk**는 HTTP 요청을 수신하고 응답을 전달하는 과정을 처리하는 Java 기반의 웹 애플리케이션 서버(WAS)입니다.
Tomcat의 Servlet Container와 유사한 역할을 수행하며, 다음과 같은 핵심 기능을 순수 Java로 구현하였습니다:

- **소켓 기반 HTTP 통신**: RFC 2616 기반 HTTP 파서(`BioHttpRequestParser`, `NioHttpRequestParser`) 직접 구현 및 요청/응답 처리
- **커넥션 및 스레드 관리**: NIO기반의 이벤트 루프(`Poller`)와 효율적인 요청 병렬 처리를 위한 스레드 풀(`NioEndpoint`의 `workerPool`)을 기반으로 연결을 수립
- **HTTP 요소 관리**: HTTP Request, Response, Session 객체 직접 구현 및 관리
- **서블릿 컨테이너 기능**: `web.xml` 파싱을 통한 라우팅, 서블릿 매핑(`Mapper`), 그리고 서블릿 생명주기(`StandardWrapper`, `StandardContext`)를 관리

<br>
이를 통해 HTTP 스펙, 네트워크 프로그래밍, 그리고 서블릿 컨테이너의 내부 구조와 웹 애플리케이션 서버의 전체적인 작동 방식을 깊이 탐구합니다.

---
### 🐍 Boa MVC - MVC Framework
> _Inspired by the boa constrictor — swallowing complexity, delivering simplicity._

**Boa MVC**는 웹 요청 처리의 복잡성을 우아하게 **추상화하고 소화하여 단순한 흐름으로 제공**하는, 코끼리 프로젝트의 경량 MVC 프레임워크입니다.

/// 구현 중///

개발자는 Boa MVC가 제공하는 직관적인 Model - View - Controller 계층에 집중함으로써, 비즈니스 로직을 구현할 수 있도록합니다.

---
### 프로젝트 철학
- **학습의 본질 추구**: 바퀴를 다시 발명함으로써 바퀴의 원리를 터득
- **탈 추상화(De-abstraction)**: 순수 Java 기반(POJO)으로 하위 계층부터 직접 구현
- **역할과 책임(Role & Responsibility)** : 객체지향 설계를 통한 명확한 책임 분리
- **안전한 리팩토링** : 테스트 코드를 통한 안정적 리팩토링 기반 마련
- **의미있는 테스트:** 커버리지 수치보다는 테스트의 본질 추구
  
### 기술 스택
- Java 17
- Gradle
- JUnit5 + AssertJ + Mokito
- SLF4J + Logback
- SonarQube + Jacoco

