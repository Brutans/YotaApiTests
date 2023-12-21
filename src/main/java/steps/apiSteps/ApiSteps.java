package steps.apiSteps;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.responseModels.CustomerResponse;
import models.requestModels.*;
import models.responseModels.*;
import org.testng.Assert;

import static io.restassured.RestAssured.given;

public class ApiSteps {


    @Step("Проверка одинаковых результатов")
    public void checkEqualsResponse(Object exceptedResult, Object actualResult){
        Assert.assertEquals(exceptedResult, actualResult);
    }

    @Step ("Получения токена авторизации")
    public TokenResponse postLogin(LoginRequest LR){
        return given()
                .log().all()
                .baseUri("http://localhost:8080")
                .body(LR)
                .contentType(ContentType.JSON)
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .response().as(TokenResponse.class);
    }
    @Step ("Получение свободного номера")
    public PhonesResponse getEmptyPhones(String token){
        return given()
                .log().all()
                .header("authToken", token)
                .baseUri("http://localhost:8080")
                .get("/simcards/getEmptyPhone")
                .then()
                .statusCode(200)
                .extract()
                .response().as(PhonesResponse.class);
    }
    @Step("Получение id нового пользователя")
    public IdResponse postCustomer(CustomerRequest CR,String token){
        return given()
                .log().all()
                .header("authToken", token)
                .body(CR)
                .contentType(ContentType.JSON)
                .baseUri("http://localhost:8080")
                .post("/customer/postCustomer")
                .then()
                .statusCode(200)
                .extract()
                .response().as(IdResponse.class);
    }
    @Step("Получение кастомера по Id")
    public CustomerResponse getCustomer(String token, String id){
        return given()
                .log().all()
                .header("authToken", token)
                .baseUri("http://localhost:8080")
                .get("/customer/getCustomerById?customerId="+id)
                .then()
                .statusCode(200)
                .extract()
                .response().as(CustomerResponse.class);
    }
    @Step("Получение id кастомера при помощи XML")
    public Response getXMLId(String body){
        return given()
                .log().all()
                .body(body)
                .contentType(ContentType.XML)
                .baseUri("http://localhost:8080")
                .post("/customer/findByPhoneNumber")
                .then()
                .statusCode(200)
                .extract()
                .response();

    }
    @Step("Смена статуса")
    public void changeStatus(String token, String id, StatusRequest status, Integer statusCode){
        given()
                .header("authToken", token)
                .log().all()
                .body(status)
                .contentType(ContentType.JSON)
                .baseUri("http://localhost:8080")
                .post("/customer/"+id+"/changeCustomerStatus")
                .then()
                .statusCode(statusCode)
                .extract().response();
    }

}
