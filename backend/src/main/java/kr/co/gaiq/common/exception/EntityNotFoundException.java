package kr.co.gaiq.common.exception;

public class EntityNotFoundException extends GaiqBusinessException {

    public EntityNotFoundException(String message) {
        super("ENTITY_NOT_FOUND", message);
    }

    public EntityNotFoundException(String entityName, Object id) {
        super("ENTITY_NOT_FOUND", entityName + "(id=" + id + ")를 찾을 수 없습니다.");
    }
}
