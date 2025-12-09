package org.itmowork.e2etest;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class E2eApplicationServiceTest extends BaseE2ETest{

    @Test
    void createApplication_success() {
        var companyJson = registerCompany();
        UUID companyId = UUID.fromString(companyJson.companyId());
        UUID ownerId    = UUID.fromString(companyJson.userId);

        JsonPath candidateJson = registerUser(
                "Candidate User",
                "candida@example.com",
                "candidate-password"
        );
        UUID candidateUserId = UUID.fromString(candidateJson.getString("id"));

        String vacancyDraftId = createVacancy(companyId, ownerId, "publish");
        UUID vacancyUUID = UUID.fromString(vacancyDraftId);


        Map<String, Object> appReq = new HashMap<>();
        appReq.put("cover_letter", "I am a strong Java developer");

        JsonPath appJson =
                given()
                        .contentType(ContentType.JSON)
                        .queryParam("vacancyId", vacancyDraftId)
                        .queryParam("userId", candidateUserId)
                        .body(appReq)
                        .when()
                        .post(baseUrl + "/api/application")
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath();

        UUID applicationId = UUID.fromString(appJson.getString("id"));
        String status      = appJson.getString("status");
        String coverLetter = appJson.getString("cover_letter");

        System.out.println("applicationId = " + applicationId);
        System.out.println("status = " + status);
        System.out.println("coverLetter = " + coverLetter);

        org.junit.jupiter.api.Assertions.assertEquals("new", status);
        org.junit.jupiter.api.Assertions.assertEquals("I am a strong Java developer", coverLetter);
    }

    public record CompanyRegistrationRes(String companyId, String userId) {
    }

    private CompanyRegistrationRes registerCompany() {
        Map<String, Object> req = new HashMap<>();
        req.put("name", "Test company");
        req.put("email", "c@example.com");
        req.put("description", "Some description");
        req.put("owner_full_name", "Owner Name");
        req.put("owner_email", "owner@example.com");
        req.put("owner_password", "strong-password");
        var json =
                given()
                        .contentType(ContentType.JSON)
                        .body(req)
                        .when()
                        .post(baseUrl + "/api/company/register-company")
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath();
        String companyId = json.getString("id");
        String userId    = json.getString("user_id");
        return new CompanyRegistrationRes(companyId, userId);
    }

    private JsonPath registerUser(String fullName, String email, String password) {
        Map<String, Object> userReq = new HashMap<>();
        userReq.put("full_name", fullName);
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

    private String createVacancy(UUID companyId, UUID userId, String statusPath) {
        Map<String, Object> req = new HashMap<>();
        req.put("title", "Java Dveloper");
        req.put("description", "Some esc");
        req.put("salary_from", 10000);
        req.put("salary_to", 20000);
        req.put("company_id", companyId.toString());
        req.put("currency_id", 1);
        return given()
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post(baseUrl + "/api/vacancies/" + userId + "/" + statusPath)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getString("id");
    }
}
