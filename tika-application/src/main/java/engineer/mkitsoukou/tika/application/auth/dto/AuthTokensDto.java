package engineer.mkitsoukou.tika.application.auth.dto;

import java.time.Instant;

public record AuthTokensDto(
    String accessToken,
    String refreshToken,
    Instant expiresAt
) {
}
