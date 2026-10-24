# Spec Kit automation scripts

This project uses the **Python** script variant for cross-platform agent
workflows (Windows and macOS/Linux).

Invoke from the repository root:

```text
python .specify/scripts/python/create_new_feature.py --json --short-name NAME "description"
python .specify/scripts/python/setup_plan.py --json
python .specify/scripts/python/setup_tasks.py --json
python .specify/scripts/python/check_prerequisites.py --json
```

The older `bash/` copies remain for reference or POSIX-only tooling. Agent
skills under `.claude/skills/speckit-*` call the Python entry points above.
`init-options.json` has `"script": "py"`.

Upstream: https://github.com/github/spec-kit (`scripts/python/`).
