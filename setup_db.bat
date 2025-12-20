@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

echo.
echo ==========================================================
echo       SmartEnergyDB Deployment Tool v2.7 (Clean)
echo ==========================================================
echo.

:: ------------------------------------------------------------
:: Configuration
:: ------------------------------------------------------------
set DB_NAME=AcademicSubmissionSystem

echo Default Server is [localhost].
set /p ServerName=Enter Server Name (Press Enter for localhost):
if "%ServerName%"=="" set ServerName=localhost

echo.
echo ----------------------------------------------------------
echo Target Server:   %ServerName%
echo Target Database: %DB_NAME%
echo ----------------------------------------------------------
echo.

:: ------------------------------------------------------------
:: [1/4] Clean up old environment
:: ------------------------------------------------------------
echo [1/4] Cleaning up old environment...

sqlcmd -S "%ServerName%" -E -b -Q "IF EXISTS (SELECT name FROM sys.databases WHERE name = N'%DB_NAME%') DROP DATABASE [%DB_NAME%]"

if !errorlevel! neq 0 (
    echo.
    echo [ERROR] Failed to drop old database. Is it in use?
    goto Error
)
echo    - Old database dropped.

:: ------------------------------------------------------------
:: [2/4] Init Script (Create Database)
:: ------------------------------------------------------------
if exist "sql\Init\01.sql" (
    echo.
    echo [2/4] Creating Database...

    sqlcmd -S "%ServerName%" -E -b -i "sql\Init\01.sql"

    if !errorlevel! neq 0 (
        echo [ERROR] Failed to execute Inijavat script.
        goto Error
    )
) else (
    echo [ERROR] sql\Init\01.sql not found!
    goto Error
)

:: ------------------------------------------------------------
:: [3/4] Create Tables
:: ------------------------------------------------------------
echo.
echo [3/4] Creating Tables...

if exist "sql\Table" (
    for /f "delims=" %%f in ('dir /b /on "sql\Table\*.sql"') do (
        echo    - Executing: sql\Table\%%f
        sqlcmd -S "%ServerName%" -E -b -d "%DB_NAME%" -i "sql\Table\%%f"
        if !errorlevel! neq 0 (
            echo    [ERROR] Failed to create table in %%f
            goto Error
        )
    )
) else (
    echo    [WARNING] sql\Table directory not found.
)

:: ------------------------------------------------------------
:: [4/4] Import Seed Data
:: ------------------------------------------------------------
echo.
echo [4/4] Importing Seed Data...

if exist "sql\SeedData" (
    for /f "delims=" %%f in ('dir /b /on "sql\SeedData\*.sql"') do (
        echo    - Executing: sql\SeedData\%%f

        :: Do NOT put comments inside the loop block to avoid syntax errors
        sqlcmd -S "%ServerName%" -E -b -d "%DB_NAME%" -i "sql\SeedData\%%f"

        if !errorlevel! neq 0 (
            echo.
            echo    ======================================================
            echo    [ERROR] Data Insert Failed in file: %%f
            echo    ======================================================
            goto Error
        )
    )
) else (
    echo    [INFO] No SeedData found. Skipping.
)

echo.
echo ==========================================================
echo    SUCCESS! Database deployed successfully.
echo ==========================================================
pause
exit

:Error
echo.
echo ==========================================================
echo    [ERROR] Script Failed!
echo ==========================================================
pause
exit /b 1