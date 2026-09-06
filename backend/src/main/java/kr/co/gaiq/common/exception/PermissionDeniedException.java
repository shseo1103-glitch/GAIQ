package kr.co.gaiq.common.exception;

public class PermissionDeniedException extends GaiqBusinessException {

    public PermissionDeniedException(String message) {
        super("PERMISSION_DENIED", message);
    }
}
