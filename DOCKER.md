# Docker Setup for Blocky Maze

This environment allows running the Blocky Maze application and its MOMoT synthesis backend within Docker containers.

## GUI Display Configuration

### Windows (VcXsrv)
1. Install [VcXsrv](https://sourceforge.net/projects/vcxsrv/).
2. Launch **XLaunch** with the following settings:
   - **Display settings**: "Multiple windows"
   - **Client startup**: "Start no client"
   - **Extra settings**: Check **"Disable access control"** (crucial for container connections).
3. Identify your host machine's IP address (e.g., using `ipconfig` in CMD).
4. Set the `DISPLAY` environment variable on your host:
   ```cmd
   set DISPLAY=<YOUR_HOST_IP>:0.0
   ```
   Or use `host.docker.internal:0.0` if using Docker Desktop (already defaulted in `docker-compose.yml`).
5. Run the application:
   ```bash
   docker-compose up
   ```

### Linux (X11)
1. Grant local Docker containers access to your X server:
   ```bash
   xhost +local:docker
   ```
2. The `docker-compose.yml` is configured to use the host's X11 socket via `/tmp/.X11-unix`.
3. Run the application:
   ```bash
   docker-compose up
   ```

### macOS (XQuartz)
1. Install [XQuartz](https://www.xquartz.org/).
2. In XQuartz settings, go to the **Security** tab and check **"Allow connections from network clients"**.
3. Restart XQuartz.
4. Run `xhost +$(hostname)` or `xhost +localhost`.
5. Set `DISPLAY` to `host.docker.internal:0`.
6. Run `docker-compose up`.

## Persistence
- `blocky_game/load.xmi` and `blocky_game/save.xmi` are mounted as volumes. Changes made within the container will persist on your host machine.

## MOMoT Synthesis in Docker

You can run the MOMoT synthesis backend within the same container environment.

1. Ensure the container is built:
   ```bash
   docker-compose build
   ```
2. Run a synthesis task:
   ```bash
   docker-compose run -e MAVEN_OPTS="--add-opens java.base/java.util=ALL-UNNAMED -Djava.util.Arrays.useLegacyMergeSort=true" blocky-maze-app mvn -pl blocky_momot exec:java -Dexec.mainClass="blocky" -Dblocky.input="blocky_momot/load.xmi" -Dblocky.henshin="blocky_model/transformations/statement_insertions_henshin_text.henshin"
   ```
   (Adjust `blocky.input` and other properties as needed).
