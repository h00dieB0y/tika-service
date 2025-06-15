package engineer.mkitsoukou.tika.domain.exception;

abstract class DomainException extends RuntimeException {
  protected DomainException(String template, Object... args) { super(String.format(template, args)); }
}
