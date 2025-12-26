package ca.zhoozhoo.loaddev.security;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converts Keycloak JWT authorization permissions into Spring Security authorities.
 * <p>
 * Extracts permissions from the {@code authorization.permissions} claim and transforms
 * each permission's resource name and scopes into {@link GrantedAuthority} instances.
 * <p>
 * Authority format: {@code resourceName:scope}
 * <p>
 * Example: A permission with {@code rsname="loads"} and {@code scopes=["read", "write"]}
 * produces authorities: {@code loads:read}, {@code loads:write}
 *
 * @author Zhubin Salehi
 * @see ReactiveJwtGrantedAuthoritiesConverterAdapter
 */
class KeycloakPermissionsConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        var authorization = jwt.getClaimAsMap("authorization");
        if (authorization == null) {
            return emptyList();
        }
        
        if (!(authorization.get("permissions") instanceof List<?> rawPermissions)) {
            return emptyList();
        }

        return rawPermissions.stream()
                .filter(p -> p instanceof Map<?, ?>)
                .map(p -> (Map<?, ?>) p)
                .filter(p -> p.get("rsname") != null)
                .flatMap(p -> switch (p.get("scopes")) {
                    case Collection<?> rawScopes -> rawScopes.stream()
                            .filter(s -> s instanceof String)
                            .map(s -> "%s:%s".formatted(p.get("rsname").toString(), s))
                            .map(SimpleGrantedAuthority::new);
                    case null, default -> Stream.empty();
                })
                .map(a -> (GrantedAuthority) a)
                .toList();
    }
}
