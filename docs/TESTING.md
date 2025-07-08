# Testing Guidelines

This document describes the conventions, tools, and heuristics we use to test the codebase effectively and maintainably.

---

## 1. Core Testing Principles

| Principle                            | Rationale                                                                                                                                     |
| ------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------- |
| **Fast > Integrated**                | Prefer unit tests that run quickly without external dependencies (databases, networks, frameworks).                                          |
| **Stateful fakes, verifiable mocks** | Use *in‑memory fakes* for stateful dependencies; use **mocks** for behavior verification and side‑effects.                                   |
| **Comprehensive coverage patterns**  | Aim to cover multiple testing scenarios using structured approaches like ZOMBIES heuristic.                                                   |
| **Deterministic time**               | Avoid `System.currentTimeMillis()` or `Instant.now()` in tests—inject controllable time sources.                                            |
| **Consistent assertion style**       | Choose one assertion library and use it consistently across the test suite.                                                                   |

---

## 2. Recommended Tooling

### Testing Frameworks
* **JUnit 5** – Modern testing framework with Jupiter engine
* **AssertJ** – Fluent assertions (`assertThat(…)`) for readability
* **Mockito** – Interaction verification & stubbing when needed

### Quality Tools
* **Static Analysis** – Tools like SpotBugs, PMD, or SonarQube
* **Coverage Analysis** – JaCoCo or similar for coverage metrics
* **Mutation Testing** – PIT or equivalent for testing test quality

### Environment Management
* **Test Profiles** – Separate configurations for different test types:
  * `mvn test` → Fast unit tests (no external dependencies)
  * `mvn verify -Pintegration` → Integration tests with real dependencies
  * Consider using Testcontainers for database/service dependencies

---

## 3. ZOMBIES Testing Heuristic

Use the ZOMBIES heuristic to ensure comprehensive test coverage for business logic:

| Letter | Category                    | Description                                  | Example Test Scenarios                           |
| ------ | --------------------------- | -------------------------------------------- | ------------------------------------------------ |
| **Z**  | **Z**ero / null / blank     | Edge cases with empty/null inputs           | `shouldHandleNullInput`, `shouldRejectEmptyString` |
| **O**  | **O**ne / happy path        | Normal successful execution                  | `shouldSuccessfullyProcessValidRequest`          |
| **M**  | **M**any / collections      | Multiple items, bulk operations              | `shouldProcessMultipleItems`, `shouldHandleLargeDataset` |
| **B**  | **B**oundary / limits       | Edge cases at boundaries                     | `shouldHandleMaxLength`, `shouldRejectOversizedInput` |
| **I**  | **I**nterface / integration | Interactions with dependencies               | `shouldCallExternalService`, `shouldPublishEvent` |
| **E**  | **E**xceptions / errors     | Error conditions and failure scenarios      | `shouldThrowWhenConstraintViolated`, `shouldHandleTimeout` |
| **S**  | **S**imple / basic          | Fundamental behavior verification            | `shouldReturnExpectedValue`, `shouldSetCorrectState` |

### Coverage Recommendations
- **Critical business logic**: Aim to cover 6-7 ZOMBIES categories
- **Utility functions**: Focus on Z, O, B, E categories  
- **Integration points**: Emphasize I, E categories
- **Simple getters/setters**: S category may be sufficient

Document your test coverage mapping in class-level Javadoc when helpful.

---

## 4. Test Doubles Strategy

Choose the appropriate test double based on the dependency's role and your testing goals:

| Dependency Type                    | Recommended Approach          | When to Use                                      | Examples                                         |
| ---------------------------------- | ----------------------------- | ------------------------------------------------ | ------------------------------------------------ |
| **Stateful repositories**          | **In-memory fake**            | Need to verify state changes over time          | `InMemoryUserRepository`, `HashMapCache`        |
| **Pure functions (no state)**      | **Simple stub**               | Just need predictable return values             | `FixedPasswordEncoder`, `StaticTimeProvider`    |
| **External services**              | **Mock with verification**    | Need to verify interactions occurred             | `EmailService`, `PaymentGateway`, `EventBus`    |
| **Complex external APIs**          | **Stub with scenarios**       | Need to simulate various response conditions     | `when(apiClient.call()).thenReturn/thenThrow()` |
| **Infrastructure (DB, network)**   | **Testcontainers/Real impl**  | Integration tests requiring real behavior        | Database connections, message queues            |

### Guidelines
- **Prefer fakes** for complex stateful behavior you control
- **Use mocks** primarily for verification of outbound calls  
- **Keep stubs simple** for predictable return values
- **Avoid over-mocking** - it can make tests brittle

---

## 5. Naming Conventions

### Test Classes
* **Pattern**: `<ClassUnderTest>Test.java`
* **Examples**: `UserServiceTest`, `PaymentProcessorTest`, `ValidationUtilTest`

### Test Methods
* **Readable format**: `should<ExpectedBehavior>When<Condition>()`
* **BDD style**: `given<Precondition>When<Action>Then<Outcome>()`
* **Domain specific**: `as<Actor>When<Action>Should<Expectation>()`

### Examples
```java
// Readable format
shouldReturnTrueWhenEmailIsValid()
shouldThrowExceptionWhenAmountIsNegative()

// BDD style  
givenValidUserWhenRegisteringThenShouldPersistUser()
givenDuplicateEmailWhenRegisteringThenShouldThrowException()

// Domain specific
asAdminWhenDeletingUserShouldRemoveFromDatabase()
asGuestWhenAccessingPrivateResourceShouldReceiveForbidden()
```

