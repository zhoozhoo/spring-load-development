package ca.zhoozhoo.load_development.webui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/login", "/error", "/webjars/**", "/static/**", "/public/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .userInfoEndpoint(userInfo -> userInfo
                    .userAuthoritiesMapper(userAuthoritiesMapper())
                )
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            );
        
        return http.build();
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            
            authorities.forEach(authority -> {
                // Add default USER role for authenticated users
                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                
                if (authority instanceof OidcUserAuthority) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
                    extractAuthoritiesFromClaims(oidcUserAuthority.getAttributes(), mappedAuthorities);
                } else if (authority instanceof OAuth2UserAuthority) {
                    OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority) authority;
                    extractAuthoritiesFromClaims(oauth2UserAuthority.getAttributes(), mappedAuthorities);
                }
            });
            
            return mappedAuthorities;
        };
    }

    private void extractAuthoritiesFromClaims(java.util.Map<String, Object> claims, Set<GrantedAuthority> mappedAuthorities) {
        // Extract roles from claims if available
        Collection<String> roles = (Collection<String>) claims.get("roles");
        if (roles != null) {
            roles.forEach(role -> {
                if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }
                mappedAuthorities.add(new SimpleGrantedAuthority(role));
            });
        }
        
        // Extract groups from claims if available (for some OAuth providers)
        Collection<String> groups = (Collection<String>) claims.get("groups");
        if (groups != null) {
            groups.forEach(group -> {
                String role = "ROLE_" + group.toUpperCase();
                mappedAuthorities.add(new SimpleGrantedAuthority(role));
            });
        }
    }
}
