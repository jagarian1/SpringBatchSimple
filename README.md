# Oracle Database 커넥션 풀 관리 가이드

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.14-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![HikariCP](https://img.shields.io/badge/HikariCP-5.0.1-blue.svg)](https://github.com/brettwooldridge/HikariCP)
[![Oracle](https://img.shields.io/badge/Oracle-21c-red.svg)](https://www.oracle.com/database/)

Spring Boot 애플리케이션에서 Oracle 데이터베이스 커넥션 풀을 효율적으로 관리하는 방법을 설명합니다.

## 📋 목차
- [기능](#기능)
- [시작하기](#시작하기)
- [설정 방법](#설정-방법)
- [사용 예제](#사용-예제)
- [모니터링](#모니터링)
- [모범 사례](#모범-사례)
- [문제 해결](#문제-해결)
- [라이선스](#라이선스)

## ✨ 기능
- HikariCP를 통한 효율적인 커넥션 풀 관리
- 실시간 커넥션 상태 모니터링
- 문제 상황 발생 시 알림 기능
- 성능 최적화를 위한 설정 가이드

## 🚀 시작하기

### 의존성 설정
```xml
<dependencies>
    <!-- Oracle JDBC Driver -->
    <dependency>
        <groupId>com.oracle.database.jdbc</groupId>
        <artifactId>ojdbc8</artifactId>
        <version>21.5.0.0</version>
    </dependency>

    <!-- HikariCP -->
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
        <version>5.0.1</version>
    </dependency>
</dependencies>
```

### 기본 설정
```yaml
spring:
  datasource:
    driver-class-name: oracle.jdbc.OracleDriver
    url: jdbc:oracle:thin:@localhost:1521:YOUR_SID
    username: your_username
    password: your_password
    
    hikari:
      pool-name: HikariCP
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000  # 5분
      connection-timeout: 20000  # 20초
      validation-timeout: 5000  # 5초
      max-lifetime: 1200000  # 20분
      connection-test-query: SELECT 1 FROM DUAL
```

## 💻 설정 방법

### 데이터소스 설정
```java
@Configuration
public class DatabaseConfig {
    
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }
    
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource(hikariConfig());
    }
}
```

### 커넥션 풀 관리
```java
@Component
@Slf4j
public class DatabaseConnectionManager {
    private final HikariDataSource dataSource;

    public DatabaseConnectionManager(DataSource dataSource) {
        this.dataSource = (HikariDataSource) dataSource;
    }

    @PostConstruct
    public void init() {
        log.info("Total connections: {}", dataSource.getHikariPoolMXBean().getTotalConnections());
        log.info("Idle connections: {}", dataSource.getHikariPoolMXBean().getIdleConnections());
        log.info("Active connections: {}", dataSource.getHikariPoolMXBean().getActiveConnections());
    }
}
```

## 📊 모니터링

### 상태 확인 API
```java
@RestController
@RequestMapping("/admin")
public class DatabaseMonitorController {
    private final HikariDataSource dataSource;

    @GetMapping("/db/status")
    public Map<String, Object> getConnectionPoolStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalConnections", dataSource.getHikariPoolMXBean().getTotalConnections());
        status.put("activeConnections", dataSource.getHikariPoolMXBean().getActiveConnections());
        status.put("idleConnections", dataSource.getHikariPoolMXBean().getIdleConnections());
        return status;
    }
}
```

## 🌟 모범 사례

### 권장 설정값

| 설정 항목 | 권장값 | 설명 |
|---------|-------|-----|
| maximum-pool-size | 10-20 | CPU 코어 수 * 2 |
| connection-timeout | 20000ms | 커넥션 획득 대기 시간 |
| idle-timeout | 300000ms | 유휴 커넥션 유지 시간 |
| max-lifetime | 1200000ms | 커넥션 최대 수명 |

### 주의사항
- 커넥션 풀 크기는 데이터베이스 서버 설정을 고려하여 설정
- 주기적인 모니터링 필수
- 문제 발생 시 즉각적인 알림 설정 필요

## 🔧 문제 해결

### 커넥션 풀 모니터링
```java
@Component
@Slf4j
public class ConnectionPoolListener {
    @EventListener
    public void handlePoolAlert(HikariPoolMXBean.PoolAlert alert) {
        log.error("Connection pool alert: {}", alert.getMessage());
        // 알림 발송 로직 구현
    }
}
```

### 일반적인 문제 해결 방법

1. **커넥션 부족 현상**
   - maximum-pool-size 증가 검토
   - 트랜잭션 처리 시간 최적화
   - 불필요한 커넥션 사용 제거

2. **성능 저하**
   - connection-timeout 설정 검토
   - idle-timeout 조정
   - 쿼리 최적화 검토

## 📝 사용 예제

### Repository 구현
```java
@Repository
@Slf4j
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Transactional
    public void save(User user) {
        String sql = "INSERT INTO USERS (NAME, EMAIL) VALUES (?, ?)";
        jdbcTemplate.update(sql, user.getName(), user.getEmail());
    }
}
```


### 주요 변경 및 설명:

## 컨텍스트 루트 설정

   - application.yml에서 server.servlet.context-path: /api/v1 설정
   - 모든 API 엔드포인트는 이제 /api/v1/... 형태로 접근


## 서블릿 초기화

   - SpringBootServletInitializer 상속
   - WAR 배포 지원을 위한 설정 추가


### 웹 설정

## CORS 설정
   - 정적 리소스 핸들링

### 고려사항:

1. 컨텍스트 루트 변경 시 클라이언트 애플리케이션의 API 
2. 엔드포인트 업데이트 필요
3. 프록시 서버 설정 시 컨텍스트 패스 고려
4. 정적 리소스 경로 설정 확인
5. 보안 설정(Spring Security)과의 통합

## 📄 라이선스
이 프로젝트는 MIT 라이선스로 제공됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 👥 기여하기
1. 이 저장소를 포크합니다
2. 새로운 브랜치를 생성합니다
3. 변경사항을 커밋합니다
4. 브랜치에 푸시합니다
5. Pull Request를 생성합니다

---
⭐ 이 프로젝트가 도움이 되었다면 스타를 눌러주세요!
