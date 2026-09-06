package kr.co.gaiq.common.exception;

public class DuplicateResourceException extends GaiqBusinessException {

    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message);
    }
}
