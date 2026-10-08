---
name: ai-pr-review
description: Automated audit of code changes before opening a Pull Request against this repo (conjur-credentials-plugin, the Jenkins HPI plugin for CyberArk Conjur secret retrieval + the DisCo Discovery Pipeline, Java 11/Maven). Analyzes the diff against the base branch, checks compliance with CyberArk standards (architecture, security, quality) as documented in this repo's own CLAUDE.md, and generates a PR_REVIEW_REPORT.md ready to paste into the PR description. Use before every commit that finalizes a task, or when the user writes "@ai-pr-review run analysis and generate report".
---

# AI PR Review

Automated audit of code changes before opening a Pull Request. The skill analyzes the diff, checks compliance with the CyberArk conventions used in this repo (`conjur-credentials-plugin`), and generates a report ready to paste into the PR description.

## Workflow

### 1. Context Extraction

- Run `git diff` against the base branch (usually `main`).
- Identify the changed components under `src/main/java/org/conjur/jenkins/`:
  - `api/` — OkHttp3 client helpers (`ConjurAPI`, `ConjurAPIUtils`)
  - `authenticator/` — APIKey vs JWT authenticator strategies
  - `configuration/` — `GlobalConjurConfiguration`, `FolderConjurConfiguration`, `ConjurJITJobProperty`
  - `conjursecrets/` — credential type implementations + bindings
  - `credentials/` — `ConjurCredentialProvider`, `ConjurCredentialStore`
  - `jwtauth/` — the `/jwtauth/conjur-jwk-set` JWKS endpoint
  - `disco/` — the DisCo Discovery Pipeline (`config/`, `model/`, `discovery/`, `security/`, `export/`)

### 2. Multi-Persona Analysis

- **Architect**:
  - HTTP calls must use **OkHttp3** (`ConjurAPIUtils.getHttpClient`) — flag any new use of `java.net.http.HttpClient`.
  - Jenkins credential lookups must use `ACL.SYSTEM2` with `lookupCredentialsInItemGroup`/`lookupCredentialsInItem` — flag any use of the deprecated `lookupCredentials(...)` + `ACL.SYSTEM` pattern.
  - Configuration persistence must follow the existing `GlobalConfiguration` + `@DataBoundSetter` + `load()`/`save()` (XStream) pattern (see `GlobalConjurConfiguration`, `DiscoExporterConfiguration`).
  - DisCo pipeline changes must respect the singleton `volatile boolean isRunning` guard and the 1-hour manual-trigger rate limit (bypassed only when `testEnvironment=true`); scheduled/CRON triggers bypass the manual rate-limit check by design — don't "fix" that as if it were a bug.
  - `subdomain` empty must hard-abort DisCo runs before any network call — flag any change that removes or weakens this guard.
- **Security**:
  - Sensitive values must use `hudson.util.Secret`; **never** log `.getPlainText()` — flag any log statement, exception message, or toString that could leak a secret value.
  - No hardcoded Conjur API keys, JWTs, or DisCo bearer tokens committed.
  - Respect the credential export mode matrix (`exportSecretValues` × `storeProvider` → fields-only vs fields+encrypted-values) — flag any DisCo export path that returns values when it shouldn't per that table.
  - JSON serialization in the DisCo pipeline uses **Gson 2.8.9** — flag inconsistent use of Jackson in new DisCo code without a reason.
- **Quality**:
  - New/changed functionality needs corresponding tests in `src/test/java/org/conjur/jenkins/` using **JUnit 4.13.2 + Mockito 3.11.2 + AssertJ 3.15.0**.
  - DisCo tests must not start a real Jenkins instance — follow the existing stub/subclass pattern (`DiscoExporterConfigurationTest`'s stub subclass, `DiscoveryOrchestratorTest`'s `TestableOrchestrator`) rather than adding `JenkinsRule`-based tests to the `disco/` package.
  - New DisCo pipeline events should follow the structured `DISC_XXX` logging convention (currently `DISC_001`–`DISC_010`) via `java.util.logging.Logger`, not ad-hoc log strings.

### 3. Standard Compliance

