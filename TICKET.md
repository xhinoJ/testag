# Ticket: Add request/response logging interceptor

**Priority:** Medium
**Description:** Add a Spring interceptor that logs incoming requests and outgoing responses for debugging purposes.

**Requirements:**
- Log HTTP method, URI, status code, and execution time
- Use SLF4J with structured logging
- Exclude health endpoint from logging
- Add to `src/main/java/com/example/loganalyzer/config/` package

**Acceptance Criteria:**
- Requests to `/api/logs/health` are not logged
- Other `/api/logs/**` requests log method, URI, status, and duration
- Logging uses `log.info()` format
