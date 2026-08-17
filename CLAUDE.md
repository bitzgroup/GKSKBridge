# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Phase 0 (project scaffolding: Gradle Android library, ktlint/detekt, CI, Gitflow branch
protection), Phase 1 (`GKSKNodeComponent`), Phase 2 (`SKNode.entity`), Phase 3 (`GKScene`), and
Phase 4 (`GKAgentNodeComponent`, `toSKVector2`/`toGKVector2`) are complete. Phase 5 (documentation)
is not yet started. See [`docs/ROADMAP.md`](docs/ROADMAP.md) for the full phase-by-phase plan and
progress checklist.

## Intent

GKSKBridge holds the parts of Apple's GameplayKit and SpriteKit APIs that reference each other
(e.g. `GKSKNodeComponent`-style entity-component ↔ scene-graph binding, a `GKAgent`/`GKAgentDelegate`
steering an `SKNode`'s position), reimplemented for Android as an idiomatic Kotlin library.

This is the third of a family of bitzgroup projects that mirror Apple's game frameworks for
Android:

- [GameplayKit for Android](https://github.com/bitzgroup/GameplayKit) (`jp.co.bitz.gameplaykit`,
  local checkout: `../GameplayKit`) — entity-component architecture, state machines, pathfinding,
  agents/goals/behaviors, rule systems, randomization, decision trees, game model AI, spatial
  partitioning, noise.
- [SpriteKit for Android](https://github.com/bitzgroup/SpriteKit) (`jp.co.bitz.spritekit`, local
  checkout: `../SpriteKit`) — scene graph, sprites/shapes/labels, actions, 2D physics, particles,
  tile maps, camera/effects/constraints, transitions, audio, shaders.

**Why a third repo instead of folding this into one of the other two:** on Apple platforms,
GameplayKit and SpriteKit are both system frameworks, so a cross-reference between them (e.g.
`GKSKNodeComponent`) costs nothing extra to depend on. On Android, GameplayKit and SpriteKit are
separate OSS libraries that are each meant to be usable standalone — SpriteKit's own `CLAUDE.md`
explicitly states the two "are not integrated with each other." Putting bridging code inside
either library would force a project that only needs one of them to also pull in the other.
GKSKBridge exists to hold that bridging surface on its own, depended on only by projects that
actually need both.

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the full dependency-mechanism design (why
this repo carries no submodules of its own, how the project-path dependency on GameplayKit/
SpriteKit resolves both standalone and when embedded in a host app, and the CI sibling-checkout
setup) — read this before touching `settings.gradle.kts`, `gkskbridge/build.gradle.kts`, or
`.github/workflows/ci.yml`.

## Commands

All commands run from the repo root, **scoped to the `:gkskbridge` module** (`:gkskbridge:<task>`)
— running a bare `./gradlew <task>` also runs that task for the included
`:GameplayKit:gameplaykit`/`:SpriteKit:spritekit` sibling projects, and their build scripts
resolve `$rootDir`-relative paths (e.g. their own `config/detekt/detekt.yml` overrides) against
*this* repo's root once included here, not their own — so an unscoped `detekt`/`ktlintCheck` run
spuriously fails on their code with the wrong ruleset. Building standalone requires GameplayKit
and SpriteKit checked out as sibling directories first — see [`README.md`](README.md#building) and
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

- Build: `./gradlew :gkskbridge:assemble`
- Unit tests: `./gradlew :gkskbridge:testDebugUnitTest` (a single test: `./gradlew :gkskbridge:testDebugUnitTest --tests "jp.co.bitz.gkskbridge.SomeTest"`)
- Lint/format check: `./gradlew :gkskbridge:ktlintCheck` (auto-fix: `./gradlew :gkskbridge:ktlintFormat`)
- Static analysis: `./gradlew :gkskbridge:detekt`
- Full CI-equivalent check: `./gradlew :gkskbridge:ktlintCheck :gkskbridge:detekt :gkskbridge:assemble :gkskbridge:testDebugUnitTest`

If `ANDROID_HOME`/`ANDROID_SDK_ROOT` is not set in the shell, create a `local.properties` (gitignored)
with `sdk.dir=/path/to/Android/sdk`.

## Decided

- **Gradle `GROUP`**: `jp.co.bitz.gkskbridge`, matching the sibling repos' convention of
  configuring group/namespace via `gradle.properties` (`GROUP`, `VERSION_NAME`).
- **Dependency mechanism**: no submodules of its own; `gkskbridge/build.gradle.kts` depends on
  GameplayKit/SpriteKit via `project(":GameplayKit:gameplaykit")` /
  `project(":SpriteKit:spritekit")`. See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).
- **No `maven-publish` scaffold**, unlike the sibling repos: a `project(...)` dependency doesn't
  translate to a resolvable Maven coordinate, so publishing GKSKBridge to Maven would need
  GameplayKit/SpriteKit to publish stable artifacts first. Revisit if that changes.
- **Phase 4 shape** (agent-steering-to-node sync helper, no Apple precedent to mirror): a marker
  `GKComponent` (`GKAgentNodeComponent`) whose `sync()` looks up the entity's `GKAgent2D` by class
  each call rather than a fixed constructor reference, called once per frame from outside
  `GKComponent.update()` (e.g. `SKScene.didFinishUpdate()`) — follows the structure of Apple's own
  WWDC 2015 "DemoBots" sample. See [`docs/ROADMAP.md`](docs/ROADMAP.md) Phase 4.

## Project structure

- `gkskbridge/` — the library module (`jp.co.bitz.gkskbridge`), namespace/group configured via
  `gradle.properties` (`GROUP`, `VERSION_NAME`) and `gkskbridge/build.gradle.kts`.
- `gradle/libs.versions.toml` — version catalog; add new dependencies/plugins here, not as
  hardcoded version strings in build files. Keep plugin/library aliases in sync with the sibling
  repos' catalogs where they overlap (`android-library`, `kotlin-android`, `ktlint`, `detekt`,
  `junit`, `kotlin-test`, `kotlin-test-junit`) — `gkskbridge`'s build script and the included
  `gameplaykit`/`spritekit` module build scripts share one version catalog at build time.
