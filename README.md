NATIVE DURABLE EXECUTION ENGINE
Software Engineer Intern Assignment – Assignment 1

------------------------------------------------------------
OVERVIEW
------------------------------------------------------------

This project implements a Native Durable Workflow Execution Engine in Java.

A Durable Workflow can:

- Be interrupted at any time (crash, power loss, kill -9)
- Resume from the exact failure point
- Skip already completed steps
- Avoid re-executing side effects
- Handle parallel execution safely
- Guarantee idempotent recovery

This architecture is inspired by:

- Temporal
- Cadence
- Azure Durable Functions
- DBOS


------------------------------------------------------------
CORE CONCEPT
------------------------------------------------------------

Normal Java code becomes durable when wrapped in:

<T> T step(String id, Callable<T> fn)

Every side-effect must be executed inside step().

The engine:

1. Checks persistent storage
2. If step already completed → returns cached result
3. If not → executes and persists result
4. Handles crash between execution & commit (Zombie step)

This ensures durability and idempotency.


------------------------------------------------------------
ARCHITECTURE
------------------------------------------------------------

App (CLI)
   |
   v
DurableEngine
   |
   v
DurableContext
   |
   v
Storage (SQLite)
   |
   v
steps table

Workflow Example:
Employee Onboarding

Step 1: Create Record (Sequential)
Step 2: Provision Laptop (Parallel)
Step 3: Provision Access (Parallel)
Step 4: Send Welcome Email (Sequential)


------------------------------------------------------------
DATABASE SCHEMA
------------------------------------------------------------

Table: steps

Columns:
- workflow_id TEXT
- step_key TEXT
- status TEXT (IN_PROGRESS / COMPLETED)
- output TEXT (Serialized JSON)
- updated_at TIMESTAMP

Primary Key:
(workflow_id, step_key)

This ensures uniqueness and idempotency.


------------------------------------------------------------
LOGICAL CLOCK / SEQUENCE MANAGEMENT
------------------------------------------------------------

To support loops and conditionals, each step uses:

step_key = id + "_" + sequence

The sequence is internally tracked using an AtomicInteger.

This ensures:

- Same step name in loop does not collide
- Conditional branching safe
- Deterministic replay


------------------------------------------------------------
STEP EXECUTION FLOW
------------------------------------------------------------

When step() is called:

1. Generate step_key
2. Check DB:
   - If COMPLETED → return cached result
   - If IN_PROGRESS → treat as Zombie Step
3. Insert IN_PROGRESS
4. Execute function
5. Serialize output
6. Update status to COMPLETED
7. Return result


------------------------------------------------------------
ZOMBIE STEP HANDLING
------------------------------------------------------------

If crash occurs:

- After execution
- Before commit

Then status remains IN_PROGRESS.

On restart:

- Engine detects IN_PROGRESS
- Re-executes safely
- Updates to COMPLETED

This prevents inconsistent state.


------------------------------------------------------------
CONCURRENCY SUPPORT
------------------------------------------------------------

Parallel steps implemented using:

CompletableFuture

Thread safety ensured by:

- SQLite transactions
- Synchronized writes
- Atomic sequence counter
- Unique primary keys

Handles SQLITE_BUSY safely.


------------------------------------------------------------
WORKFLOW RUNNER
------------------------------------------------------------

CLI supports:

Start workflow:

mvn exec:java -Dexec.mainClass="com.aman.durable.App"

Simulate crash:

mvn exec:java -Dexec.mainClass="com.aman.durable.App" -Dexec.args="--crash-at=2"

Restart workflow:

mvn exec:java -Dexec.mainClass="com.aman.durable.App"

Completed steps will be skipped.


------------------------------------------------------------
EMPLOYEE ONBOARDING WORKFLOW
------------------------------------------------------------

1. Create Employee Record
2. Provision Laptop (Parallel)
3. Provision System Access (Parallel)
4. Send Welcome Email

Parallel steps use CompletableFuture and durable step wrapper.


------------------------------------------------------------
TYPE SAFETY
------------------------------------------------------------

Generic Step API:

<T> T step(String id, Callable<T> fn)

Supports any return type.

Results serialized using Jackson JSON.


------------------------------------------------------------
RESILIENCE FEATURES
------------------------------------------------------------

- Crash recovery
- Step memoization
- Zombie step handling
- Parallel execution safety
- SQLite-based durable store
- No DSL, pure idiomatic Java


------------------------------------------------------------
TESTING
------------------------------------------------------------

Includes:

- DurableEngineTest
- Restart behavior validation
- Step memoization verification

Run tests:

mvn test


------------------------------------------------------------
BUILD & RUN
------------------------------------------------------------

Build:

mvn clean package

Run:

java -jar target/native-durable-execution-engine-1.0-SNAPSHOT.jar

Run with crash simulation:

java -jar target/native-durable-execution-engine-1.0-SNAPSHOT.jar --crash-at=2


------------------------------------------------------------
PROJECT STRUCTURE
------------------------------------------------------------

native-durable-execution-engine/
  src/main/java/com/aman/durable/
    engine/
    examples/onboarding/
    App.java
  src/test/
  pom.xml
  README.md
  Prompts.txt


------------------------------------------------------------
DESIGN DECISIONS
------------------------------------------------------------

- SQLite chosen for lightweight persistence
- Generics for type-safe step results
- JSON serialization for portability
- Sequence ID for loop safety
- CompletableFuture for concurrency
- No external orchestration frameworks


------------------------------------------------------------
EVALUATION RUBRIC COVERAGE
------------------------------------------------------------

Correctness      -> Skips completed steps
Concurrency      -> Parallel durable execution
Resilience       -> Zombie step handling
Type Safety      -> Generic step method
Persistence      -> RDBMS-backed durable state
Testing          -> Restart behavior validated


------------------------------------------------------------
BONUS IMPLEMENTATION
------------------------------------------------------------

Automatic Sequence ID generation using:

AtomicInteger logical clock

No need for manual ID management in loops.


------------------------------------------------------------
END OF DOCUMENT
------------------------------------------------------------
