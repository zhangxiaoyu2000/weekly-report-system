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
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/debug/**").permitAll()  // Debug endpoints for development
                
                // Development and testing endpoints
                .requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                
                // Admin only endpoints
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                
                // User profile management (allow users to update their own profile)
                .requestMatchers(HttpMethod.GET, "/users/profile").authenticated()
                .requestMatchers(HttpMethod.PUT, "/users/profile").authenticated()
                
                // User management (Admin and Super Admin)
                .requestMatchers(HttpMethod.GET, "/users").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/users").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                
                // Department management (Admin only)
                .requestMatchers("/departments/**").hasAnyAuthority("ROLE_ADMIN")
                
                // Report management by role
                .requestMatchers(HttpMethod.GET, "/reports").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/reports").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/reports/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/reports/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                
                // Profile management
                .requestMatchers(HttpMethod.GET, "/profile").authenticated()
                .requestMatchers(HttpMethod.PUT, "/profile").authenticated()
                
                // Comments
                .requestMatchers("/comments/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                
                // Templates
                .requestMatchers(HttpMethod.GET, "/templates/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/templates").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/templates/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/templates/**").hasAnyAuthority("ROLE_ADMIN")
                
                // AI Analysis endpoints
                .requestMatchers("/ai/analysis/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                .requestMatchers("/ai/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                
                // API endpoints for projects and weekly reports (context-path is /api, so patterns are relative)
                .requestMatchers("/projects/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                .requestMatchers("/simple/projects/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                .requestMatchers("/weekly-reports/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                
                // Task management endpoints
                .requestMatchers("/tasks/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                
                // User profile endpoints - allow MANAGER to access their own profile
                .requestMatchers(HttpMethod.GET, "/users/profile").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/users/profile").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                // Other user management endpoints - Admin only
                .requestMatchers("/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                
                // Simple project endpoints (backward compatibility)
                .requestMatchers("/simple/projects/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                .requestMatchers("/simple/weekly-reports/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_SUPER_ADMIN")
                
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