- `config/detekt/detekt.yml` — detekt rule overrides (builds upon detekt's default ruleset).
- `docs/ARCHITECTURE.md` — the dependency-mechanism design (no submodules of its own, project-path
  dependency, CI sibling checkouts).
- `docs/ROADMAP.md` — phased implementation plan and progress checklist.
- `docs/API_COMPATIBILITY.md` — deviation log from Apple's API shape, filled in per phase as it
  lands.

## Git Branching Workflow

This repo follows a [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)-style
branching model.

| Branch | Branches from | Merges into | Naming |
|---|---|---|---|
| `main` | — | — | Always releasable. Direct pushes are blocked (branch protection); merge only from `release/*` or `hotfix/*`. Each merge is tagged with the corresponding `VERSION_NAME`. |
| `develop` | `main` | — | Integration branch for work heading to the next release. Also branch-protected. |
| `feature/<name>` | `develop` | `develop` | e.g. `feature/entity-node-sync`, `feature/agent-steering`. |
| `release/<version>` | `develop` | `main` **and** `develop` | e.g. `release/0.2.0`. Release-prep fixes only, no new features. |
| `hotfix/<name>` | `main` | `main` **and** `develop` | e.g. `hotfix/0.1.1-npe-fix`. Urgent fixes to a released `main`. |

- Every merge goes through a PR (no direct pushes to `main` or `develop`); CI
  (`ktlintCheck detekt assemble testDebugUnitTest`) must pass first.
- `release/*`/`hotfix/*` don't exist yet: all work happens on `feature/*` branches merged into
  `develop` until the first release is cut.

## Working in this repo

- **Documentation language:** all docs (README, KDoc, ARCHITECTURE, etc.) must be written in
  **English** — this is an OSS project.
- **Documentation location:** project docs beyond the root `README.md` live under `docs/`.
- **No app/demo module, ever.** This repo is meant to be embedded into host apps as a **git
  submodule** — `settings.gradle.kts`'s `:gkskbridge` module is the only thing a host app should
  need from this repo (the `:GameplayKit:gameplaykit`/`:SpriteKit:spritekit` includes in this
  repo's own `settings.gradle.kts` are standalone-dev/CI-only, see
  [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)). Adding a sample/demo Android app module would
  confuse a host app's build the same way it would for SpriteKit.
- **`.gitignore`** covers macOS `.DS_Store`, a standard Android/Gradle project (`.gradle/`,
  `build/`, `local.properties`, `*.apk`/`*.aab`, keystores, `google-services.json`, IntelliJ/
  Android Studio files), and the sibling `/GameplayKit/`/`/SpriteKit/` checkouts used for
  standalone dev/CI (never vendored into this repo).
- **Git operations:** branch per the workflow above (`feature/*` off `develop`, etc.); do not run
  `git commit` or `git push` unless explicitly requested by the user for that specific change.
