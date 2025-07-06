package engineer.mkitsoukou.tika.application.shared;

import jakarta.validation.constraints.Min;

public record PageRequestDto(
    @Min(0) int page,
    @Min(1) int size,
    String sort,
    Direction dir) {

  private static final String DEFAULT_SORT = "id";

  public static PageRequestDto of(int page, int size) {
    return new PageRequestDto(page, size, DEFAULT_SORT, Direction.ASC);
  }
}
