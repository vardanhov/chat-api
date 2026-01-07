# Chat API (WebSocket Backend)

Backend application that provides a **WebSocket-based API** for managing chat messages with **strict ordering** and **optimistic versioning**.

---

## Tech Stack

- Java 25 (runtime)
- Spring Boot 4
- WebSockets
- PostgreSQL
- GraalVM Native Image (optional)
- Docker / Docker Compose

---

## Prerequisites

### Required
- **JDK 25**
- **Maven 3.9+**
- **Docker Desktop**
- **Docker Compose v2**

### Optional (for native image)
- **Linux or WSL2 (Ubuntu recommended)**
- **GraalVM 22.3+ with native-image**


---

## Database Schema

```sql
CREATE TABLE message (
    id               UUID    NOT NULL PRIMARY KEY,
    user_id          UUID    NOT NULL,
    chat_id          UUID    NOT NULL,
    message_chat_n   INTEGER NOT NULL,
    version          INTEGER NOT NULL,
    payload          VARCHAR NOT NULL
);

CREATE UNIQUE INDEX message_ux1
    ON message (chat_id ASC, message_chat_n DESC, version DESC);
