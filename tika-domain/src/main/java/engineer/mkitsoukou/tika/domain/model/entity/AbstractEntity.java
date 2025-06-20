package engineer.mkitsoukou.tika.domain.model.entity;

import engineer.mkitsoukou.tika.domain.exception.DomainEventException;
import engineer.mkitsoukou.tika.domain.exception.EntityRequiredFieldException;
import engineer.mkitsoukou.tika.domain.model.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for all domain entities that need to track and publish domain events.
 * Provides common functionality for event handling.
 */
public abstract class AbstractEntity {
  private final transient List<DomainEvent> events = new ArrayList<>();

  /**
   * Utility method to verify that a value is not null.
   *
   * @param value the value to check
   * @param name  the name of the parameter for the error message
   * @param <T>   the type of the value
   * @return the value if it is not null
   * @throws EntityRequiredFieldException if the value is null
   */
  protected static <T> T requireNonNull(T value, String name) {
    if (value == null) {
      throw new EntityRequiredFieldException(name);
    }
    return value;
  }

  /**
   * Records a domain event to be published later.
   *
   * @param event the domain event to be published
   * @throws DomainEventException if the event is null
   */
  protected void recordEvent(DomainEvent event) {
    if (event == null) {
      throw new DomainEventException("Domain event cannot be null");
    }
    events.add(event);
  }

  /**
   * Returns and clears all recorded domain events.
   * This method is typically called by the repository or application service
   * after persisting the entity.
   *
   * @return an unmodifiable list of domain events
   */
  public List<DomainEvent> pullEvents() {
    List<DomainEvent> pulledEvents = List.copyOf(events);
    events.clear();
    return pulledEvents;
  }
}
