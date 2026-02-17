# Durable Execution Engine (Java)

## Overview

This project implements a **Native Durable Execution Engine** in Java.

Unlike traditional programs where a crash results in complete loss of in-memory state, this engine allows workflows to:

- Resume from the exact point of failure
- Skip previously completed steps
- Persist step results in an RDBMS (SQLite)
- Support parallel step execution
- Maintain logical ordering using sequence tracking

This architecture is inspired by durable execution systems such as:

- Temporal
- Cadence
- Azure Durable Functions
- DBOS

---

## Core Concepts

### 1. Durable Workflow

A workflow is written using normal Java code (loops, conditionals, concurrency).

Side-effecting operations are wrapped in a:

```java
StepExecutor.step(...)
