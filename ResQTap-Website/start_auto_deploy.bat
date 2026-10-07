@echo off
title ResQTap Auto-Deploy Watcher
cd /d "%~dp0"
echo ========================================================
echo Memulakan ResQTap Auto-Deploy Watcher...
echo ========================================================
node auto_deploy_watcher.js
pause
