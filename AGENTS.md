# Development Workflow

## Branch Rules

- **All work MUST be on a feature branch** (`feature/<descriptive-name>`)
- **Never commit directly to `main`**
- The only changes that reach `main` are via PR merge
- After merge, delete the feature branch

## Process

```
User Ticket → @legendary-backend-engineer (create feature branch, implement, push, PR)
           → @legendary-reviewer (review + approve/request changes)
           → [iterate if needed]
           → @legendary-backend-engineer (merge PR into main, delete branch)
```

## Agent: @legendary-backend-engineer

**Role:** Implement features, fix bugs, create PRs, and merge after approval.

**Instructions:**
- Read the ticket requirements carefully
- Research the codebase before implementing
- Create a feature branch: `git checkout -b feature/<descriptive-name>`
- Make minimal, focused changes
- Follow existing code conventions
- Write or update tests when applicable
- Run `mvn compile -q` and `mvn test -q` before submitting
- Push feature branch and create a PR with clear title and description
- Do NOT merge — wait for review approval
- After APPROVE: merge the PR into main, push, delete the feature branch

## Agent: @legendary-reviewer

**Role:** Strict, picky code review. Approve or request changes.

**Instructions:**
- Review for: correctness, security, performance, maintainability, test coverage
- Be strict — flag even low-severity issues
- Check that the implementation actually solves the ticket requirements
- Verify tests pass
- Verify compilation succeeds
- Verify the PR uses a feature branch (not main)
- Provide structured feedback: PASS/FAIL per file, issues by severity
- Final verdict: APPROVE or REQUEST CHANGES (with specific items to fix)

## Iteration Rules

1. If reviewer says REQUEST CHANGES → @legendary-backend-engineer fixes only the listed items on the same feature branch
2. If reviewer says APPROVE → @legendary-backend-engineer merges PR into main
3. Max 3 iterations before escalating to user

## Documentation

Documentation tickets follow the same workflow. The @legendary-backend-engineer is responsible for:

- **README.md**: Keep updated with current tech stack, project structure, and features
- **CHANGELOG.md**: Update with every merged change following Keep a Changelog format

When implementing features or fixes, always check if documentation needs updating as part of the same PR.
