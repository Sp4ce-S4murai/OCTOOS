@echo off
chcp 65001 >nul
title Publicar OctoOS no GitHub
cd /d "%~dp0"
echo.
echo === Publicar OctoOS ===
set "GIT=git"
where git >nul 2>&1
if errorlevel 1 (
  for /d %%D in ("%LOCALAPPDATA%\GitHubDesktop\app-*") do set "GHD=%%D"
  set "GIT=%GHD%\resources\app\git\cmd\git.exe"
)
echo Usando: %GIT%
"%GIT%" log --oneline -3
echo.
"%GIT%" push origin main
if errorlevel 1 (
  echo.
  echo FALHOU o push. Copie a mensagem acima e me envie.
  pause
  exit /b 1
)
echo.
echo Push concluido com sucesso.
pause
