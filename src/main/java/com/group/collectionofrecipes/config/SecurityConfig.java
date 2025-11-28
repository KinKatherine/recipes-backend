package com.group.collectionofrecipes.config;

import com.group.collectionofrecipes.enums.UserRole;
import com.group.collectionofrecipes.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static com.group.collectionofrecipes.utils.ApiConstants.AUTH_LOGIN_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.AUTH_REGISTER_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.AVATAR_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.CATEGORIES_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.CATEGORY_RECIPES_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.CHECK_EMAIL_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.CHECK_USERNAME_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.COMMENTS_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.COMMENT_ID_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.MEASURES_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPES_CONFIRM_UNCONFIRMED_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPES_DELETE_UNCONFIRMED_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.USER_AVATAR_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.EMAIL_VERIFICATION_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RATINGS_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RATING_ID_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPES_CONFIRMED_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPES_FAVOURITE_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPES_POPULAR_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPES_UNCONFIRMED_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPES_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPES_USER_ADDED_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPES_USER_FAVOURITES_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPE_ID_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPE_OF_THE_DAY_URI;
import static com.group.collectionofrecipes.utils.ApiConstants.RECIPE_RECENT_URI;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final UserService userService;
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) //отключена csrf
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auths -> auths

                        //пользователь
                        //можно все пустить по пермит олл!!
                        .requestMatchers(HttpMethod.POST, AUTH_REGISTER_URI).permitAll()
                        .requestMatchers(HttpMethod.POST, AUTH_LOGIN_URI).permitAll()
                        .requestMatchers(HttpMethod.PUT, EMAIL_VERIFICATION_URI).permitAll()
                        .requestMatchers(HttpMethod.POST, USER_AVATAR_URI).authenticated()
                        .requestMatchers(HttpMethod.DELETE, USER_AVATAR_URI).authenticated()
                        .requestMatchers(HttpMethod.GET, CHECK_USERNAME_URI).permitAll()
                        .requestMatchers(HttpMethod.GET, CHECK_EMAIL_URI).permitAll()
                        .requestMatchers(HttpMethod.GET, AVATAR_URI).authenticated()

                        //категории
                        .requestMatchers(HttpMethod.GET, CATEGORIES_URI).permitAll()
                        .requestMatchers(HttpMethod.GET, CATEGORY_RECIPES_URI).permitAll()

                        //комментарии
                        .requestMatchers(HttpMethod.POST, COMMENTS_URI).authenticated()
                        .requestMatchers(HttpMethod.DELETE, COMMENT_ID_URI).hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, COMMENT_ID_URI).hasAuthority(UserRole.ADMIN.name()) //не используется

                        //рейтинг
                        .requestMatchers(HttpMethod.POST, RATINGS_URI).authenticated()
                        .requestMatchers(HttpMethod.DELETE, RATING_ID_URI).authenticated()
                        .requestMatchers(HttpMethod.PUT, RATING_ID_URI).authenticated()

                        //рецепты
                        .requestMatchers(HttpMethod.GET, RECIPE_OF_THE_DAY_URI).permitAll()
                        .requestMatchers(HttpMethod.GET, RECIPE_RECENT_URI).permitAll()
                        .requestMatchers(HttpMethod.GET, RECIPES_FAVOURITE_URI).authenticated()
                        .requestMatchers(HttpMethod.GET, RECIPES_CONFIRMED_URI).authenticated()
                        .requestMatchers(HttpMethod.GET, RECIPES_UNCONFIRMED_URI).hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, RECIPES_POPULAR_URI).hasAuthority(UserRole.ADMIN.name())

                        .requestMatchers(HttpMethod.PUT, RECIPES_CONFIRM_UNCONFIRMED_URI).hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, RECIPES_DELETE_UNCONFIRMED_URI).hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, RECIPES_URI).authenticated()
                        .requestMatchers(HttpMethod.GET, RECIPE_ID_URI).permitAll()


                        //ингредиенты
                        .requestMatchers(HttpMethod.GET, MEASURES_URI).authenticated()

                        .requestMatchers(HttpMethod.POST, RECIPES_URI).authenticated()
                        .requestMatchers(HttpMethod.DELETE, RECIPE_ID_URI).hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, RECIPES_URI + "/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, RECIPES_URI + "/**").hasAnyAuthority("ROLE_ANONYMOUS", UserRole.USER.name(), UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, RECIPES_USER_FAVOURITES_URI).authenticated()
                        .requestMatchers(HttpMethod.GET, RECIPES_USER_ADDED_URI).authenticated()
                        .requestMatchers(HttpMethod.GET, RECIPE_ID_URI).hasAnyAuthority("ROLE_ANONYMOUS", UserRole.USER.name(), UserRole.ADMIN.name())


                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .userDetailsService(userService)
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); //setAllowedOrigins(List.of("http://localhost:4000"))
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}