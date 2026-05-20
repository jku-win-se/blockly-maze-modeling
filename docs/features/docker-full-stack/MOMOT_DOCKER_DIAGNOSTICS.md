# MoMoT Full-Stack Docker Diagnostics Findings

## Issues Identified

### 1. Default Package Conflict (Module System)
**Finding:** The MoMoT runner classes (`blocky.java` and `blocky_custom.java`) were located in the default (unnamed) package.
**Impact:** Modern Java runtimes (especially when used with JavaFX/Modules) fail to derive module descriptors for JARs containing classes in the default package. This resulted in the following warning and partial failure:
`[WARNING] Unable to derive module descriptor... cause: blocky$4.class found in top-level directory (unnamed package not allowed in module)`
**Resolution:** Moved runner classes to the `blocky_momot_runner` package.

### 2. Metamodel ClassLoader Mismatch
**Finding:** The `BlockyPackage` was initialized twice in different ClassLoaders (AppClassLoader for the UI/XMI and a dynamic URLClassLoader for MoMoT).
**Impact:** Henshin rules failed to match nodes in the graph because EMF type-identity is tied to the ClassLoader. Even with identical NSURIs, the classes were seen as incompatible, causing zero matches and instant search termination.
**Resolution:** Implemented a ClassLoader bridge in `blocky_custom.java` to register the local package instance into the active `ResourceSet`.

### 3. Reflection Method Discovery Failure
**Finding:** Standard `getMethod()` calls were resolving to the base class `blocky.performSearch` instead of the mediator override `blocky_custom.performSearch`.
**Impact:** Custom initialization and bridging logic in the mediator were bypassed.
**Resolution:** Hardened method discovery logic in `MomotRunService.java` to exhaustively scan the hierarchy using `getDeclaredMethods()`.

### 4. Log Redirection & Buffering
**Finding:** Redirection of `System.out` to the UI console was causing logs to be swallowed or delayed in the Docker environment.
**Impact:** No diagnostics were visible in `docker compose logs`.
**Resolution:** Implemented a `MirroringOutputStream` with forced flushing to standard error to ensure real-time visibility in container logs.
