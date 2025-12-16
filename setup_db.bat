@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

echo.
echo ==========================================================
echo       SmartEnergyDB Deployment Tool v2.5 (Stable)
echo ==========================================================
echo.

:: ------------------------------------------------------------
:: 配置区域
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
:: [1/4] 清理旧环境 (只删除，不创建)
:: ------------------------------------------------------------
echo [1/4] Cleaning up old environment...

:: 只执行 DROP 操作
sqlcmd -S "%ServerName%" -E -Q "IF EXISTS (SELECT name FROM sys.databases WHERE name = N'%DB_NAME%') DROP DATABASE [%DB_NAME%]"

if !errorlevel! neq 0 (
    echo.
    echo [ERROR] Failed to drop old database.
    echo Possible reason: Database is in use. Close IDEA/Navicat.
    goto Error
)
echo    - Old database dropped if it existed.

:: ------------------------------------------------------------
:: [2/4] 执行初始化脚本 (由你的 SQL 文件负责建库)
:: ------------------------------------------------------------
if exist "sql\Init\01.sql" (
    echo.
    :: 【修复点】去掉了这里的括号，改为破折号，防止语法崩溃
    echo [2/4] Running Init Script - Creating Database...

    :: 此时不指定 -d 数据库名，因为还没创建
    sqlcmd -S "%ServerName%" -E -i "sql\Init\01.sql"

    if !errorlevel! neq 0 goto Error
) else (
    echo [ERROR] sql\Init\01.sql not found! Cannot create database.
    goto Error
)

:: ------------------------------------------------------------
:: [3/4] 创建表结构 (Table)
:: ------------------------------------------------------------
echo.
echo [3/4] Creating Tables...

if exist "sql\Table" (
    for /f "delims=" %%f in ('dir /b /on "sql\Table\*.sql"') do (
        echo    - Executing: sql\Table\%%f
        sqlcmd -S "%ServerName%" -E -d "%DB_NAME%" -i "sql\Table\%%f"
        if !errorlevel! neq 0 goto Error
    )
) else (
    echo    [WARNING] sql\Table directory not found.
)

:: ------------------------------------------------------------
:: [4/4] 导入种子数据 (SeedData)
:: ------------------------------------------------------------
echo.
echo [4/4] Importing Seed Data...

if exist "sql\SeedData" (
    for /f "delims=" %%f in ('dir /b /on "sql\SeedData\*.sql"') do (
        echo    - Executing: sql\SeedData\%%f
        sqlcmd -S "%ServerName%" -E -d "%DB_NAME%" -i "sql\SeedData\%%f"
        if !errorlevel! neq 0 goto Error
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
echo    Please check the error message above.
echo ==========================================================
pause
exit /b 1