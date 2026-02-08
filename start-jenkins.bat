@echo off
REM Quick Start Jenkins on Docker Desktop
echo Starting Jenkins on Docker Desktop...
echo.

docker run -d ^
  --name jenkins-master ^
  -p 8081:8080 ^
  -p 50000:50000 ^
  -v jenkins_home:/var/jenkins_home ^
  -v /var/run/docker.sock:/var/run/docker.sock ^
  -v %CD%:/workspace ^
  --restart=unless-stopped ^
  jenkins/jenkins:lts

if errorlevel 1 (
    echo Jenkins container already exists. Starting it...
    docker start jenkins-master
)

echo.
echo Jenkins is starting...
echo This takes 2-3 minutes on first startup.
echo.
echo Access Jenkins at: http://localhost:8081
echo.
echo Getting admin password in 120 seconds...
timeout /t 120 /nobreak > nul

echo.
echo ========================================
echo Jenkins Admin Password:
docker exec jenkins-master cat /var/jenkins_home/secrets/initialAdminPassword 2>nul
if errorlevel 1 (
    echo Password not ready yet. Run this command:
    echo docker exec jenkins-master cat /var/jenkins_home/secrets/initialAdminPassword
)
echo ========================================
echo.
pause
