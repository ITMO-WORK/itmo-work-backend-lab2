package org.itmowork.e2etest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import java.util.HashMap;
import java.util.Map;

@Testcontainers
public class E2eTestApplicationTests {

    private static final int GATEWAY_PORT = 8765;

    @Container
    static DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(new File("docker-compose.yml"))
                    .withExposedService(
                            "itmo-work-gateway",
                            GATEWAY_PORT,
                            Wait.forListeningPort()
                                    .withStartupTimeout(Duration.ofMinutes(5))
                    );

    private String baseUrl;

    @BeforeAll
    static void waitForSystem() throws InterruptedException {
        Thread.sleep(120_000);
    }

    @BeforeAll
    static void initRestAssured() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void setUp() {
        String host = environment.getServiceHost("itmo-work-gateway", GATEWAY_PORT);
        Integer port = environment.getServicePort("itmo-work-gateway", GATEWAY_PORT);
        this.baseUrl = "http://" + host + ":" + port;
    }

    @Test
    void publishedVacanciesList_shouldContainCreatedVacancy() {
        Map<String, Object> companyReq = new HashMap<>();
        companyReq.put("name", "Test company");
        companyReq.put("email", "company@example.com");
        companyReq.put("description", "Some description");
        companyReq.put("owner_full_name", "Owner Name");
        companyReq.put("owner_email", "owner@example.com");
        companyReq.put("owner_password", "strong-password");

        var response =
                given()
                        .contentType(ContentType.JSON)
                        .body(companyReq)
                        .when()
                        .post(baseUrl + "/api/company/register-company")
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath();

        String companyId = response.getString("id");
        String userId    = response.getString("user_id");

        System.out.println("companyId = " + companyId);
        System.out.println("userId = " + userId);
    }
}