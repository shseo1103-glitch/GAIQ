package kr.co.gaiq.common.exception;

public abstract class GaiqBusinessException extends RuntimeException {

    private final String code;

    protected GaiqBusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
