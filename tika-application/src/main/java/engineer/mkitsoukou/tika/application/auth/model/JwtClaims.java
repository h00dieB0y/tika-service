package engineer.mkitsoukou.tika.application.auth.model;

import java.time.Instant;
import java.util.Set;

public record JwtClaims(
  String userId,        // subject (sub)
  String jti,           // JWT ID – used for blacklist
  Instant issuedAt,
  Instant expiresAt,
  Set<String> roles     // copied from JWT «roles» claim
) {
    public JwtClaims {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
