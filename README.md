Durable Execution Engine (Java)
Overview

This project implements a Native Durable Execution Engine in Java.

Unlike traditional programs where a crash results in complete loss of in-memory state, this engine allows workflows to:

Resume from the exact point of failure

Skip previously completed steps

Persist step results in an RDBMS (SQLite)

Support parallel step execution

Maintain logical ordering using sequence tracking

This architecture is inspired by durable execution systems such as:

Temporal

Cadence

Azure Durable Functions

DBOS

The workflow is written in normal, idiomatic Java code without any DSL, XML, or external orchestrator.

Core Architecture
1. Durable Workflow

A workflow is written using normal Java constructs:

Loops

Conditionals

Concurrency (CompletableFuture)

Side-effecting operations must be wrapped inside:

StepExecutor.step(...)


This converts regular code into durable code.

2. Step Primitive
Signature
public static <T> T step(
    DurableContext ctx,
    String id,
    Callable<T> fn,
    Class<T> type
)

What It Does

Generates a unique step key using:

Step ID

Logical sequence number

Checks persistent storage (SQLite):

If already completed → return cached result

If not → execute function

Stores:

Status (RUNNING / COMPLETED)

Serialized output (JSON)

Returns result

This ensures idempotent execution.

3. Logical Sequence Tracking

To support loops and conditionals, each step call increments an internal counter:

step_key = stepId + "-" + sequenceNumber


Example:

create-record-1
provision-laptop-2
provision-access-3
send-email-4


This prevents collisions even if:

Step IDs repeat

Steps are inside loops

Steps execute in parallel

Sequence generation uses AtomicInteger for thread safety.

Persistence Layer

SQLite is used as the durable state store.

Table Schema
CREATE TABLE steps (
    workflow_id TEXT,
    step_key TEXT,
    status TEXT,
    output TEXT,
    PRIMARY KEY(workflow_id, step_key)
);

Stored Fields
Field	Purpose
workflow_id	Identifies workflow instance
step_key	Unique step identifier
status	RUNNING / COMPLETED
output	JSON serialized result
SQLite Configuration

To support concurrency:

PRAGMA journal_mode=WAL;
PRAGMA busy_timeout=5000;


This avoids SQLITE_BUSY errors during parallel writes.

Concurrency Support

Parallel steps are implemented using:

CompletableFuture


Thread safety is ensured by:

Atomic sequence generation

Synchronized DB writes

WAL mode

Busy timeout configuration

Zombie Step Handling
Problem

If crash occurs:

After marking step RUNNING

Before marking it COMPLETED

The step may remain in inconsistent state.

Solution

Only steps with status = COMPLETED are reused.

RUNNING steps are treated as incomplete.

On restart, incomplete steps are safely re-executed.

This guarantees correctness even during crash between execution and commit.

Example Workflow – Employee Onboarding

Located in:

examples/onboarding/OnboardingWorkflow.java

Steps:

Create Employee Record (Sequential)

Provision Laptop (Parallel)

Provision Access (Parallel)

Send Welcome Email (Sequential)

Parallel execution uses:

CompletableFuture.allOf(...)

Project Structure
src/
 ├── engine/
 │      Database.java
 │      DurableContext.java
 │      StepExecutor.java
 │      StepRepository.java
 │
 ├── examples/
 │      onboarding/
 │          OnboardingWorkflow.java
 │
 └── App.java

Build Instructions
Compile & Package
mvn clean package


This generates a fat jar in:

target/durable-engine-1.0-SNAPSHOT.jar

Running the Application
Normal Run
java -jar target/durable-engine-1.0-SNAPSHOT.jar

Crash Simulation

You can simulate crash after specific steps.

Crash After Step 1
java -jar target/durable-engine-1.0-SNAPSHOT.jar 1


Application exits intentionally.

Then resume:

java -jar target/durable-engine-1.0-SNAPSHOT.jar


Output will show:

[SKIPPED] create-record-1
[EXECUTED] provision-laptop-2
[EXECUTED] provision-access-3
[EXECUTED] send-email-4


This proves partial resume capability.

Proof of Durability
First Run
[EXECUTED] create-record-1
[EXECUTED] provision-laptop-2
[EXECUTED] provision-access-3
[EXECUTED] send-email-4

Second Run
[SKIPPED] create-record-1
[SKIPPED] provision-laptop-2
[SKIPPED] provision-access-3
[SKIPPED] send-email-4


Steps are not re-executed.
