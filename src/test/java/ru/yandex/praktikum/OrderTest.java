package ru.yandex.praktikum;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderTest {
    private final String baseUri = "https://qa-scooter.praktikum-services.ru/";

    @Test
    @Description("Проверка создания заказа с параметризацией цветов")
    public void testCreateOrder() {
        String orderJson = "{ \"color\": [\"BLACK\"] }"; // Для одного цвета
        given()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .body(orderJson)
                .when()
                .post("api/v1/orders")
                .then()
                .statusCode(201)
                .body("track", notNullValue());
    }

    @Test
    @Description("Проверка получения списка заказов")
    public void testGetOrders() {
        given()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .when()
                .get("api/v1/orders")
                .then()
                .statusCode(200)
                .body("orders", not(empty()));
    }


}
