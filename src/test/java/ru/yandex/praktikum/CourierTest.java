package ru.yandex.praktikum;

import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourierTest {

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    private static int courierId;
    private static String courierLogin;
    private static final String courierPassword = "12345";
    private static final String courierFirstName = "TestUser";

    @Test
    @Order(1)
    @DisplayName("Создание уникального курьера без использования JSON-файла")
    @Description("Регистрирует нового курьера с уникальным логином, сгенерированным во время выполнения теста")
    public void createCourierWithUniqueLogin() {
        String timeSuffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
        courierLogin = "courier_" + timeSuffix;

        String jsonBody = String.format(
                "{ \"login\": \"%s\", \"password\": \"%s\", \"firstName\": \"%s\" }",
                courierLogin, courierPassword, courierFirstName
        );

        given()
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));

        System.out.println("Курьер успешно создан: " + courierLogin);
    }





    @Test
    @Order(4)
    @Description("Проверка создания двух одинаковых курьеров")
    public void testCreateDuplicateCourier() {
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
