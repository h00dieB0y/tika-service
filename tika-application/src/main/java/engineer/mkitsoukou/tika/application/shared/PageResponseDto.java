package engineer.mkitsoukou.tika.application.shared;

import java.util.Collections;
import java.util.List;

public record PageResponseDto<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int page,
    int size
) {
  public PageResponseDto {
    content = List.copyOf(content);
  }

  @Override
  public List<T> content() {
    return Collections.unmodifiableList(content);
  }
}
