# Security audits

Independent security audits of `react-native-nitro-google-signin`. These artifacts are **not shipped to npm** (see `.npmignore`).

| Run | Version | Date | Summary |
|-----|---------|------|---------|
| [run-2](./run-2/REPORT.md) | 1.0.1 | 2026-07-11 | 0 CRITICAL, 0 HIGH, 6 MEDIUM, 4 LOW; 3 fixes since run-1 |
| [run-1](./run-1/REPORT.md) | 0.7.2 | 2026-06-24 | 0 CRITICAL, 0 HIGH, 4 MEDIUM, 5 LOW |

## Latest (run-2)

**Overall risk:** Low-to-medium. No remote unauthenticated attacks against the library itself. Exploitable findings require compromised in-app JavaScript, physical access to an unlocked device, or a colliding iOS URL scheme.

**Fixed since run-1:** Android auth race (RNGS-003), Android signOut no-op (RNGS-006), iOS `offlineAccess` ignored (RNGS-009).

**Active MEDIUM findings:** Unpinned/re-callable `configure()`, unrestricted scope escalation, Android `requestScopes()` session guard gap, silent token retrieval, Android `revokeAccess()` forced logout.

## Artifacts per run

| File | Purpose |
|------|---------|
| `REPORT.md` | Human-readable report |
| `FINDINGS-DETAIL.md` | Data flows for MEDIUM+ findings |
| `findings.json` | Machine-readable structured output |
| `architecture.md` | Trust boundaries and hunt scope |
| [`REMEDIATION-PHASES.md`](./REMEDIATION-PHASES.md) | Phased plan to fix findings (maintainers + consumers) |

## Reporting new issues

See [SECURITY.md](../SECURITY.md) for vulnerability disclosure. Audit findings here are **documented known behavior and hardening gaps**, not GitHub Security Advisories unless independently confirmed as exploitable defects requiring a patch.

## Re-running audits

Audits are run with the [security-audit](https://skills.sh) agent skill. Coverage improves with additional runs after major version bumps or native API changes.
