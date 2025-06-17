package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

public class RoleAssigned implements DomainEvent{
  private final UserId userId;
  private final RoleId roleId;
  private final Instant occurredAt;

  public RoleAssigned(UserId userId, RoleId roleId) {
    this.userId = Objects.requireNonNull(userId, "userId must not be null");
    this.roleId = Objects.requireNonNull(roleId, "roleId must not be null");
    this.occurredAt = Instant.now();
  }

  public static RoleAssigned of(UserId userId, RoleId roleId) {
    return new RoleAssigned(userId, roleId);
  }

  @Override
  public Instant occurredAt() {
    return occurredAt;
  }

  public RoleId getRoleId() {
    return roleId;
  }

  public UserId getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RoleAssigned that)) {
      return false;
    }

    return Objects.equals(roleId, that.roleId)
        && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, userId);
  }

  @Override
  public String toString() {
    return "RoleAssigned{"
        + "roleId=" + roleId
        + ", userId=" + userId
        + ", occurredAt=" + occurredAt
        + '}';
  }
}
