package engineer.mkitsoukou.tika.domain.model.event;

import java.time.Instant;

public interface DomainEvent {
  Instant occurredAt();
}

