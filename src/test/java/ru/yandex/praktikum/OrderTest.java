package ru.yandex.praktikum;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderTest {
    private final String baseUri = "https://qa-scooter.praktikum-services.ru/";

    @ParameterizedTest(name = "Создание заказа с цветом: {0}")
    @ValueSource(strings = {"BLACK", "GREY", "BLACK,GREY"})
    @Description("Проверка создания заказа с параметризацией по цветам")
    public void testCreateOrderWithColors(String colors) {

        String[] colorArray = colors.split(",");
        StringBuilder colorJsonArray = new StringBuilder("[");
        for (int i = 0; i < colorArray.length; i++) {
            colorJsonArray.append("\"").append(colorArray[i].trim()).append("\"");
            if (i < colorArray.length - 1) {
                colorJsonArray.append(",");
            }
        }
        colorJsonArray.append("]");

        String orderJson = "{ \"color\": " + colorJsonArray.toString() + " }";

        given()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .body(orderJson)
                .when()
                .post("/api/v1/orders")
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
