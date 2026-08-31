# Keycloak SPI

Custom Keycloak Service Provider Interface (SPI) that bridges identity lifecycle events with the application domain.

---

## 1. Responsibilities

- Listen to identity-related events within the IAM system
- Propagate relevant identity changes
- Maintain consistency between IAM-managed identities and application users

---

## 2. Tech Stack

- Java 21
- Keycloak SPI

---

## 3. Implementation Notes

- Captures user lifecycle events such us user creation and update
- Calls configurable backend API endpoint to propagate identity changes