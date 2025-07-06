package engineer.mkistoukou.tika.application.shared;

import jakarta.validation.constraints.Min;

public record PageRequestDto(
    @Min(0) int page,
    @Min(1) int size,
    String sort,
    Direction dir) {


  public static PageRequestDto of(int page, int size) {
    return new PageRequestDto(page, size, "id", Direction.ASC);
  }
}
