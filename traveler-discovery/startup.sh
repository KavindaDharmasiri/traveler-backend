#!/bin/sh
# Wait for config server to be ready
echo "Waiting for config server..."
until wget --spider -q http://traveler-config:8888/actuator/health; do
  echo "Config server not ready, waiting..."
  sleep 3
done

echo "Config server is ready, starting discovery service..."
exec java -jar app.jar
