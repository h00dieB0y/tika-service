package engineer.mkitsoukou.tika.application.shared;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class PageResponseDtoTest {

  @Test
  void shouldExposePaginationMetadata() {
    var content = List.of("alice", "bob");
    PageResponseDto<String> dto = new PageResponseDto<>(content, 10, 5, 0, 2);

    assertThat(dto.content()).containsExactly("alice", "bob");
    assertThat(dto.totalElements()).isEqualTo(10);
    assertThat(dto.totalPages()).isEqualTo(5);
    assertThat(dto.page()).isZero();
    assertThat(dto.size()).isEqualTo(2);
  }
}
