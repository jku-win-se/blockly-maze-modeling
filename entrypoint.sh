#!/bin/bash
set -e

# 1. Start Xvfb (Virtual Framebuffer)
echo "Starting Xvfb..."
Xvfb :99 -screen 0 1280x1024x24 &
sleep 2

# 2. Start x11vnc (VNC server)
echo "Starting x11vnc..."
x11vnc -display :99 -forever -nopw -listen localhost -xkb &
sleep 2

# 3. Start noVNC (Web-based VNC client)
echo "Starting noVNC..."
# Create a symbolic link so vnc.html is the default index page
ln -s /usr/share/novnc/vnc.html /usr/share/novnc/index.html || true
websockify --web /usr/share/novnc 6080 localhost:5900 &
sleep 2

# 4. Run the application
echo "Starting Blocky Maze..."
export DISPLAY=:99
export JAVA_TOOL_OPTIONS="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.xml/com.sun.org.apache.xerces.internal.jaxp=ALL-UNNAMED --add-exports=java.xml/com.sun.org.apache.xerces.internal.jaxp=ALL-UNNAMED -Djava.util.Arrays.useLegacyMergeSort=true"
export MAVEN_OPTS="$JAVA_TOOL_OPTIONS"
export CLASSPATH="/app/blocky_momot/target/classes:/app/blocky_game/target/classes"
mvn -pl blocky_game javafx:run
