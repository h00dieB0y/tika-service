package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import java.util.Objects;

public class PasswordChanged extends AbstractDomainEvent {
  private final UserId userId;

  public PasswordChanged(UserId userId) {
    super();
    this.userId = requireNonNull(userId, "userId");
  }

  /**
   * Creates a new PasswordChanged event.
   *
   * @param userId the ID of the user whose password was changed
   * @return a new PasswordChanged event
   */
  public static PasswordChanged of(UserId userId) {
    return new PasswordChanged(userId);
  }

  public UserId getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PasswordChanged that)) {
      return false;
    }

    return Objects.equals(this.userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId);
  }

  @Override
  public String toString() {
    return "PasswordChanged{"
        + "userId=" + userId
        + ", occurredAt=" + occurredAt()
        + '}';
  }
}
