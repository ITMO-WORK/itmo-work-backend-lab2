//package org.itmowork.e2etest;
//
//import org.junit.jupiter.api.Test;
//import static io.restassured.RestAssured.given;
//import io.restassured.http.ContentType;
//import java.util.HashMap;
//import java.util.Map;
//import static org.hamcrest.Matchers.notNullValue;
//
//public class E2eTestApplicationTests extends BaseE2ETest {
//
//    @Test
//    void publishedVacanciesListShouldContainCreatedVacancy() {
//
//        Map<String, Object> companyReq = new HashMap<>();
//        companyReq.put("name", "Test company");
//        companyReq.put("email", "company@example.com");
//        companyReq.put("description", "Some description");
//        companyReq.put("owner_full_name", "Owner Name");
//        companyReq.put("owner_email", "owner@example.com");
//        companyReq.put("owner_password", "strong-password");
//
//        var response =
//                given()
//                        .contentType(ContentType.JSON)
//                        .body(companyReq)
//                        .when()
//                        .post(baseUrl + "/api/company/register-company")
//                        .then()
//                        .statusCode(201)
//                        .body("id", notNullValue())
//                        .body("user_id", notNullValue())
//                        .extract()
//                        .jsonPath();
//
//        String companyId = response.getString("id");
//        String userId = response.getString("user_id");
//
//        System.out.println("companyId = " + companyId);
//        System.out.println("userId = " + userId);
//    }
//}