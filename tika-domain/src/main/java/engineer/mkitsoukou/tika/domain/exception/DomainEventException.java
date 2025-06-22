package engineer.mkitsoukou.tika.domain.exception;

public class DomainEventException extends DomainException {
  public DomainEventException(String message) {
    super("%s", message);
  }
}
