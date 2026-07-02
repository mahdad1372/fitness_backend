package com.example.fitness.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfiguration(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // AuthenticationManager for login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Password encoder (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Security filter configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .cors().and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests()

                // Allow CORS preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // PUBLIC endpoints
                .requestMatchers(
                        "/users/login",
                        "/me",
                        "/numberchatpeople",
                        "/getstatsroom",
                        "/checkuser",
                        "/removepeople",
                        "/removewaitinglist",
                        "/getwaitinglist",
                        "/addwaitinglist",
                        "/addpeople",
                        "/users/adduser",
                        "/users/public-info",
                        "/users/about",
                        "/users/login",
                        "/users",
                        "/google-fit/heart-rate",
                        "/google-fit/**",
                        "/users/bmi/**",
                        "/health_metric/**",
                        "/foods/**",
                        "/foods/addfoods",
                        "/foods/updatefoods",
                        "/foods/food_user_id/**",
                        "/goals/add",
                        "/goals/test",
                        "/goals/**",
                        "/goals/updategoal/**",
                        "/goals/before-image/**",
                        "/goals/after-image/**",
                        "/goals/mainpage",
                        "/health_metric/deletehealth/**",
                        "/health_metric/addmetrics",
                        "/health_metric/update_healthmetric",
                        "/health_metric/calculate",
                        "/health_metric/cardiovascular/**",
                        "/worksout",
                        "/worksout/addworksout",
                        "/worksout/getworkouts",
                        "/ws",
                        "/ws/**"
                ).permitAll()

                // All other endpoints require authentication
                .anyRequest().authenticated();

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Global CORS configuration
    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://fitness-frontend-oracle.vercel.app"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Origin",
                "Accept"
        ));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}