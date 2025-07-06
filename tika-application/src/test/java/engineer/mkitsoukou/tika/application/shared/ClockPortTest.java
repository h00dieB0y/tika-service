package engineer.mkitsoukou.tika.application.shared;

import org.junit.jupiter.api.Test;
import java.time.*;
import static org.assertj.core.api.Assertions.*;

class ClockPortTest {

  /* ---------- S – Simple ---------- */
  @Test
  void systemClockShouldBeCloseToInstantNow() {
    Instant before = Instant.now();
    Instant sysNow = ClockPort.SYSTEM.now();
    Instant after  = Instant.now();

    assertThat(sysNow).isCloseTo(before, Duration.ofMillis(50));
    assertThat(ClockPort.SYSTEM.zone()).isEqualTo(ZoneId.systemDefault());
  }

  /* ---------- I – Interface / mocking ---------- */
  @Test
  void fixedClockProvidesDeterministicNow() {
    Instant fixed = Instant.parse("2025-07-06T12:00:00Z");
    ClockPort fixedClock = new ClockPort() {
      @Override public Instant now()  { return fixed; }
      @Override public ZoneId  zone() { return ZoneOffset.UTC; }
    };

    assertThat(fixedClock.now()).isEqualTo(fixed);
  }
}
