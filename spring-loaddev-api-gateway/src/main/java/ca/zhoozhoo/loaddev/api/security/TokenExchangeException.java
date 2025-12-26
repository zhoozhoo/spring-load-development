package ca.zhoozhoo.loaddev.api.security;

/**
 * Exception thrown when UMA token exchange fails.
 * 
 * <p>This exception wraps lower-level exceptions from the token exchange process
 * and provides meaningful context about the failure.</p>
 * 
 * @author Zhubin Salehi
 */
public class TokenExchangeException extends RuntimeException {

    public TokenExchangeException(String message) {
        super(message);
    }

    public TokenExchangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
