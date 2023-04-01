package ru.job4j.auth.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.job4j.auth.filter.JWTAuthenticationFilter;
import ru.job4j.auth.filter.JWTAuthorizationFilter;
import ru.job4j.auth.service.UserDetailsServiceImpl;

import static ru.job4j.auth.filter.JWTAuthenticationFilter.SIGN_UP_URL;

/**
 * @author: Egor Bekhterev
 * @date: 01.04.2023
 * @project: job4j_auth
 */
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private UserDetailsServiceImpl userDetailsService;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * CORS - cross-origin resource sharing.
     * Разрешает запросы со всех источников(*), с любым заголовком (*).
     * @return объект CorsConfigurationSource с установленными настройками для разрешения доступа к ресурсам
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    /**
     * Конфигурирует Spring Security для обработки запросов.
     * cors() - включает поддержку CORS.
     * crsf().disable() - отключает проверку подделки межсайтовых запросов.
     * .antMatchers - разрешает регистрацию пользователя для неаутентифицированных пользователей.
     * .anyRequest().authenticated() - другие запросы доступны только аутентифицированным пользователям.
     * @param http {@link HttpSecurity} конфигурируемый объект.
     * @throws Exception в случае возникновения исключения настройки безопасности.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(new JWTAuthenticationFilter(authenticationManager()))
                .addFilter(new JWTAuthorizationFilter(authenticationManager()))
                /* this disables session creation on Spring Security */
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    /**
     * Определяется сервис для обработки запросов аутентификации пользователей. Определяет класс для кодирования
     * и проверки паролей пользователей.
     * @param auth {@link AuthenticationManagerBuilder} конфигурируемый объект.
     * @throws Exception в случае возникновения исключения настройки аутентификации.
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }
}
