import io.qameta.allure.Description;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CourierTest {

    private final String baseUri = "https://qa-scooter.praktikum-services.ru/";

    private int courierId;

    @AfterEach
    public void deleteCourier() {
        if (courierId != 0) {
            given()
                    .baseUri(baseUri)
                    .header("Content-Type", "application/json")
                    .when()
                    .delete("api/v1/courier/" + courierId)
                    .then()
                    .statusCode(200)
                    .body("ok", equalTo(true));
        }
    }
    @Test
    @Description("Проверка создания курьера")
    public void testCreateCourier() {
        // Получаем текущую дату в формате yyyyMMdd
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Создаем уникальное имя курьера с добавлением текущей даты
        String courierLogin = "login" + currentDate;

        // Данные для создания курьера
        String courierJson = "{ \"login\": \"" + courierLogin + "\", \"password\": \"12345\", \"firstName\": \"TestUser\" }";

        // Создаем курьера
        ValidatableResponse createResponse = given()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .body(courierJson)
                .when()
                .post("api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));

        courierId = createResponse.extract().path("id");
    }

//    @Test
//    @Description("Проверка удаления курьера")
//    public void testDeleteCourier() {
//        // Проверяем, что курьер был создан и у нас есть ID для удаления
//        if (courierId == 0) {
//            throw new RuntimeException("Courier ID not found. Please create a courier first.");
//        }
//
//        // Удаляем курьера
//        given()
//                .baseUri(baseUri)
//                .header("Content-Type", "application/json")
//                .when()
//                .delete("api/v1/courier/" + courierId)
//                .then()
//                .statusCode(200)
//                .body("ok", equalTo(true));
//    }

    @Test
    @Description("Проверка создания двух одинаковых курьеров")
    public void testCreateDuplicateCourier() {
        String courierJson = "{ \"login\": \"duplicateLogin\", \"password\": \"12345\", \"firstName\": \"Test\" }";

        given()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .body(courierJson)
                .when()
                .post("api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));

        given()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .body(courierJson)
                .when()
                .post("api/v1/courier")
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется"));
    }

    @Test
    @Description("Проверка создания курьера без обязательных полей")
    public void testCreateCourierWithoutRequiredFields() {
        String courierJson = "{ \"login\": \"\", \"password\": \"\", \"firstName\": \"\" }";

        given()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .body(courierJson)
                .when()
                .post("api/v1/courier")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания курьера"));
    }

    @Test
    @Description("Проверка логина курьера")
    public void testLoginCourier() {
        String loginJson = "{ \"login\": \"newLogin\", \"password\": \"12345\" }";

        given()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .body(loginJson)
                .when()
                .post("api/v1/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

}
