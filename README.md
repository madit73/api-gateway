# api-gateway

Cloud-agnostic API Gateway built with Spring Cloud Gateway and JWT-based authentication. This is a multi-module Maven project with a gateway app and shared utility libraries.

**Modules**
- `gateway`: Spring Boot WebFlux gateway service.
- `common/auth-utility`: JWT utilities and auth-related helpers.
- `common/rest-utility`: REST-related helpers (sanitization, security, servlet support).

**Key Features**
- Reactive Spring Cloud Gateway routes and filters
- JWT authorization filter
- Health and metrics via Spring Boot Actuator

**Requirements**
- Java 25 (as configured in `pom.xml`)
- Maven 3.9+

**Configuration**
The gateway expects a JWT secret provided via environment variable:
```bash
export JWT_SECRET=your-secret
```

Default port and URIs are configured in `gateway/src/main/resources/application.yaml`:
- Server port: `8080`
- Portal URI: `http://localhost:8088`
- Angular app URI: `http://localhost:4200`


**Build**
```bash
mvn clean install
```

**Run Locally**
From the repo root:
```bash
mvn -pl gateway -am spring-boot:run
```

**Tests**
```bash
mvn test
```
