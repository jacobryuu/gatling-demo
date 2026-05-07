# Gatling Demo Project Instructions

## Architecture & Conventions

- **DSL Support**: All common scenarios, chains, and protocols should reside in `src/test/scala/support/`.
- **Environment Driven**: Favor `support.Config` for all parameters. Do not hardcode URLs, user counts, or durations in Simulation classes.
- **SLO Verification**: Every simulation must apply `support.Slo.all` assertions to ensure CI pass/fail consistency.
- **Formatting**: Maintain clean Scala code. (Planned: scalafmt integration).
- **Security**: Never commit real credentials to `src/test/resources/feeders/`. Use environment variables or CI secrets to inject sensitive data.

## Workflow

- **Local Verification**: Use `ci/mock_server.py` for smoke testing during development.
- **CI Integration**: The GitHub Action workflow is the source of truth for "official" performance runs.
- **Version Management**: Gatling is currently at **3.15.0** and `gatling-sbt` at **4.18.1**.

## Key Files

- `src/test/scala/support/Config.scala`: The central configuration registry.
- `src/test/scala/support/LoadProfiles.scala`: Factory for all load injection models.
- `src/test/scala/support/Journeys.scala`: High-level business logic chains.
