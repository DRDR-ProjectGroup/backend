package com.dorandoran.global.security;


import com.dorandoran.global.security.cors.CorsProperties;
import com.dorandoran.global.security.entrypoint.CustomAccessDeniedEntryPoint;
import com.dorandoran.global.security.entrypoint.CustomAuthenticationEntryPoint;
import com.dorandoran.global.security.filter.AuthenticationFilter;
import com.dorandoran.global.security.filter.GuestTokenFilter;
import com.dorandoran.global.security.filter.RequestLoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.dorandoran.global.jwt.JWTConstant.ACCESS_TOKEN_HEADER;
import static com.dorandoran.global.jwt.JWTConstant.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CorsProperties corsProperties;
    private final AuthenticationFilter authenticationFilter;
    private final GuestTokenFilter guestTokenFilter;
    private final RequestLoggingFilter requestLoggingFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedEntryPoint customAccessDeniedEntryPoint;

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of(ACCESS_TOKEN_HEADER, CONTENT_TYPE));
        configuration.setExposedHeaders(List.of(ACCESS_TOKEN_HEADER));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin
                        )
                )
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests

                        // default
                        .requestMatchers(
                                "/",
                                "/h2-console/**",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()

                        // member domain
                        .requestMatchers(
                                "/api/v1/members/join",
                                "/api/v1/members/login",
                                "/api/v1/members/sendEmail",
                                "/api/v1/members/verifyEmail"
                        ).permitAll()
                        .requestMatchers("/api/v1/members/**").authenticated()

                        // post domain
                        .requestMatchers(GET, "/api/v1/posts/**", "/api/v1/posts").permitAll()
                        .requestMatchers("/api/v1/posts/**").authenticated()

                        // comment domain
                        .requestMatchers(GET, "/api/v1/posts/*/comments/**", "/api/v1/posts/*/comments").permitAll()
                        .requestMatchers("/api/v1/posts/*/comments/**").authenticated()

                        // message domain
                        .requestMatchers("/api/v1/messages/**").authenticated()

                        // 관리자 전용
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // auth
                        .requestMatchers("/api/v1/auth/me").authenticated()

                        // 나머지는 허용
                        .anyRequest().permitAll()
                )
                .exceptionHandling((exception) -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedEntryPoint))
                .addFilterBefore(guestTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
        ;

        return http.build();
    }
}