package engineer.mkitsoukou.tika.domain.exception;

public class IncorrectPasswordException extends DomainException {
  public IncorrectPasswordException() {
    super("Old password is incorrect");
  }
}
