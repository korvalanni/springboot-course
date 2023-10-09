package ru.urfu.MyFirstAppTest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс-бин приветствие
 *
 * @author Николай Александрович и korvalanni
 */
@RestController
public class HelloController {

    private final List<String> strings;
    private final Map<String, Integer> stringsFrequency;

    @Autowired
    public HelloController() {
        this.strings = new ArrayList<>();
        this.stringsFrequency = new HashMap<>();
    }
    /**
     * Метод HelloWorld
     *
     * @param name имя
     * @return строку приветствие с именем, если имя задано, либо Hello world
     * @author Николай Александрович
     */
    @GetMapping("/hello")
    public ResponseEntity<String> hello(@RequestParam(value = "name", defaultValue = "world") String name) {
        return ResponseEntity.ok(String.format("Hello %s", name));
    }

    /**
     * Метод обновления списка строк
     *
     * @param string строка
     * @return лог о добавлении в список
     * @author korvalanni
     */
    @GetMapping("/updateArrayList")
    public ResponseEntity<String> updateArrayList(@RequestParam(value = "string") String string) {
        strings.add(string);
        return ResponseEntity.ok(String.format("Строка %s добавлена в список", string));
    }

    /**
     * Метод добавления строк, вводимых пользователем в словарь частот
     *
     * @param string строка
     * @return лог о добавлении в словарь
     * @author korvalanni
     */
    @GetMapping("updateHashMap")
    public ResponseEntity<String> updateHashMap(@RequestParam(value = "string") String string) {
        stringsFrequency.put(string, stringsFrequency.getOrDefault(string, 0) + 1);
        return ResponseEntity.ok(String.format("Строка %s добавлена в словарь", string));
    }


    /**
     * Метод просмотра содержимого словаря
     *
     * @return содержимое словаря в json формате
     * @author korvalanni
     */
    @GetMapping("showHashMap")
    public ResponseEntity<Map<String, Integer>> showHashMap() {
        return ResponseEntity.ok(stringsFrequency);
    }


    /**
     * Метод просмотра содержимого списка
     *
     * @return содержимое списка в json формате
     * @author korvalanni
     */
    @GetMapping("showArrayList")
    public ResponseEntity<List<String>> showArrayList() {
        return ResponseEntity.ok(strings);
    }


    /**
     * Метод просмотра содержимого списка и словаря
     *
     * @return лог о количестве элементов списка и словаря
     * @author korvalanni
     */
    @GetMapping("showAllLength")
    public ResponseEntity<String > showAllLength() {
        return ResponseEntity.ok(String.format("Словарь содержит %d элементов, список содержит %d элементов",
                stringsFrequency.size(), strings.size()));
    }

}
