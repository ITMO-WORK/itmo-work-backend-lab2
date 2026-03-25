//package org.ilestegor.applicationservice;
//
//import io.restassured.http.ContentType;
//import io.restassured.path.json.JsonPath;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.Matchers.notNullValue;
//
//public class E2eApplicationServiceTest extends BaseE2ETest {
//
//    @Test
//    void fullApplicationFlow_success() {
//        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
//        var companyJson = registerCompany(
//                "Test Company " + uniqueSuffix,
//                "company_" + uniqueSuffix + "@mail.com"
//        );
//        UUID companyId = UUID.fromString(companyJson.companyId());
//        UUID ownerId   = UUID.fromString(companyJson.userId);
//
//        JsonPath candidateJson = registerUser(
//                "Candidate User",
//                "candidate_" + uniqueSuffix + "@example.com",
//                "candidate-password"
//        );
//        UUID candidateUserId = UUID.fromString(candidateJson.getString("id"));
//
//        String vacancyId = createVacancy(companyId, ownerId, "publish");
//        UUID vacancyUuid = UUID.fromString(vacancyId);
//
//        Map<String, Object> createReq = new HashMap<>();
//        createReq.put("cover_letter", "I am a strong Java developer");
//
//        JsonPath createdAppJson =
//                given()
//                        .contentType(ContentType.JSON)
//                        .queryParam("vacancyId", vacancyUuid)
//                        .queryParam("userId", candidateUserId)
//                        .body(createReq)
//                        .when()
//                        .post(baseUrl + "/api/application")
//                        .then()
//                        .statusCode(201)
//                        .extract()
//                        .jsonPath();
//
//        UUID applicationId = UUID.fromString(createdAppJson.getString("id"));
//        String status      = createdAppJson.getString("status");
//        String coverLetter = createdAppJson.getString("cover_letter");
//
//        org.junit.jupiter.api.Assertions.assertEquals("new", status);
//        org.junit.jupiter.api.Assertions.assertEquals("I am a strong Java developer", coverLetter);
//
//        Map<String, Object> updateReq = new HashMap<>();
//        updateReq.put("cover_letter", "I am an even stronger Java developer");
//
//        JsonPath updatedAppJson =
//                given()
//                        .contentType(ContentType.JSON)
//                        .body(updateReq)
//                        .when()
//                        .patch(baseUrl + "/api/application/" + vacancyUuid + "?userId=" + candidateUserId)
//                        .then()
//                        .statusCode(200)
//                        .extract()
//                        .jsonPath();
//
//        String updatedCoverLetter = updatedAppJson.getString("cover_letter");
//        String updatedStatus      = updatedAppJson.getString("status");
//
//        org.junit.jupiter.api.Assertions.assertEquals(
//                "I am an even stronger Java developer",
//                updatedCoverLetter
//        );
//        org.junit.jupiter.api.Assertions.assertEquals("new", updatedStatus);
//
//        Map<String, Object> statusReq = new HashMap<>();
//        statusReq.put("application_status_name", "viewed");
//
//        JsonPath statusJson =
//                given()
//                        .contentType(ContentType.JSON)
//                        .body(statusReq)
//                        .when()
//                        .patch(baseUrl + "/api/application/" + applicationId + "/status?userId=" + ownerId)
//                        .then()
//                        .statusCode(200)
//                        .extract()
//                        .jsonPath();
//
//        String newStatus = statusJson.getString("status");
//        org.junit.jupiter.api.Assertions.assertEquals("viewed", newStatus);
//
//        JsonPath pageJson =
//                given()
//                        .when()
//                        .get(baseUrl + "/api/application"
//                                + "?vacancyId=" + vacancyUuid
//                                + "&userId=" + ownerId
//                                + "&page=0&size=10")
//                        .then()
//                        .statusCode(200)
//                        .extract()
//                        .jsonPath();
//
//        var content = pageJson.getList("content");
//        org.junit.jupiter.api.Assertions.assertFalse(content.isEmpty(), "Applications page must not be empty");
//
//        var returnedIds = pageJson.getList("content.id", String.class);
//        org.junit.jupiter.api.Assertions.assertTrue(
//                returnedIds.contains(applicationId.toString()),
//                "Page of applications must contain our application id"
//        );
//    }
//
//    public record CompanyRegistrationRes(String companyId, String userId) {}
//
//    private CompanyRegistrationRes registerCompany(String companyName, String companyEmail) {
//        Map<String, Object> req = new HashMap<>();
//        req.put("name", companyName);
//        req.put("email", companyEmail);
//        req.put("description", "Some description");
//        req.put("owner_full_name", "Owner Name");
//        req.put("owner_email", "owner@example.com");
//        req.put("owner_password", "strong-password");
//        var json =
//                given()
//                        .contentType(ContentType.JSON)
//                        .body(req)
//                        .when()
//                        .post(baseUrl + "/api/company/register-company")
//                        .then()
//                        .statusCode(201)
//                        .extract()
//                        .jsonPath();
//        String companyId = json.getString("id");
//        String userId    = json.getString("user_id");
//        return new CompanyRegistrationRes(companyId, userId);
//    }
//
//    private JsonPath registerUser(String fullName, String email, String password) {
//        Map<String, Object> userReq = new HashMap<>();
//        userReq.put("full_name", fullName);
//        userReq.put("email", email);
//        userReq.put("password", password);
//
//        return given()
//                .contentType(ContentType.JSON)
//                .body(userReq)
//                .when()
//                .post(baseUrl + "/api/user/create")
//                .then()
//                .statusCode(201)
//                .extract()
//                .jsonPath();
//    }
//
//    private String createVacancy(UUID companyId, UUID userId, String statusPath) {
//        Map<String, Object> req = new HashMap<>();
//        req.put("title", "Java Developer");
//        req.put("description", "Some desc");
//        req.put("salary_from", 10000);
//        req.put("salary_to", 20000);
//        req.put("company_id", companyId.toString());
//        req.put("currency_id", 1);
//        return given()
//                .contentType(ContentType.JSON)
//                .body(req)
//                .when()
//                .post(baseUrl + "/api/vacancies/" + userId + "/" + statusPath)
//                .then()
//                .statusCode(201)
//                .body("id", notNullValue())
//                .extract()
//                .jsonPath()
//                .getString("id");
//    }
//}