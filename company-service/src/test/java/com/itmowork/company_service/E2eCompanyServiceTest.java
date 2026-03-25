//package com.itmowork.company_service;
//
//import io.restassured.http.ContentType;
//import io.restassured.path.json.JsonPath;
//import org.junit.jupiter.api.Test;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static io.restassured.RestAssured.given;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class E2eCompanyServiceTest extends BaseE2ETest{
//
//    @Test
//    public void fullCompanyFlowTest() {
//        CompanyRegistrationRes companyRegistrationRes = registerCompany();
//        assertEquals("Test company", companyRegistrationRes.name);
//        assertEquals("c@example.com", companyRegistrationRes.email);
//        assertEquals("Some description", companyRegistrationRes.description);
//
//        Map<String, Object> updateCompanyRequest = new HashMap<>();
//        updateCompanyRequest.put("name", "Updated Company");
//        updateCompanyRequest.put("description", "Updated description");
//
//        JsonPath jsonUpdateCompany =
//                given()
//                        .contentType(ContentType.JSON)
//                        .body(updateCompanyRequest)
//                        .when()
//                        .patch(baseUrl + "/api/company/update-company/" + companyRegistrationRes.companyId + "/" + companyRegistrationRes.userId)
//                        .then()
//                        .statusCode(200)
//                        .extract()
//                        .jsonPath();
//
//        assertEquals("Updated Company", jsonUpdateCompany.getString("name"));
//        assertEquals("Updated description", jsonUpdateCompany.getString("description"));
//
//
//        JsonPath pageJson =
//                given()
//                        .when()
//                        .get(baseUrl + "/api/company?"
//                                + "page=0&size=10")
//                        .then()
//                        .statusCode(200)
//                        .extract()
//                        .jsonPath();
//
//        var content = pageJson.getList("content");
//        assertFalse(content.isEmpty(), "Companies page must not be empty");
//
//        var returnedIds = pageJson.getList("content.id", String.class);
//        assertTrue(returnedIds.contains(companyRegistrationRes.companyId),
//                "Page of companies must contain our company id"
//        );
//
//        JsonPath deleteCompanyPath =
//                given()
//                        .contentType(ContentType.JSON)
//                        .when()
//                        .delete(baseUrl + "/api/company/delete/"+ companyRegistrationRes.companyId + "/" + companyRegistrationRes.userId)
//                        .then()
//                        .statusCode(200)
//                        .extract()
//                        .jsonPath();
//
//        assertEquals("Компания была успешно удалена", deleteCompanyPath.getString("message"));
//
//
//    }
//
//
//    public record CompanyRegistrationRes(String companyId, String userId, String name, String email, String description) {
//    }
//
//    private CompanyRegistrationRes registerCompany() {
//        Map<String, Object> req = new HashMap<>();
//        req.put("name", "Test company");
//        req.put("email", "c@example.com");
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
//        String userId = json.getString("user_id");
//        String name = json.getString("name");
//        String email = json.getString("email");
//        String description = json.getString("description");
//        return new CompanyRegistrationRes(companyId, userId, name, email, description);
//    }
//}