### Organization
* **Use `@Nested` classes** for grouping related tests (max depth: 2)
* **Group by scenario** rather than method when it improves clarity
* **Consistent naming** within each test class

---

## 6. Performance Guidelines

### Execution Time Targets
* **Unit test class**: Aim for < 100ms per class (adjust based on complexity)
* **Full test suite**: Target under 10 seconds for rapid feedback
* **Integration tests**: Budget 1-5 seconds per test, run separately from unit tests

### Performance Best Practices
* **Avoid `Thread.sleep()`** - use deterministic time controls and proper synchronization
* **Minimize I/O** in unit tests - use in-memory implementations
* **Parallel execution** - ensure tests are thread-safe when running in parallel  
* **Resource cleanup** - properly close resources to prevent memory leaks
* **Selective test execution** - use test categories/tags for different execution contexts

### Monitoring
* Track test execution times in CI to catch performance regressions
* Set up alerts for when test suite exceeds time budgets  
* Profile slow tests to identify optimization opportunities

---

## 7. Test Quality & Coverage

### Mutation Testing
* **Purpose**: Verify that your tests actually catch bugs by introducing small code changes
* **Tools**: PIT, PITest, or framework-specific mutation testing tools
* **Target**: Aim for >85% mutation score on critical business logic
* **Scope**: Focus on core domain logic and complex algorithms
* **Integration**: Run mutation tests on key modules during CI for quality gates

### Coverage Metrics
* **Line coverage**: Useful baseline, but not sufficient alone
* **Branch coverage**: More meaningful for conditional logic
* **Path coverage**: Consider for complex algorithms  
* **Functional coverage**: Ensure all requirements/use cases are tested

### Quality Indicators
* Tests fail when they should (no false positives)
* Tests pass consistently (deterministic)
* Tests are readable and maintainable
* Tests run quickly and independently

### Execution Time Targets
* **Unit test class**: Aim for < 100ms per class (adjust based on complexity)
* **Full test suite**: Target under 10 seconds for rapid feedback
* **Integration tests**: Budget 1-5 seconds per test, run separately from unit tests

### Performance Best Practices
* **Avoid `Thread.sleep()`** - use deterministic time controls and proper synchronization
* **Minimize I/O** in unit tests - use in-memory implementations
* **Parallel execution** - ensure tests are thread-safe when running in parallel  
* **Resource cleanup** - properly close resources to prevent memory leaks
* **Selective test execution** - use test categories/tags for different execution contexts

### Monitoring
* Track test execution times in CI to catch performance regressions
* Set up alerts for when test suite exceeds time budgets  
* Profile slow tests to identify optimization opportunitieses
* **Pattern**: `<ClassUnderTest>Test.java`
* **Examples**: `UserServiceTest`, `PaymentProcessorTest`, `ValidationUtilTest`

### Test Methods
* **Readable format**: `should<ExpectedBehavior>When<Condition>()`
* **BDD style**: `given<Precondition>When<Action>Then<Outcome>()`
* **Domain specific**: `as<Actor>When<Action>Should<Expectation>()`

### Examples
```java
// Readable format
shouldReturnTrueWhenEmailIsValid()
shouldThrowExceptionWhenAmountIsNegative()

// BDD style  
givenValidUserWhenRegisteringThenShouldPersistUser()
givenDuplicateEmailWhenRegisteringThenShouldThrowException()

// Domain specific
asAdminWhenDeletingUserShouldRemoveFromDatabase()
asGuestWhenAccessingPrivateResourceShouldReceiveForbidden()
```

### Organization
* **Use `@Nested` classes** for grouping related tests (max depth: 2)
* **Group by scenario** rather than method when it improves clarity
* **Consistent naming** within each test classoose the appropriate test double based on the dependency's role and your testing goals:

| Dependency Type                    | Recommended Approach          | When to Use                                      | Examples                                         |
| ---------------------------------- | ----------------------------- | ------------------------------------------------ | ------------------------------------------------ |
| **Stateful repositories**          | **In-memory fake**            | Need to verify state changes over time          | `InMemoryUserRepository`, `HashMapCache`        |
| **Pure functions (no state)**      | **Simple stub**               | Just need predictable return values             | `FixedPasswordEncoder`, `StaticTimeProvider`    |
| **External services**              | **Mock with verification**    | Need to verify interactions occurred             | `EmailService`, `PaymentGateway`, `EventBus`    |
| **Complex external APIs**          | **Stub with scenarios**       | Need to simulate various response conditions     | `when(apiClient.call()).thenReturn/thenThrow()` |
| **Infrastructure (DB, network)**   | **Testcontainers/Real impl**  | Integration tests requiring real behavior        | Database connections, message queues            |

### Guidelines
- **Prefer fakes** for complex stateful behavior you control
- **Use mocks** primarily for verification of outbound calls  
- **Keep stubs simple** for predictable return values
- **Avoid over-mocking** - it can make tests brittle 5** – Modern testing framework with Jupiter engine
* **AssertJ** – Fluent assertions (`assertThat(…)`) for readability
* **Mockito** – Interaction verification & stubbing when needed

### Quality Tools
* **Static Analysis** – Tools like SpotBugs, PMD, or SonarQube
* **Coverage Analysis** – JaCoCo or similar for coverage metrics
* **Mutation Testing** – PIT or equivalent for testing test quality

### Environment Management
* **Test Profiles** – Separate configurations for different test types:
  * `mvn test` → Fast unit tests (no external dependencies)
  * `mvn verify -Pintegration` → Integration tests with real dependencies
  * Consider using Testcontainers for database/service dependencies. This document describes the conventions, tools, and heuristics we use to test the codebase effectively and maintainably.

----

*Last updated: 08 July 2025*
