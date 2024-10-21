@echo off
CALL constants.bat

echo HOST: %HOST%
echo PASSWORD: %PASSWORD%
echo JAR_PATH: %JAR_PATH%
echo XYU

rem Выполнение команд на сервере
plink -pw %PASSWORD% %HOST% -P 22 -m script/create.sh
echo XYU

rem Передача JAR-файла на сервер
scp -r %JAR_PATH% %HOST%:~/
echo JAR file transferred.
echo XYU

rem Выполнение команд на сервере
plink -pw %PASSWORD% %HOST% -P 22 -m script/commands.sh
echo XYU
echo negr
pause
