package ru.job4j.auth.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static ru.job4j.auth.filter.JWTAuthenticationFilter.HEADER_STRING;
import static ru.job4j.auth.filter.JWTAuthenticationFilter.SECRET;
import static ru.job4j.auth.filter.JWTAuthenticationFilter.TOKEN_PREFIX;

/**
 * Класс-фильтр, проверяет на наличие в заголовке запроса токена, если его нет - отправляет статус 403.
 * @author: Egor Bekhterev
 * @date: 01.04.2023
 * @project: job4j_auth
 */
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    /**
     * Извлекает JWT-токен из заголовка, проверяет его подпись на наличие секретного ключа.
     * Далее извлекает имя пользователя из токена.
     * @param request объект HttpServletRequest, который содержит данные запроса.
     * @return объект {@link UsernamePasswordAuthenticationToken},
     * если имя пользователя не получено или токен не найден - возвращает null.
     */
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            /* parse the token. */
            String user = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                    .build()
                    .verify(token.replace(TOKEN_PREFIX, ""))
                    .getSubject();

            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }
            return null;
        }
        return null;
    }

    /**
     * Производит извлечение JWT-токена из заголовка и проверяет его корректность.
     * Получает аутентифицированного пользователя, записывает его в Security Context.
     * @param request объект HttpServletRequest, который содержит данные запроса.
     * @param response объект HttpServletResponse, который будет использоваться для ответа на запрос.
     * @param chain объект {@link FilterChain}.
     * @throws IOException в случае возникновения ошибок ввода-вывода в процессе обработки запроса.
     * @throws ServletException в случае возникновения ошибок при обработке запроса сервлетом.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String header = request.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return ;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
}
