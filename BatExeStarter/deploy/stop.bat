@echo off

CALL constants.bat


plink -pw %PASSWORD% %HOST% -P 22 -m script/docker_stop.sh
echo приложение остановлено
pause