# Testing Guidelines

This document describes the conventions, tools, and heuristics we use to test the **Tika** code‑base.

---

## 1. Philosophies

| Principle                            | Rationale                                                                                                                                     |
| ------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------- |
| **Fast > Integrated**                | The vast majority of tests run without Spring context, DB or network.                                                                         |
| **Stateful fakes, verifiable mocks** | Use *hand‑rolled fakes* for in‑memory repositories / hashers; use **Mockito** mocks for outbound ports that only emit events or side‑effects. |
| **ZOMBIES coverage**                 | Every use‑case/service test must cover at least 5 of the 7 ZOMBIES categories.                                                                |
| **Deterministic time**               | Never call `Instant.now()` directly in tests—inject a fixed `ClockPort`.                                                                      |
| **One assertion family**             | Use **AssertJ** for all assertions; avoid JUnit `Assertions` mix.                                                                             |

---

## 2. Tooling

* **JUnit 5** – Jupiter engine.
* **AssertJ** – fluent assertions (`assertThat(…)`).
* **Mockito** – interaction verification & stubbing.
* **SpotBugs** – static analysis; suppress only with justification.

### Maven profiles

* `mvn test` → unit tests (no Spring context).
* `mvn verify -Pintegration` → spins Testcontainers for slice/integration tests.

---

## 3. ZOMBIES Heuristic

| Letter | Description                 | Example in *RegisterUserServiceTest*         |
| ------ | --------------------------- | -------------------------------------------- |
| **Z**  | **Z**ero / null / blank     | `blankPasswordShouldFail`                    |
| **O**  | **O**ne / happy path        | `happyPathShouldPersistUserAndReturnDto`     |
| **M**  | **M**any / concurrency      | `parallelRegistrationsWithUniqueEmails`      |
| **B**  | **B**oundary / off‑by‑one   | `passwordLengthBoundary`                     |
| **I**  | **I**nterface / mocks       | `shouldPublishUserRegisteredEvent`           |
| **E**  | **E**xceptions / error flow | `duplicateEmailShouldThrowSpecificException` |
| **S**  | **S**imple / smoke          | `dtoShouldContainExpectedValues`             |

Every new service test‐class must map test methods to the table in its Javadoc.

---

## 4. Fakes vs Mocks Matrix

|  Dependency type                   |  Guideline                    |  Example                                         |
| ---------------------------------- | ----------------------------- | ------------------------------------------------ |
| Repository, password hasher, Clock | **Fake / Stub**               | `InMemUserRepo`, `StubHasher`, fixed `ClockPort` |
| Event bus, Mailer, SMS             | **Mockito mock** + `verify()` | `EventPublisherPort`                             |
| External SDK needing errors        | **Mockito stub**              | `when(s3.putObject…).thenThrow(...)`             |

---

## 5. Naming conventions

* **Test file**: `<ClassUnderTest>Test.java`
* **Nested tests** (`@Nested`) for method/group if needed, max depth 2.
* **Method name**: `asActorWhenActionIfConditionShouldExpectation()` (see README).

---

## 6. Performance budget

* Unit test class < 50 ms.
* Suite green under 5 s.
* No `Thread.sleep`—use deterministic clocks & concurrency utilities.

---

## 7. Mutation testing

* **PIT** runs on domain + application modules every PR; target > 90% surviving mutants killed on critical aggregates.

---

## 8. CI enforcement

* GitHub Actions `test.yml` fails build if:

    * Unit tests fail
    * SpotBugs high/medium warnings not suppressed with justification
    * Mutation score Δ‑5% vs main.

---

*Updated: 2025‑07‑07*
