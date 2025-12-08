package org.itmowork.e2etest;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class E2eApplicationServiceTest extends BaseE2ETest{

    @Test
    void createApplication_success() {
        JsonPath companyJson = registerCompany("Test company", "company@example.com");
        UUID companyId = UUID.fromString(companyJson.getString("id"));
        UUID ownerId   = UUID.fromString(companyJson.getString("user_id"));

        JsonPath candidateJson = registerUser(
                "Candidate User",
                "candidate@example.com",
                "candidate-password"
        );
        UUID candidateUserId = UUID.fromString(candidateJson.getString("id"));

        JsonPath vacancyJson = createVacancy(
                companyId,
                ownerId,
                "Java Developer",
                "Write awesome Java code",
                150,
                200
        );
        UUID vacancyId = UUID.fromString(vacancyJson.getString("id"));


        Map<String, Object> appReq = new HashMap<>();
        appReq.put("coverLetter", "I am a strong Java developer");

        JsonPath appJson =
                given()
                        .contentType(ContentType.JSON)
                        .queryParam("vacancyId", vacancyId)
                        .queryParam("userId", candidateUserId)
                        .body(appReq)
                        .when()
                        .post(baseUrl + "/api/application")
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath();

        UUID applicationId = UUID.fromString(appJson.getString("id"));
        String status      = appJson.getString("statusName");
        String coverLetter = appJson.getString("coverLetter");

        System.out.println("applicationId = " + applicationId);
        System.out.println("status = " + status);
        System.out.println("coverLetter = " + coverLetter);

        org.junit.jupiter.api.Assertions.assertEquals("NEW", status);
        org.junit.jupiter.api.Assertions.assertEquals("I am a strong Java developer", coverLetter);
    }

    private JsonPath registerCompany(String name, String email) {
        Map<String, Object> companyReq = new HashMap<>();
        companyReq.put("name", name);
        companyReq.put("email", email);
        companyReq.put("description", "Some description");
        companyReq.put("ownerFullName", "Owner Name");
        companyReq.put("ownerEmail", "owner_" + email);
        companyReq.put("ownerPassword", "strong-password");

        return given()
                .contentType(ContentType.JSON)
                .body(companyReq)
                .when()
                .post(baseUrl + "/api/company/register-company")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath();
    }

    private JsonPath registerUser(String fullName, String email, String password) {
        Map<String, Object> userReq = new HashMap<>();
        userReq.put("fullName", fullName);
        userReq.put("email", email);
        userReq.put("password", password);

        return given()
                .contentType(ContentType.JSON)
                .body(userReq)
                .when()
                .post(baseUrl + "/api/user/create")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath();
    }

    private JsonPath createVacancy(UUID companyId,
                                   UUID ownerUserId,
                                   String title,
                                   String description,
                                   int salaryFrom,
                                   int salaryTo) {

        Map<String, Object> vacancyReq = new HashMap<>();
        vacancyReq.put("title", title);
        vacancyReq.put("description", description);
        vacancyReq.put("salaryFrom", salaryFrom);
        vacancyReq.put("salaryTo", salaryTo);
        vacancyReq.put("companyId", companyId.toString());
        vacancyReq.put("currencyId", 1L);

        return given()
                .contentType(ContentType.JSON)
                .body(vacancyReq)
                .when()
                .post(baseUrl + "/api/vacancies/" + ownerUserId + "/publish")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath();
    }
}
