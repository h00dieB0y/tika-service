package engineer.mkitsoukou.tika.infrastructure.time;

import engineer.mkitsoukou.tika.application.shared.ClockPort;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.*;

/**
 * Test class for {@link SystemClockAdapter} following ZOMBIES heuristic.
 *
 * Coverage mapping:
 * - Z (Zero/null): null clock handling
 * - O (One/happy): normal operation with system clock
 * - M (Many): N/A (no collections)
 * - B (Boundary): extreme time values
 * - I (Interface): ClockPort contract compliance
 * - E (Exceptions): error scenarios
 * - S (Simple): basic functionality
 */
class SystemClockAdapterTest {

    /* ---------- S – Simple / Basic Functionality ---------- */
    @Nested
    class BasicFunctionality {

        @Test
        void shouldImplementClockPortInterface() {
            SystemClockAdapter adapter = new SystemClockAdapter();

            assertThat(adapter).isInstanceOf(ClockPort.class);
        }

        @Test
        void shouldReturnNonNullInstant() {
            SystemClockAdapter adapter = new SystemClockAdapter();

            Instant result = adapter.now();

            assertThat(result).isNotNull();
        }
    }

    /* ---------- O – One / Happy Path ---------- */
    @Nested
    class HappyPath {

        @Test
        void shouldReturnCurrentTimeWhenUsingSystemClock() {
            SystemClockAdapter adapter = new SystemClockAdapter();

            Instant before = Instant.now();
            Instant result = adapter.now();
            Instant after = Instant.now();

            assertThat(result).isBetween(before.minusMillis(10), after.plusMillis(10));
        }

        @Test
        void shouldReturnConsistentTimeWithFixedClock() {
            Instant fixedTime = Instant.parse("2025-07-08T12:00:00Z");
            Clock fixedClock = Clock.fixed(fixedTime, ZoneOffset.UTC);
            SystemClockAdapter adapter = new SystemClockAdapter(fixedClock);

            Instant result1 = adapter.now();
            Instant result2 = adapter.now();

            assertThat(result1).isEqualTo(fixedTime);
            assertThat(result2).isEqualTo(fixedTime);
            assertThat(result1).isEqualTo(result2);
        }
    }

    /* ---------- B – Boundary / Limits ---------- */
    @Nested
    class BoundaryConditions {

        @Test
        void shouldHandleMinInstant() {
            Clock minClock = Clock.fixed(Instant.MIN, ZoneOffset.UTC);
            SystemClockAdapter adapter = new SystemClockAdapter(minClock);

            Instant result = adapter.now();

            assertThat(result).isEqualTo(Instant.MIN);
        }

        @Test
        void shouldHandleMaxInstant() {
            Clock maxClock = Clock.fixed(Instant.MAX, ZoneOffset.UTC);
            SystemClockAdapter adapter = new SystemClockAdapter(maxClock);

            Instant result = adapter.now();

            assertThat(result).isEqualTo(Instant.MAX);
        }

        @Test
        void shouldHandleEpochInstant() {
            Clock epochClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
            SystemClockAdapter adapter = new SystemClockAdapter(epochClock);

            Instant result = adapter.now();

            assertThat(result).isEqualTo(Instant.EPOCH);
        }
    }

    /* ---------- I – Interface / Integration ---------- */
    @Nested
    class InterfaceCompliance {

        @Test
        void shouldComplyWithClockPortContract() {
            SystemClockAdapter adapter = new SystemClockAdapter();

            // Test that it behaves like any other ClockPort implementation
            ClockPort clockPort = adapter;
            Instant result = clockPort.now();

            assertThat(result).isNotNull();
            assertThat(result).isBeforeOrEqualTo(Instant.now().plusSeconds(1));
        }

        @Test
        void shouldWorkWithDifferentTimeZones() {
            Instant fixedTime = Instant.parse("2025-07-08T12:00:00Z");
            Clock tokyoClock = Clock.fixed(fixedTime, ZoneId.of("Asia/Tokyo"));
            Clock nyClock = Clock.fixed(fixedTime, ZoneId.of("America/New_York"));

            SystemClockAdapter tokyoAdapter = new SystemClockAdapter(tokyoClock);
            SystemClockAdapter nyAdapter = new SystemClockAdapter(nyClock);

            // Same instant regardless of zone (since we're dealing with Instant)
            assertThat(tokyoAdapter.now()).isEqualTo(nyAdapter.now());
        }
    }

    /* ---------- E – Exceptions / Error Scenarios ---------- */
    @Nested
    class ErrorScenarios {

        @Test
        void shouldThrowWhenClockIsNull() {
            assertThatThrownBy(() -> new SystemClockAdapter(null))
                .isInstanceOf(NullPointerException.class);
        }

    }

    /* ---------- Z – Zero / Null / Edge Cases ---------- */
    @Nested
    class EdgeCases {

        @Test
        void shouldHandleSystemDefaultClockCreation() {
            SystemClockAdapter adapter = new SystemClockAdapter();

            // Verify it uses system UTC clock by default
            Instant result = adapter.now();
            Instant systemUtc = Clock.systemUTC().instant();

            // Should be very close to system UTC time
            assertThat(result).isBetween(
                systemUtc.minusMillis(100),
                systemUtc.plusMillis(100)
            );
        }

        @Test
        void shouldHandleMultipleCallsInSequence() {
            SystemClockAdapter adapter = new SystemClockAdapter();

            Instant first = adapter.now();
            Instant second = adapter.now();
            Instant third = adapter.now();

            // All should be valid instants
            assertThat(first).isNotNull();
            assertThat(second).isNotNull();
            assertThat(third).isNotNull();

            // Should be in chronological order (or equal if very fast)
            assertThat(second).isAfterOrEqualTo(first);
            assertThat(third).isAfterOrEqualTo(second);
        }
    }

    /* ---------- Performance / Thread Safety ---------- */
    @Nested
    class PerformanceAndThreadSafety {

        @Test
        void shouldBeThreadSafe() throws InterruptedException {
            SystemClockAdapter adapter = new SystemClockAdapter();

            // Test concurrent access
            Thread[] threads = new Thread[10];
            Instant[] results = new Instant[10];

            for (int i = 0; i < 10; i++) {
                final int index = i;
                threads[i] = new Thread(() -> results[index] = adapter.now());
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // All results should be valid
            for (Instant result : results) {
                assertThat(result).isNotNull();
            }
        }
    }

    /* ---------- Constructor Variants ---------- */
    @Nested
    class ConstructorBehavior {

        @Test
        void shouldInitializeWithSystemUtcClockByDefault() {
            SystemClockAdapter adapter = new SystemClockAdapter();

            Instant result = adapter.now();
            Instant systemUtc = Clock.systemUTC().instant();

            // Should be very close to system UTC (within 50ms)
            assertThat(result).isBetween(
                systemUtc.minusMillis(50),
                systemUtc.plusMillis(50)
            );
        }

        @Test
        void shouldUseProvidedClock() {
            Instant fixedTime = Instant.parse("2025-07-08T15:30:00Z");
            Clock providedClock = Clock.fixed(fixedTime, ZoneOffset.UTC);

            SystemClockAdapter adapter = new SystemClockAdapter(providedClock);

            assertThat(adapter.now()).isEqualTo(fixedTime);
        }
    }
}
