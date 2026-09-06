package kr.co.gaiq.common.exception;

public class BusinessRuleViolationException extends GaiqBusinessException {

    public BusinessRuleViolationException(String message) {
        super("BUSINESS_RULE_VIOLATION", message);
    }

    public BusinessRuleViolationException(String code, String message) {
        super(code, message);
    }
}
