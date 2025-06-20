package engineer.mkitsoukou.tika.domain.model.event;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class for domain events providing common functionality.
 */
public abstract class AbstractDomainEvent implements DomainEvent {
  private final Instant occurredAt;

  /**
   * Constructs a new domain event with the current timestamp.
   */
  protected AbstractDomainEvent() {
    this.occurredAt = Instant.now();
  }

  /**
   * Constructs a domain event with a specific timestamp.
   * Primarily used for testing or event replay scenarios.
   *
   * @param occurredAt the timestamp when the event occurred
   */
  protected AbstractDomainEvent(Instant occurredAt) {
    this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
  }

  @Override
  public Instant occurredAt() {
    return occurredAt;
  }

  /**
   * Helper method to verify that the provided value is not null.
   *
   * @param value the value to check
   * @param parameterName the name of the parameter for the error message
   * @param <T> the type of the value
   * @return the value if it is not null
   * @throws NullPointerException if the value is null
   */
  protected static <T> T requireNonNull(T value, String parameterName) {
    return Objects.requireNonNull(value, parameterName + " must not be null");
  }
}
