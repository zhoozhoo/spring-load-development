package ca.zhoozhoo.loaddev.api.security;

/**
 * Represents a UMA permission token obtained from Keycloak token exchange.
 * 
 * <p>A permission token is an enhanced access token that includes resource-specific
 * permissions based on Keycloak's authorization policies. It is obtained through
 * the UMA grant type: {@code urn:ietf:params:oauth:grant-type:uma-ticket}.</p>
 * 
 * <p>This token can be forwarded to downstream microservices for fine-grained
 * authorization decisions without requiring additional Keycloak calls.</p>
 * 
 * @param accessToken the UMA permission token value
 * @param tokenType the token type (typically "Bearer")
 * @param expiresIn token expiration time in seconds
 * @param scope the granted scopes
 * 
 * @author Zhubin Salehi
 */
public record UmaPermissionToken(

        String accessToken,

        String tokenType,

        Integer expiresIn,
        
        String scope
) {
    /**
     * Returns the formatted Authorization header value.
     * 
     * @return a string in the format "Bearer {token}"
     */
    public String toAuthorizationHeaderValue() {
        return "%s %s".formatted(tokenType, accessToken);
    }
}
