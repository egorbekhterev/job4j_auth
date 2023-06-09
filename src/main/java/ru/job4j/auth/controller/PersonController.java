package ru.job4j.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.auth.handler.GlobalExceptionHandler;
import ru.job4j.auth.model.Person;
import ru.job4j.auth.model.PersonDTO;
import ru.job4j.auth.service.PersonService;
import ru.job4j.auth.validation.Operation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/**
 * @author: Egor Bekhterev
 * @date: 31.03.2023
 * @project: job4j_auth
 */
@RestController
@RequestMapping("/person")
@AllArgsConstructor
public class PersonController {

    private final PersonService persons;

    private final BCryptPasswordEncoder encoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class.getSimpleName());

    private final ObjectMapper objectMapper;

    /**
     * Обработчик исключений для {@link IllegalArgumentException}. Устанавливает 400 статус ответа, тип содержимого
     * JSON и записывает тело ответа в JSON-объект. Ответ состоит из сообщения и типа возникшего исключения.
     * @param e Исключение, которое было сгенерировано и перехвачено этим обработчиком.
     * @param request HTTP-сервлет запроса, который вызвал исключение.
     * @param response HTTP-сервлет ответа, который будет изменен для содержания сообщения об ошибке и типа.
     * @throws IOException если происходит исключение ввода или вывода.
     * P.S. Этот подход ограничен использованием только в классе, где он определен, поэтому его следует применять
     * только к узко специфическим исключениям, если вы уверены, что их не возникнет в других классах или их нужно
     * будет обрабатывать иначе.
     */
    @ExceptionHandler(value = { IllegalArgumentException.class })
    public void exceptionHandler(Exception e, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() { {
            put("message", e.getMessage());
            put("type", e.getClass());
        }}));
        LOGGER.error(e.getLocalizedMessage());
    }

    @GetMapping("/")
    public List<Person> findAll() {
        return this.persons.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = this.persons.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Person is not found. Please, check the identificator."
        ));

        return ResponseEntity.status(HttpStatus.OK)
                .header("Job4jCustomHeader", "egor")
                .contentType(MediaType.APPLICATION_JSON)
                .body(person);
    }

    @PostMapping("/")
    public ResponseEntity<Person> create(@Validated(Operation.OnCreate.class) @RequestBody Person person) {
        if (person.getLogin() == null || person.getPassword() == null) {
            throw new NullPointerException("Username and password mustn't be empty");
        }
        if (person.getPassword().length() < 6) {
            throw new IllegalArgumentException("Invalid password. Password length must be more than 6 characters.");
        }
        return new ResponseEntity<>(
                this.persons.save(person),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@Validated(Operation.OnUpdate.class) @RequestBody Person person) {
        var rsl = this.persons.update(person);
        return new ResponseEntity<>(
                rsl ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        var rsl = this.persons.delete(id);
        return new ResponseEntity<>(
                rsl ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @PostMapping("/sign-up")
    public void signUp(@Validated(Operation.OnCreate.class) @RequestBody Person person) {
        if (person.getLogin() == null || person.getPassword() == null) {
            throw new NullPointerException("Username and password mustn't be empty");
        }
        if (person.getPassword().length() < 6) {
            throw new IllegalArgumentException("Invalid password. Password length must be more than 6 characters.");
        }
        person.setPassword(encoder.encode(person.getPassword()));
        persons.save(person);
    }

    @GetMapping("/pomDownload")
    public byte[] pomDownload() throws IOException {
        return Files.readAllBytes(Path.of("./pom.xml"));
    }

    /**
     * Метод служит для обновления поля password объекта {@link Person}. Используется Data Transfer Object.
     * @param personDTO DTO объект, содержащий новую информацию о {@link Person}.
     * @param id - идентификатор объекта.
     * @return содержит информацию о результате операции и измененном объекте Person.
     */
    @PatchMapping("/patchDTO/{id}")
    public ResponseEntity<Person> patchDTO(@Valid @RequestBody PersonDTO personDTO, @PathVariable int id) {
        var person = persons.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        person.setPassword(personDTO.getPassword());
        var rsl = persons.update(person);
        return new ResponseEntity<>(person, rsl ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    /**
     * Метод служит для обновления ненулевых полей объекта {@link Person}. Использует рефлексию.
     * @param person Объект типа Person, содержащий значения полей для обновления текущего объекта.
     * @return ResponseEntity с объектом типа Person и статусом OK, если обновление выполнено успешно,
     * или BAD_REQUEST в противном случае.
     * @throws InvocationTargetException если вызов метода завершился исключением.
     * @throws IllegalAccessException если метод не может быть вызван.
     */
    @PatchMapping("/patch")
    public ResponseEntity<Person> patch(@Validated(Operation.OnUpdate.class) @RequestBody Person person) throws InvocationTargetException, IllegalAccessException {
        var currentPerson = persons.findById(person.getId());
        if (currentPerson.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Person is not found. Please, check the identificator.");
        }

        /* Через рефлексию получаем массив методов объекта Person. Далее собираем мапу из геттеров и сеттеров.*/
        var methods = currentPerson.get().getClass().getDeclaredMethods();
        var namePerMethod = new HashMap<String, Method>();
        for (var method : methods) {
            var name = method.getName();
            if (name.startsWith("get") || name.startsWith("set")) {
                namePerMethod.put(name, method);
            }
        }

        for (var name : namePerMethod.keySet()) {
            if (name.startsWith("get")) {
                var getMethod = namePerMethod.get(name);
                var setMethod = namePerMethod.get(name.replace("get", "set"));
                if (setMethod == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Impossible invoke set method from object : " + currentPerson
                                    + ", Check set and get pairs.");
                }
                /* Получаем новые значения для полей объекта, если не null - обновляем поле currentPerson.*/
                var newValue = getMethod.invoke(person);
                if (newValue != null) {
                    setMethod.invoke(currentPerson.get(), newValue);
                }
            }
        }
        var rsl = persons.update(currentPerson.get());
        return new ResponseEntity<>(person, rsl ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}
