package com.weeklyreport.config;

import com.weeklyreport.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration class
 * Configures HTTP security, authentication, and authorization for the weekly report system
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:3002}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String[] allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String[] allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    /**
     * Password encoder bean with BCrypt (12 rounds for security)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * DAO authentication provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false); // For better error messages in development
        return authProvider;
    }

    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L); // Cache preflight response for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Main security filter chain configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management (stateless for JWT)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/actuator/health").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                
                // Development and testing endpoints
                .requestMatchers("/api/h2-console/**").permitAll()
                .requestMatchers("/api/actuator/**").hasAuthority("ROLE_ADMIN")
                
                // Admin only endpoints
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("ROLE_ADMIN")
                
                // User management (Admin and HR)
                .requestMatchers(HttpMethod.GET, "/api/users").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/users").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER")
                
                // Department management
                .requestMatchers("/api/departments/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER", "ROLE_DEPARTMENT_MANAGER")
                
                // Report management by role
                .requestMatchers(HttpMethod.GET, "/api/reports").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER", "ROLE_DEPARTMENT_MANAGER", "ROLE_TEAM_LEADER", "ROLE_EMPLOYEE")
                .requestMatchers(HttpMethod.POST, "/api/reports").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER", "ROLE_DEPARTMENT_MANAGER", "ROLE_TEAM_LEADER", "ROLE_EMPLOYEE")
                .requestMatchers(HttpMethod.PUT, "/api/reports/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER", "ROLE_DEPARTMENT_MANAGER", "ROLE_TEAM_LEADER", "ROLE_EMPLOYEE")
                .requestMatchers(HttpMethod.DELETE, "/api/reports/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER", "ROLE_DEPARTMENT_MANAGER")
                
                // Profile management
                .requestMatchers(HttpMethod.GET, "/api/profile").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/profile").authenticated()
                
                // Comments
                .requestMatchers("/api/comments/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER", "ROLE_DEPARTMENT_MANAGER", "ROLE_TEAM_LEADER", "ROLE_EMPLOYEE")
                
                // Templates
                .requestMatchers(HttpMethod.GET, "/api/templates/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/templates").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER", "ROLE_DEPARTMENT_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/templates/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER", "ROLE_DEPARTMENT_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/templates/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HR_MANAGER")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Configure authentication provider
            .authenticationProvider(authenticationProvider())
            
            // Add JWT filter before username/password authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Allow H2 console in development (disable frame options)
        http.headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}