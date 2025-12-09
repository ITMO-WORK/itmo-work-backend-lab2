package com.itmowork.company_service;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import io.restassured.RestAssured;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@Testcontainers
public abstract class BaseE2ETest {

    protected static final int GATEWAY_PORT = 8765;
    protected String baseUrl;

    @Container
    protected static final DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(new File("docker-compose_test.yml"))
                    .withExposedService(
                            "itmo-work-gateway",
                            GATEWAY_PORT,
                            Wait.forListeningPort()
                                    .withStartupTimeout(Duration.ofMinutes(5))
                    );

    @BeforeAll
    static void waitForClusterReady() throws Exception {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        waitUntilUserServiceReady();
        Thread.sleep(60000);
    }

    private static void waitUntilUserServiceReady() throws Exception {
        String host = environment.getServiceHost("itmo-work-gateway", GATEWAY_PORT);
        Integer port = environment.getServicePort("itmo-work-gateway", GATEWAY_PORT);
        String url = "http://" + host + ":" + port + "/api/user/create";
        Map<String, Object> req = new HashMap<>();
        req.put("full_name", "HealthCheck User");
        req.put("email", "healthcheck@test.com");
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

    @BeforeEach
    void setupBaseUrl() {
        String host = environment.getServiceHost("itmo-work-gateway", GATEWAY_PORT);
        Integer port = environment.getServicePort("itmo-work-gateway", GATEWAY_PORT);
        this.baseUrl = "http://" + host + ":" + port;
    }
}
