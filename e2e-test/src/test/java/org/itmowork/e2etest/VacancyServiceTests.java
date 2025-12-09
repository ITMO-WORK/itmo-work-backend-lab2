package org.itmowork.e2etest;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.*;

public class VacancyServiceTests extends BaseE2ETest {

    public record CompanyRegistrationResult(String companyId, String userId) {
    }

    private CompanyRegistrationResult registerCompany() {
        Map<String, Object> req = new HashMap<>();
        req.put("name", "Test company");
        req.put("email", "compa@example.com");
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
        return new CompanyRegistrationResult(companyId, userId);
    }

    private String createVacancy(UUID companyId, UUID userId, String statusPath) {
        Map<String, Object> req = new HashMap<>();
        req.put("title", "Java Developer");
        req.put("description", "Some desc");
        req.put("salary_from", 100000);
        req.put("salary_to", 200000);
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

    @Test
    void fullVacancyFlowTest() {
        var reg = registerCompany();
        UUID companyId = UUID.fromString(reg.companyId);
        UUID userId    = UUID.fromString(reg.userId);
        String vacancyDraftId = createVacancy(companyId, userId, "draft");
        UUID vacancyUUID = UUID.fromString(vacancyDraftId);
        given()
                .when()
                .get(baseUrl + "/api/vacancies/" + vacancyUUID + "/exists")
                .then()
                .statusCode(200)
                .body(equalTo("true"));
        given()
                .when()
                .get(baseUrl + "/api/vacancies/" + vacancyUUID + "/title")
                .then()
                .statusCode(200)
                .body(equalTo("Java Developer"));
        given()
                .when()
                .get(baseUrl + "/api/vacancies/" + vacancyUUID + "/is-published")
                .then()
                .statusCode(200)
                .body(equalTo("false"));
        given()
                .when()
                .patch(baseUrl + "/api/vacancies/" + userId + "/" + vacancyUUID + "/change-status?newStatus=PUBLISHED")
                .then()
                .statusCode(200)
                .body("status_id", equalTo(1));
        given()
                .when()
                .get(baseUrl + "/api/vacancies")
                .then()
                .statusCode(200)
                .body("content", not(empty()));
        Map<String, Object> update = new HashMap<>();
        update.put("title", "Senior Java Developer");
        given()
                .contentType(ContentType.JSON)
                .body(update)
                .when()
                .patch(baseUrl + "/api/vacancies/" + userId + "/" + vacancyUUID + "/update")
                .then()
                .statusCode(200)
                .body("title", equalTo("Senior Java Developer"));
        Map<String, Object> update2 = new HashMap<>();
        update2.put("title", "Middle Java Developer");
        given()
                .contentType(ContentType.JSON)
                .body(update2)
                .when()
                .patch(baseUrl + "/api/vacancies/" + userId + "/" + vacancyUUID + "/update-and-change-status?newStatus=DRAFT")
                .then()
                .statusCode(200)
                .body("status_id", equalTo(2))
                .body("title", equalTo("Middle Java Developer"));
        given()
                .when()
                .get(baseUrl + "/api/vacancies/" + vacancyUUID + "/company-id")
                .then()
                .statusCode(200)
                .body(notNullValue());
    }
}


