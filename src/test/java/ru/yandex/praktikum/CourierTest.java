package ru.yandex.praktikum;

import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourierTest {

    private int courierId;
    private String baseUri = "https://qa-scooter.praktikum-services.ru";  // Используем правильную базовую ссылку

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = baseUri;
    }

    @Test
    @Order(1)
    @Description("Проверка создания курьера и авторизация")
    public void testCreateCourier() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmssSSS"));

        String courierLogin = "login" + currentTime;

        String courierJson = "{ \"login\": \"" + courierLogin + "\", \"password\": \"12345\", \"firstName\": \"TestUser\" }";

        ValidatableResponse createResponse = given()
                .header("Content-Type", "application/json")
                .body(courierJson)
                .when()
                .post("api/v1/courier")
                .then()
                .statusCode(201)  // Ожидаем статус 201
                .body("ok", equalTo(true));

        courierId = createResponse.extract().path("id");

        if (courierId == 0) {
            throw new RuntimeException("Courier ID not found in the response.");
        }

        System.out.println("Created courier ID: " + courierId);

        given()
                .header("Content-Type", "application/json")
                .body(courierId)
                .when()
                .post("api/v1/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue());  // Проверяем, что ID курьера возвращается


    }

    @Test
    @Order(3)
    @Description("Проверка удаления курьера")
    public void testDeleteCourier() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmssSSS"));

        String courierLogin = "login" + currentTime;

        String courierJson = "{ \"login\": \"" + courierLogin + "\", \"password\": \"12345\", \"firstName\": \"TestUser\" }";

        ValidatableResponse createResponse = given()
                .header("Content-Type", "application/json")
                .body(courierJson)
                .when()
                .post("api/v1/courier")
                .then()
                .statusCode(201)  // Ожидаем статус 201
                .body("ok", equalTo(true));

        courierId = createResponse.extract().path("id");

        if (courierId == 0) {
            throw new RuntimeException("Courier ID not found. Please create a courier first.");
        }

        given()
                .header("Content-Type", "application/json")
                .when()
                .delete("api/v1/courier/" + courierId)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));

        courierId = 0;
    }

    @Test
    @Order(4)
    @Description("Проверка создания двух одинаковых курьеров")
    public void testCreateDuplicateCourier() {
        // Создаем курьера с дублирующим логином
        String loginJson = "{ \"login\": \"login" + LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmssSSS")) + "\", \"password\": \"12345\" }";

        given()
                .header("Content-Type", "application/json")
                .body(loginJson)
                .when()
                .post("api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));

        given()
                .header("Content-Type", "application/json")
                .body(loginJson)
                .when()
                .post("api/v1/courier")
                .then()
                .statusCode(409)  // Конфликт — логин уже существует
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @Order(5)
    @Description("Проверка создания курьера без обязательных полей")
    public void testCreateCourierWithoutRequiredFields() {
        String courierJson = "{ \"login\": \"\", \"password\": \"\", \"firstName\": \"\" }";

        given()
                .header("Content-Type", "application/json")
                .body(courierJson)
                .when()
                .post("api/v1/courier")
                .then()
                .statusCode(400)  // Ожидаем ошибку 400, если поля пустые
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @AfterEach
    public void deleteCourier() {
        if (courierId != 0) {
            given()
                    .header("Content-Type", "application/json")
                    .when()
                    .delete("api/v1/courier/" + courierId)
                    .then()
                    .statusCode(200)
                    .body("ok", equalTo(true));

            courierId = 0;
        }
    }
}
