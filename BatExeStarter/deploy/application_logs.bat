@echo off

CALL constants.bat

rem Выполнение команд на сервере
plink -pw %PASSWORD% %HOST% -P 22 -m script/logs.sh

pause