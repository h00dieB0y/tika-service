package engineer.mkitsoukou.tika.application.auth.port.out;

import engineer.mkitsoukou.tika.application.auth.dto.AuthTokensDto;
import engineer.mkitsoukou.tika.application.auth.model.AuthSubject;
import java.time.Instant;

public interface JwtIssuerPort {
  AuthTokensDto issueTokens(AuthSubject subject, Instant now);
}

