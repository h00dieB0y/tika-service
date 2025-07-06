package engineer.mkistoukou.tika.shared;

import java.time.Instant;
import java.time.ZoneId;

public interface ClockPort {
  Instant now();
  ZoneId zoneId();

  ClockPort SYSTEM = new ClockPort() {
    @Override
    public Instant now() {
      return Instant.now();
    }

    @Override
    public ZoneId zoneId() {
      return ZoneId.systemDefault();
    }
  };
}
