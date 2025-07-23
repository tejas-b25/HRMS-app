package com.quantumsoft.hrms.securityConfig;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class AppConfig {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AppFilter appFilter;

    private final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(getPasswordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("in filter chain");

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests

                        // Publicly accessible endpoints
                        .requestMatchers("/api/user/login", "/api/user/forgotPwd", "/api/user/resetPwd", "/api/user/single/**", "/api/user/logout").permitAll()
                        .requestMatchers("/api/admin/login").permitAll()
                        .requestMatchers("/api/auditLog/logs").permitAll()
                        .requestMatchers("/api/employees/single/**", "/api/employees/all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/departments", "/api/departments/**").permitAll()
                        .requestMatchers("/api/leaveType/get", "/api/leaveType/get/**").permitAll()
                        .requestMatchers("/api/optionalHoliday/get").permitAll()
                        .requestMatchers("/notification.html", "/notification.html/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payroll/**").hasRole("ADMIN")


                        // ✅ Allow WebSocket connections
                        .requestMatchers("/api/ws-notifications/**").permitAll()

                        // Swagger + API docs
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/api/v1/auth/**",
                                "/v2/api-docs",
                                "/swagger-resources", "/configuration/ui",
                                "/configuration/security"
                        ).permitAll()

                        // Allow public access to review & announcement-related APIs
                        .requestMatchers("/api/review-cycles/**").permitAll()
                        .requestMatchers("/api/announcements/visible").permitAll()
                        .requestMatchers("/api/announcements/**").hasAnyRole("ADMIN", "HR")

                        // Role-based access control
                        .requestMatchers("/api/user/users").hasAnyRole("ADMIN", "HR", "MANAGER", "FINANCE")
                        .requestMatchers("/api/user/updateRole").hasAnyRole("ADMIN", "HR")
                        .requestMatchers("/api/user/manageStatus/**").hasRole("ADMIN")
                        .requestMatchers("/api/employees/myProfile").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
                        .requestMatchers("/api/attendance/**").hasAnyRole("EMPLOYEE", "HR", "MANAGER", "FINANCE")
                        .requestMatchers("/api/salaryStructure/get/by-employee/**").hasAnyRole("HR", "ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/employee-benefits/**").hasRole("HR")
                        .requestMatchers("/api/documents/verify/**").hasAnyRole("HR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/appreciations").hasAnyRole("EMPLOYEE", "MANAGER", "HR")


                        // All other /api/** endpoints require authentication
                        .requestMatchers("/api/**").authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(appFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized - Please log in\"}");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Forbidden - You do not have access to this resource\"}");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200")); // Your Angular frontend
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
