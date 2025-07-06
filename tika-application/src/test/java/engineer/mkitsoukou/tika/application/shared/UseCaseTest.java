package engineer.mkitsoukou.tika.application.shared;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UseCaseTest {

  @Test
  void lambdaUseCaseShouldReturnValue() {
    UseCase<Integer, Integer> square = x -> x * x;   // simple lambda impl
    assertThat(square.execute(3)).isEqualTo(9);
  }
}
