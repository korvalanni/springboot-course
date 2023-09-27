package ru.urfu.MyFirstAppTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import ru.urfu.MyFirstAppTest.controllers.HelloController;


import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class HelloControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private HelloController helloController;


    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }


    /**
     * Выполняет GET-запрос с одним параметром и проверяет наличие ожидаемого ответа.
     *
     * @param paramName имя параметра
     * @param param     значение параметра
     * @param path      URL-путь для запроса
     * @param expected  ожидаемое значение ответа
     * @author korvalanni
     */
    void runDSLWithParamCheck(String paramName, String param, String path, String expected) {
        given()
                .param(paramName, param)
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body(is(expected));
    }

    /**
     * Выполняет GET-запрос без параметров и проверяет наличие ожидаемого ответа.
     *
     * @param path     URL-путь для запроса
     * @param expected ожидаемое значение ответа
     * @author korvalanni
     */
    void runDSLWithoutParamCheck(String path, String expected) {
        given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body(is(expected));
    }

    /**
     * Тестирование ответа "/hello" без указания параметра.
     * Ожидается ответ "Hello world".
     *
     * @author korvalanni
     */
    @Test
    public void testHelloWithNoParameter() {
        String expected = "Hello world";

        runDSLWithoutParamCheck("/hello", expected);
    }

    /**
     * Тестирование ответа "/hello" с указанием параметра "name".
     * Ожидается ответ "Hello [имя]".
     *
     * @author korvalanni
     */
    @Test
    public void testHelloWithParameter() {
        String name = "John";
        String expected = "Hello " + name;

        runDSLWithParamCheck("name", name, "/hello", expected);
    }

    /**
     * Тестирование добавления строки в ArrayList через "/updateArrayList".
     * Ожидается, что добавленная строка будет присутствовать в списке.
     *
     * @author korvalanni
     */
    @Test
    public void testUpdateArrayList() {
        String expected = "testString";

        runDSLWithParamCheck("string", expected,
                "/updateArrayList", String.format("Строка %s добавлена в список", expected));

        List<String> strings = helloController.showArrayList().getBody();
        //Есть подозрение, что тут плохо написано
        if (strings == null || strings.isEmpty())
            Assertions.fail("The returned list is null or empty.");

        String result = strings.get(strings.size() - 1);

        Assertions.assertEquals(expected, result);
    }

    /**
     * Тестирование добавления строки в HashMap через "/updateHashMap".
     * Ожидается, что частота добавленной строки увеличится на 1.
     *
     * @author korvalanni
     */
    @Test
    public void testUpdateHashMap() {
        String key = "testString";
        int expected = 1;
        Map<String, Integer> stringFrequency = helloController.showHashMap().getBody();
        //Есть подозрение, что тут плохо написано
        if (stringFrequency == null)
            Assertions.fail("The returned map is null.");

        if (stringFrequency.containsKey(key))
            expected = stringFrequency.get(key) + 1;

        runDSLWithParamCheck("string", key,
                "/updateHashMap", String.format("Строка %s добавлена в словарь", key));
        int result = stringFrequency.get(key);

        Assertions.assertEquals(expected, result);
    }

    /**
     * Тестирование вывода содержимого HashMap при его пустом состоянии.
     * Ожидается пустой HashMap в ответе.
     *
     * @author korvalanni
     */
    @Test
    public void testShowEmptyHashMap() {
        runDSLWithoutParamCheck("/showHashMap", "{}");
    }

    /**
     * Параметризированный тест для проверки длины ArrayList и HashMap после их заполнения.
     * Ожидается, что длина каждой коллекции соответствует заданной.
     *
     * @param expectedArrayLength   ожидаемая длина ArrayList
     * @param expectedHashMapLength ожидаемая длина HashMap
     * @author korvalanni
     */
    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "0, 1",
            "1, 0",
            "2, 1",
            "2, 2"
    })
    public void testShowAllLength(int expectedArrayLength, int expectedHashMapLength) {
        for (int i = 0; i < expectedArrayLength; i++) {
            runDSLWithParamCheck("string", "test",
                    "/updateArrayList", "Строка test добавлена в список");
        }

        for (int i = 0; i < expectedHashMapLength; i++) {
            runDSLWithParamCheck("string", "test" + i,
                    "/updateHashMap", String.format("Строка %s добавлена в словарь", "test" + i));
        }

        List<String> strings = helloController.showArrayList().getBody();
        Map<String, Integer> stringsFrequency = helloController.showHashMap().getBody();
       //Есть подозрение, что тут плохо написано
        if(strings == null || stringsFrequency == null)
            Assertions.fail("The returned map is null.");

        int resultArrayLength = strings.size();
        int resultHashMapLength = stringsFrequency.size();

        runDSLWithoutParamCheck("showAllLength",
                String.format("Словарь содержит %d элементов, список содержит %d элементов",
                        resultHashMapLength, resultArrayLength));
    }
}