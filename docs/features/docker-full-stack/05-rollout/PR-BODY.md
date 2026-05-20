# PR Description — Docker Full Stack Environment

## Summary
This PR introduces a comprehensive Docker-based environment for the Blocky Maze application. It enables developers to build and run the JavaFX GUI and the search-based MOMoT synthesis backend within a containerized Ubuntu environment, ensuring consistency across different host operating systems.

## Key Changes
- **Dockerfile**: Implements a multi-stage build using Maven and OpenJDK 21. Includes native dependencies for JavaFX (GTK, Mesa) and integrates MOMoT 2.0 libraries (MOMoT Core, MOEA, OCL, Nashorn).
- **docker-compose.yml**: Orchestrates the application container with volume mounts for model persistence (`load.xmi`, `save.xmi`) and X11 socket forwarding.
- **blocky_momot/pom.xml**: Now supports standalone Maven compilation and execution outside of Eclipse, enabling headless synthesis runs in Docker.
- **DOCKER.md**: Comprehensive guide for setting up X server forwarding on Windows (VcXsrv), Linux (X11), and macOS (XQuartz).
- **Automation**: Pre-configures JVM arguments (`--add-opens`, `useLegacyMergeSort`) for stable MOMoT execution on Java 17+.

## Verification Results
- **App Startup**: Confirmed logic-level startup and dependency resolution in container logs.
- **Synthesis Task**: Successfully executed a 10-seed NSGA-II search task within the container, with output models persisted to the host.
- **Cross-Platform**: Designed to support all major host OSs via standard X11 forwarding patterns.

## Instructions
1. Build: `docker-compose build`
2. Run GUI: `docker-compose up`
3. Run Synthesis: `docker-compose run blocky-maze-app mvn -pl blocky_momot exec:java ...` (see DOCKER.md for full command).
