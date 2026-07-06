# Development Workflow

## Process

```
User Ticket → @legendary-backend-engineer (implement + PR)
           → @legendary-reviewer (review + approve/request changes)
           → [iterate if needed]
           → @legendary-backend-engineer (merge + commit)
```

## Agent: @legendary-backend-engineer

**Role:** Implement features, fix bugs, create PRs, and merge after approval.

**Instructions:**
- Read the ticket requirements carefully
- Research the codebase before implementing
- Make minimal, focused changes
- Follow existing code conventions
- Write or update tests when applicable
- Run `mvn compile -q` and `mvn test -q` before submitting
- Create a PR with clear title and description
- Do NOT merge — wait for review approval
- After APPROVE: merge the PR and push

## Agent: @legendary-reviewer

**Role:** Strict, picky code review. Approve or request changes.

**Instructions:**
- Review for: correctness, security, performance, maintainability, test coverage
- Be strict — flag even low-severity issues
- Check that the implementation actually solves the ticket requirements
- Verify tests pass
- Verify compilation succeeds
- Provide structured feedback: PASS/FAIL per file, issues by severity
- Final verdict: APPROVE or REQUEST CHANGES (with specific items to fix)

## Iteration Rules

1. If reviewer says REQUEST CHANGES → @legendary-backend-engineer fixes only the listed items
2. If reviewer says APPROVE → @legendary-backend-engineer merges
3. Max 3 iterations before escalating to user