- Java baseline is 11; Jenkins core baseline is 2.462.3; plugin version is tracked in `CHANGELOG.md`/`pom.xml` (currently 3.1.1) — flag unexplained baseline bumps.
- DisCo payload structure must stay compatible with `DISCO_PAYLOAD_CONTRACT.md` — any field addition/removal/rename in `DiscoverySnapshot`/`CredentialRecord`/`FolderRecord`/`JobRecord` must be cross-checked against that contract doc.
- This repo does **not** own: ingestion/SMS-template mapping, Conjur policy generation from DisCo annotations, TTL management for exported secrets, or regional API endpoint definitions (those come dynamically from `DiscoveryServiceClient`) — flag any PR that tries to implement these here instead of consuming them from the platform.
- `.gitleaks.toml` governs secret-scanning exceptions — don't broaden exclusions without justification.

### 4. Report Generation

Generate `PR_REVIEW_REPORT.md` **fully filled in** — never leave a `<placeholder>` in the final file. Derive each field automatically:

- **One-line summary**: one imperative-mood sentence describing what the diff does.
- **Desired Outcome**: if the branch name or commit messages reference a ticket/issue, pull context from it (e.g. `gh issue view <n>` for a `#<n>` reference); otherwise infer the motivating problem directly from what the diff changes.
- **Implemented Changes**: build this directly from the actual `git diff` and the Multi-Persona Analysis findings (step 2) — what changed, why, and how a reviewer should verify it (manual/integration steps if any). State screenshots as "N/A" unless the diff touches UI.
- **Connected Issue/Story**: scan the current branch name and the commit messages on this branch for a ticket pattern like `[A-Z]+-\d+` (e.g. `CP-26179`, `CNJR-1234`) and use the first match as the "CyberArk internal issue ID". If a GitHub issue number is referenced (`#123` in a branch name or commit message), use it for "Resolves #123" — otherwise omit that line entirely rather than leaving a placeholder.
- **Definition of Done** — tick exactly one box per subsection based on what the diff actually contains, never by default:
  - *Changelog*: tick "CHANGELOG has been updated" only if `CHANGELOG.md` (or equivalent) is part of the diff; tick "does not require a CHANGELOG update" only if the change has no user-facing effect.
  - *Test coverage*: tick "includes new tests" only if test files changed in the diff; tick "does not require tests" only if the change is non-functional (docs, config, tooling, skills).
  - *Documentation*: tick "docs were updated" only if README/docs files changed; tick "does not require updating documentation" only if nothing user-facing changed.
  - *Behavior*: tick "no behavior was changed" only if the diff is limited to non-runtime files (skills, docs, CI config, tests); otherwise tick one of the other two options and explain why.
  - *Security*: tick "no security aspects" only if the Security persona (step 2) genuinely found nothing; otherwise tick the appropriate other option and reference the finding.

If a field genuinely cannot be determined, write a short explicit note (e.g. "No linked issue found in branch/commit history") instead of leaving an angle-bracket placeholder.

The output must follow this structure when pasted into a PR description:

```markdown
<filled summary>

### Desired Outcome

<filled>

### Implemented Changes

- <filled bullets>

### Connected Issue/Story

Resolves #<n>  (omit this line if none found)

CyberArk internal issue ID: <filled or note>

### Definition of Done

#### Changelog
- [ ] The CHANGELOG has been updated, or
- [ ] This PR does not include user-facing changes and doesn't require a CHANGELOG update

#### Test coverage
- [ ] This PR includes new unit and integration tests to go with the code changes, or
- [ ] The changes in this PR do not require tests

#### Documentation
- [ ] Docs (e.g. READMEs) were updated in this PR
- [ ] A follow-up issue to update official docs has been filed here: [insert issue ID]
- [ ] This PR does not require updating any documentation

#### Behavior
- [ ] This PR changes product behavior and has been reviewed by a PO, or
- [ ] These changes are part of a larger initiative that will be reviewed later, or
- [ ] No behavior was changed with this PR

#### Security
- [ ] Security architect has reviewed the changes in this PR,
- [ ] These changes are part of a larger initiative with a separate security review, or
- [ ] There are no security aspects to these changes
```

(the checkboxes above are the template shape — in the actual generated report, exactly one box per subsection must be ticked `[x]`, the rest `[ ]`)

## Usage Instructions

Run this command before every commit that finalizes a task:

```
@ai-pr-review run analysis and generate report
```

## Port.io Metadata

- **Maturity Level**: Tier 2 (Repeatable Skill)
- **Service**: Secrets Manager Integrations
