package org.itmowork.vacancy_service.integration_tests;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import io.restassured.RestAssured;
import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Testcontainers
public abstract class BaseIntegrationTest {

    protected static final int GATEWAY_PORT = 8765;
    protected static String baseUrl;

    @Container
    protected static final DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(new File("docker-compose.test.yml"))
                    .withExposedService(
                            "itmo-work-gateway",
                            GATEWAY_PORT,
                            Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5))
                    )
                    .waitingFor(
                            "itmo-work-gateway",
                            Wait.forHttp("/api/vacancies")   // или /actuator/health
                                    .forStatusCodeMatching(code -> code != 503)
                                    .withStartupTimeout(Duration.ofMinutes(5))
                    );

    @BeforeAll
    static void init() throws Exception {
        String host = environment.getServiceHost("itmo-work-gateway", GATEWAY_PORT);
        Integer port = environment.getServicePort("itmo-work-gateway", GATEWAY_PORT);

        baseUrl = "http://" + host + ":" + port;

        waitUntilUserServiceReady();
        waitUntilVacancyServiceReady();
    }

    private static void waitUntilVacancyServiceReady() throws Exception {
        String url = baseUrl + "/api/vacancies";
        for (int i = 0; i < 180; i++) {
            try {
                int code = RestAssured.get(url).getStatusCode();
                if (code != 503) {
                    System.out.println("Vacancy-service is ready! Status: " + code);
                    return;
                }
            } catch (Exception ignored) {}
            Thread.sleep(1000);
        }
        throw new IllegalStateException("Vacancy-service did not become ready in time");
    }

    private static void waitUntilUserServiceReady() throws Exception {
        String url = baseUrl + "/api/user/create";
        Map<String, Object> req = new HashMap<>();
        req.put("full_name", "HealthCheck User");
        req.put("email", "healthcheck+" + UUID.randomUUID() + "@test.com");
        req.put("password", "123456");

        for (int i = 0; i < 180; i++) {
            try {
                int status = given()
                        .contentType(ContentType.JSON)
                        .body(req)
                        .post(url)
                        .getStatusCode();

                if (status != 503) {
                    return;
                }
            } catch (Exception ignored) {}
            Thread.sleep(1000);
        }
        throw new IllegalStateException("User service did not become ready in time");
    }
}