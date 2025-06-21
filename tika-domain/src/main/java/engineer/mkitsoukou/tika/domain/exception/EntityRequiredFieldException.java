package engineer.mkitsoukou.tika.domain.exception;

public class EntityRequiredFieldException extends DomainException {
  public EntityRequiredFieldException(String fieldName) {
    super("Required field '%s' cannot be null", fieldName);
  }
}
