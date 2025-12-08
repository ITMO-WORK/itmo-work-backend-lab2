package org.itmowork.e2etest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import io.restassured.RestAssured;

import java.io.File;
import java.time.Duration;

@Testcontainers
public abstract class BaseE2ETest {

    protected static final int GATEWAY_PORT = 8765;
    protected String baseUrl;

    @Container
    protected static final DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(new File("docker-compose.yml"))
                    .withExposedService(
                            "itmo-work-gateway",
                            GATEWAY_PORT,
                            Wait.forListeningPort()
                                    .withStartupTimeout(Duration.ofMinutes(5))
                    );

    @BeforeAll
    static void waitForClusterReady() throws Exception {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        waitUntilVacancyEndpointReady();
    }

    private static void waitUntilVacancyEndpointReady() throws Exception {
        String host = environment.getServiceHost("itmo-work-gateway", GATEWAY_PORT);
        Integer port = environment.getServicePort("itmo-work-gateway", GATEWAY_PORT);
        String url = "http://" + host + ":" + port + "/api/vacancies";
        for (int i = 0; i < 180; i++) {
            try {
                int code = RestAssured
                        .given()
                        .get(url)
                        .getStatusCode();
                if (code == 200) {
                    System.out.println("Gateway is ready! Vacancies endpoint returned 200");
                    return;
                }
            } catch (Exception ignored) {}
            Thread.sleep(1000);
        }
        throw new IllegalStateException("Gateway did not become ready in time");
    }

    @BeforeEach
    void setupBaseUrl() {
        String host = environment.getServiceHost("itmo-work-gateway", GATEWAY_PORT);
        Integer port = environment.getServicePort("itmo-work-gateway", GATEWAY_PORT);
        this.baseUrl = "http://" + host + ":" + port;
    }
}
