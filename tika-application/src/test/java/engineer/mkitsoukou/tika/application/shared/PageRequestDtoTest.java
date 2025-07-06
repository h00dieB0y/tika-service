package engineer.mkitsoukou.tika.application.shared;

import org.junit.jupiter.api.*;
import jakarta.validation.*;

import static org.assertj.core.api.Assertions.*;

class PageRequestDtoTest {

  private static Validator validator;

  @BeforeAll
  static void init() {
    ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
    validator = vf.getValidator();
  }

  @Test
  void shouldFailWhenPageIsNegative() {
    PageRequestDto dto = new PageRequestDto(-1, 10, "id", Direction.ASC);
    assertThat(validator.validate(dto))
      .hasSize(1)  // @Min(0) violated
      .anyMatch(v -> v.getPropertyPath().toString().equals("page"));
  }

  @Test
  void shouldFailWhenSizeIsZero() {
    PageRequestDto dto = new PageRequestDto(0, 0, "id", Direction.ASC);
    assertThat(validator.validate(dto))
      .extracting(ConstraintViolation::getPropertyPath)
      .anyMatch(p -> p.toString().equals("size"));
  }

  @Test
  void factoryMethodShouldPopulateDefaults() {
    PageRequestDto dto = PageRequestDto.of(1, 20);
    assertThat(dto.page()).isEqualTo(1);
    assertThat(dto.size()).isEqualTo(20);
    assertThat(dto.sort()).isEqualTo("id");
    assertThat(dto.dir()).isEqualTo(Direction.ASC);
  }

  @Test
  void shouldAcceptManySortFields() {
    PageRequestDto dto = new PageRequestDto(2, 50, "email,name,createdAt", Direction.DESC);
    assertThat(validator.validate(dto)).isEmpty();          // no constraint errors
    assertThat(dto.sort().split(",")).hasSize(3);
  }
}
