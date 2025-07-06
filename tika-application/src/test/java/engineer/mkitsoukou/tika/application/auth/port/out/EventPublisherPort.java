package engineer.mkitsoukou.tika.application.auth.port.out;

import engineer.mkitsoukou.tika.domain.model.event.DomainEvent;

public interface EventPublisherPort {

  void publish(DomainEvent event);
}
