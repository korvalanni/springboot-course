package ru.urfu.MyFirstAppTest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponentsBuilder;
import ru.urfu.MyFirstAppTest.controllers.HelloController;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class HelloControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private HelloController helloController;

    @Autowired
    private MockMvc mockMvc;


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
    private void executeHttpGetRequestWithResponseCheck(String paramName, String param, String path, String expected) {
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
    private void executeHttpGetRequestWithResponseCheck(String path, String expected) {
        given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body(is(expected));
    }

    /**
     * Выполняет GET-запрос к для получения объектов контроллера
     *
     * @param endpoint тип запроса (показать список или словарь)
     * @return результат запроса в виде объекта MvcResult.
     * @throws Exception если произошла ошибка при выполнении запроса.
     * @author korvalanni
     */
    private MvcResult performGetRequest(String endpoint) throws Exception {
        String urlWithQueryParam = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(endpoint)
                .build()
                .toUriString();

        return mockMvc.perform(MockMvcRequestBuilders.get(urlWithQueryParam))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    private <T> T parseResponse(MvcResult mvcResult, Class<T> responseType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaType javaType = objectMapper.getTypeFactory().constructType(responseType);
        return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), javaType);
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

        executeHttpGetRequestWithResponseCheck(ApiConstants.HELLO, expected);
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

        executeHttpGetRequestWithResponseCheck("name", name, ApiConstants.HELLO, expected);
    }

    /**
     * Тестирование добавления строки в ArrayList через "/updateArrayList".
     * Ожидается, что добавленная строка будет присутствовать в списке.
     *
     * @author korvalanni
     */
    @Test
    public void testUpdateArrayList() throws Exception {
        MvcResult mvcResultBeforeUpdate = performGetRequest(ApiConstants.SHOW_ARRAYLIST);
        List<String> stringsBeforeUpdate = parseResponse(mvcResultBeforeUpdate, List.class);
        Assertions.assertTrue(stringsBeforeUpdate.isEmpty());

        String expected = "testString";
        String successMessage = String.format("Строка %s добавлена в список", expected);

        executeHttpGetRequestWithResponseCheck("string", expected,
                ApiConstants.UPDATE_ARRAYLIST, successMessage);
        MvcResult mvcResult = performGetRequest(ApiConstants.SHOW_ARRAYLIST);
        List<String> stringsFromResponse = parseResponse(mvcResult, List.class);


        Assertions.assertNotNull(stringsFromResponse);
        Assertions.assertFalse(stringsFromResponse.isEmpty());
        Assertions.assertEquals(expected, stringsFromResponse.get(stringsFromResponse.size() - 1));
    }

    /**
     * Тестирование добавления строки в HashMap через "/updateHashMap".
     * Ожидается, что частота добавленной строки увеличится на 1.
     *
     * @author korvalanni
     */
    @Test
    public void testUpdateHashMap() throws Exception {
        String key = "testString";
        String successMessage = String.format("Строка %s добавлена в словарь", key);


        MvcResult mvcResultBeforeUpdate = performGetRequest(ApiConstants.SHOW_HASHMAP);
        Map<String, Integer> stringFrequencyBeforeUpdate = parseResponse(mvcResultBeforeUpdate, Map.class);
        Assertions.assertTrue(stringFrequencyBeforeUpdate.isEmpty());

        executeHttpGetRequestWithResponseCheck("string", key, ApiConstants.UPDATE_HASHMAP, successMessage);
        MvcResult mvcResultAfterUpdate = performGetRequest(ApiConstants.SHOW_HASHMAP);
        Map<String, Integer> stringFrequencyAfterUpdate = parseResponse(mvcResultAfterUpdate, Map.class);

        Assertions.assertTrue(stringFrequencyAfterUpdate.containsKey(key));
    }

    /**
     * Тестирование вывода содержимого HashMap при его пустом состоянии.
     * Ожидается пустой HashMap в ответе.
     *
     * @author korvalanni
     */
    @Test
    public void testShowEmptyHashMap() {
        executeHttpGetRequestWithResponseCheck("/showHashMap", "{}");
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
    public void testShowAllLength(int expectedArrayLength, int expectedHashMapLength) throws Exception {
        for (int i = 0; i < expectedArrayLength; i++) {
            executeHttpGetRequestWithResponseCheck("string", "test",
                    ApiConstants.UPDATE_ARRAYLIST, "Строка test добавлена в список");
        }

        for (int i = 0; i < expectedHashMapLength; i++) {
            executeHttpGetRequestWithResponseCheck("string", "test" + i,
                    ApiConstants.UPDATE_HASHMAP, String.format("Строка %s добавлена в словарь", "test" + i));
        }

        MvcResult arrayListResult = performGetRequest(ApiConstants.SHOW_ARRAYLIST);
        List<String> strings = parseResponse(arrayListResult, List.class);

        MvcResult hashMapResult = performGetRequest(ApiConstants.SHOW_HASHMAP);
        HashMap<String, Integer> stringsFrequency = parseResponse(hashMapResult, HashMap.class);

        Assertions.assertNotNull(strings);
        Assertions.assertNotNull(stringsFrequency);

        int resultArrayLength = strings.size();
        int resultHashMapLength = stringsFrequency.size();

        Assertions.assertEquals(expectedArrayLength, resultArrayLength, "ArrayList length mismatch.");
        Assertions.assertEquals(expectedHashMapLength, resultHashMapLength, "HashMap length mismatch.");

        executeHttpGetRequestWithResponseCheck("showAllLength",
                String.format("Словарь содержит %d элементов, список содержит %d элементов",
                        resultHashMapLength, resultArrayLength));
    }
}