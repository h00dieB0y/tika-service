package engineer.mkitsoukou.tika.infrastructure.time;

import engineer.mkitsoukou.tika.application.shared.ClockPort;

import java.time.Clock;
import java.time.Instant;

/**
 * Production implementation of {@link ClockPort} using {@link Clock#systemUTC()}.
 */
public class SystemClockAdapter implements ClockPort {

  private final Clock clock;

  public SystemClockAdapter() {
    this.clock = Clock.systemUTC();
  }

  SystemClockAdapter(Clock clock) {
    this.clock = clock;
  }

  @Override
  public Instant now() {
    return clock.instant();
  }
}

