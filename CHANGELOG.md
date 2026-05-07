# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Real-time Prometheus monitoring via Graphite protocol.
- Docker Compose for local development (Mock Server, Prometheus, Graphite Exporter).
- `scalafmt` for automatic code formatting.
- `MUnit` for unit testing support logic.
- `GEMINI.md` for project instructions and architecture documentation.

### Changed
- Upgraded Gatling to 3.15.0 and `gatling-sbt` to 4.18.1.
- Enhanced `gatling.conf` with production-ready tuning (DNS, connection pooling).
- Excluded IDE metadata (`.idea/`) from repository.
- Updated `README.md` with new features and quickstart instructions.
