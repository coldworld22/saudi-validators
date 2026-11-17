# 1. Zero runtime dependencies

Date: 2026-11-17 · Status: accepted

## Context

Validation code ends up everywhere: Spring services, batch jobs, Android apps, Odoo connectors
written in Java. Every transitive dependency a small library brings is a version conflict
waiting to happen in someone else's build.

## Decision

The core package (`io.github.coldworld22.saudi`) uses only the JDK. Bean Validation support uses
`jakarta.validation-api` with `provided` scope: applications that use the annotations already
have it, and applications that don't never see it.

The minimum Java version is 17, the oldest LTS still common in Saudi enterprise and government
projects, so records can be used for value types.

## Consequences

- No regex or IBAN library; the mod-97 and Luhn checks are written here and tested directly.
- Bank names are a small hard-coded map, not a data file that needs updating.
