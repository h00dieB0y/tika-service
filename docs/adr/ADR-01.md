# ADR‑001: Dual‑Layer Password Strength Validation

|  Status     | **Accepted**   |
| ----------- | -------------- |
|  Date       | 2025‑07‑07     |
|  Deciders   | Core Auth Team |
|  Supersedes | —              |

---

## 1. Context

* The **domain** already contains a `PlainPassword` value object that enforces **hard invariants**: 8‑64 chars, upper/lower/digit/special.
* We need **stronger** rules (entropy check, repeated‑char limit, breached‑password lookup) without coupling the domain to external libraries (zxcvbn) or changing persisted data.
* Tests must remain deterministic.

## 2. Decision

1. **Keep `PlainPassword` minimal** – it continues to guard fixed, non‑negotiable rules that must hold for every password stored in the system.
2. Introduce an **application‑layer guard** `PasswordPolicyValidator` with a configurable rule set:

    * `RepeatedCharRule(maxRepeat = 4)`
    * `DictionaryRule` (zxcvbn score ≥ 3)
3. Service flow order:

   ```java
   passwordPolicy.validate(cmd.password());      // soft rules
   PlainPassword pw = PlainPassword.of(cmd.password()); // hard rules
   PasswordHash   h = passwordHasher.hash(pw);
   ```
4. Tests inject a **fixed `ClockPort`** and may supply a relaxed policy to isolate domain checks.
5. Domain events no longer call `Instant.now()` directly; timestamp is supplied by application (`clock.now()`).

## 3. Consequences

| Positive                                                                      | Negative                                                                       |
| ----------------------------------------------------------------------------- | ------------------------------------------------------------------------------ |
| Application can **tighten or relax** rules per tenant without a DB migration. | Slight duplication: both layers throw their own exception types.               |
| Domain remains **free of external libs**; maintains persistence portability.  | Application tests must be aware of both exception classes.                     |
| Deterministic unit tests (clock injected, entropy library stub‑able).         | Developers must remember to call validator **before** `PlainPassword.of(...)`. |

## 4. Alternatives Considered

* **Move zxcvbn logic into `PlainPassword`** – rejected: adds heavy lib to domain, slows mutation tests.
* **Remove domain length rule and rely solely on policy** – rejected: weakens invariant; risk if policy is disabled.

## 5. Implementation Tasks

1. Remove `Instant.now()` from `AbstractDomainEvent`; require timestamp in ctor.
2. Add `PasswordPolicyValidator`, `PasswordPolicyRule`, `PasswordPolicyViolation`.
3. Update `RegisterUserService` & other flows to call validator first.
4. Write unit tests per ZOMBIES table.
5. Document in `Testing.md` and update CI.

---
