@echo off
echo Building all traveler services...

echo Building parent project...
call mvn clean install -DskipTests

echo Building traveler-common...
cd traveler-common
call mvn clean install -DskipTests
cd ..

echo Building traveler-config...
cd traveler-config
call mvn clean package -DskipTests
cd ..

echo Building traveler-discovery...
cd traveler-discovery
call mvn clean package -DskipTests
cd ..

echo Building traveler-gateway...
cd traveler-gateway
call mvn clean package -DskipTests
cd ..

echo Building traveler-auth...
cd traveler-auth
call mvn clean package -DskipTests
cd ..

echo Building traveler-core...
cd traveler-core
call mvn clean package -DskipTests
cd ..

echo Building traveler-storage...
cd traveler-storage
call mvn clean package -DskipTests
cd ..

echo Building traveler-notification...
cd traveler-notification
call mvn clean package -DskipTests
cd ..

echo Building traveler-payment...
cd traveler-payment
call mvn clean package -DskipTests
cd ..

echo All services built successfully!
echo Now you can run: docker-compose up --build