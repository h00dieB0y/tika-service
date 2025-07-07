package engineer.mkitsoukou.tika.application.shared;

import java.time.Instant;
import java.time.ZoneId;

public interface ClockPort {
  Instant now();
  default ZoneId zone() {
    return ZoneId.systemDefault();
  }

  ClockPort SYSTEM = new ClockPort() {
    @Override
    public Instant now() {
      return Instant.now();
    }
  };
}
