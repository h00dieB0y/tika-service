package engineer.mkitsoukou.tika.application.shared;

import java.time.Instant;
import java.time.ZoneId;

public interface ClockPort {
  Instant now();
  ZoneId zone();

  ClockPort SYSTEM = new ClockPort() {
    @Override
    public Instant now() {
      return Instant.now();
    }

    @Override
    public ZoneId zone() {
      return ZoneId.systemDefault();
    }
  };
}
