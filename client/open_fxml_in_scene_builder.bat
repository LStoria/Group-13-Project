@echo off
rem open_fxml_in_scene_builder.bat
rem Usage: open_fxml_in_scene_builder.bat [relative-path-to-fxml]
rem If no argument given, opens src\main\resources\view\login.fxml

setlocal

rem 1) Determine script directory
set SCRIPT_DIR=%~dp0

rem 2) Default relative path if none provided
if "%1"=="" (
  set REL_PATH=src\main\resources\view\login.fxml
) else (
  set REL_PATH=%1
)

rem 3) Find SceneBuilder executable
if defined SCENEBUILDER_PATH (
  set SB=%SCENEBUILDER_PATH%
) else (
  rem try common locations
  if exist "%ProgramFiles%\SceneBuilder\SceneBuilder.exe" (
    set SB=%ProgramFiles%\SceneBuilder\SceneBuilder.exe
  ) else if exist "%ProgramFiles(x86)%\SceneBuilder\SceneBuilder.exe" (
    set SB=%ProgramFiles(x86)%\SceneBuilder\SceneBuilder.exe
  ) else if exist "%LocalAppData%\Programs\SceneBuilder\SceneBuilder.exe" (
    set SB=%LocalAppData%\Programs\SceneBuilder\SceneBuilder.exe
  ) else (
    set SB=
  )
)

if "%SB%"=="" (
  echo Scene Builder executable not found.
  echo Please install Gluon Scene Builder and/or set environment variable SCENEBUILDER_PATH to the full path of SceneBuilder.exe
  goto :EOF
)

rem 4) Resolve target path (accept absolute or relative paths)
set TARGET=%REL_PATH%
rem if TARGET doesn't exist, try relative to script dir
if not exist "%TARGET%" (
  if exist "%SCRIPT_DIR%%REL_PATH%" (
    set TARGET=%SCRIPT_DIR%%REL_PATH%
  )
)

if not exist "%TARGET%" (
  echo Could not find FXML file: %REL_PATH%
  echo Checked current working directory and %SCRIPT_DIR%
  goto :EOF
)

rem 5) Launch Scene Builder with the target FXML
echo Opening %TARGET% with Scene Builder...
start "SceneBuilder" "%SB%" "%TARGET%"

endlocal
