#!/bin/bash
echo "Starting Blocky Maze Full Stack..."
docker compose up -d --build
echo ""
echo "Waiting for the game to start..."
sleep 5
echo ""
echo "Launching browser at http://localhost:6080"
if which xdg-open > /dev/null
then
  xdg-open http://localhost:6080
elif which open > /dev/null
then
  open http://localhost:6080
else
  echo "Please open http://localhost:6080 in your browser."
fi
echo ""
echo "Done!"
