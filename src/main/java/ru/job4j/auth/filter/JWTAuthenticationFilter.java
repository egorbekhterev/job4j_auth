package ru.job4j.auth.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.job4j.auth.model.Person;
import org.springframework.security.core.userdetails.User;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Класс-фильтр, реализует механизм аутентификации на основе JSON WEB TOKENS.
 * @author: Egor Bekhterev
 * @date: 01.04.2023
 * @project: job4j_auth
 */
@AllArgsConstructor
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    /**
     * Секретный ключ для подписи JWT-токена.
     */
    public static final String SECRET = "SecretKeyToGenJWTs";

    /**
     * 10 дней.
     */
    public static final long EXPIRATION_TIME = 864_000_000;

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String HEADER_STRING = "Authorization";

    public static final String SIGN_UP_URL = "/person/sign-up";

    private AuthenticationManager auth;

    /**
     * Производит попытку аутентификации пользователя, проверяя логин и пароль на соответствие.
     * ObjectMapper служит для автоматической десериализации из JSON.
     * @param request объект HttpServletRequest, который содержит данные запроса.
     * @param response объект HttpServletResponse, который будет использоваться для ответа на запрос.
     * @return объект {@link Authentication}.
     * @throws AuthenticationException если аутентификация не выполнена.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            var creds = new ObjectMapper().readValue(request.getInputStream(), Person.class);

            return auth.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getLogin(),
                            creds.getPassword(),
                            new ArrayList<>()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * После успешной аутентификации формирует JWT-токен из имени пользователя, устанавливает время его действия,
     * подписывает секретным ключом. Этот токен добавляется в заголовок ответа вместе с именем заголовка и префиксом.
     * @param request объект HttpServletRequest, который содержит данные запроса.
     * @param response объект HttpServletResponse, который будет использоваться для ответа на запрос.
     * @param chain объект {@link FilterChain}.
     * @param authResult объект {@link Authentication}.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) {
        String token = JWT.create()
                .withSubject(((User) authResult.getPrincipal()).getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(SECRET.getBytes()));
        response.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
    }
}
