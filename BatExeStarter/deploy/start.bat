@echo off

CALL constants.bat


plink -pw %PASSWORD% %HOST% -P 22 -m script/start.sh
echo приложение запущено
pause
