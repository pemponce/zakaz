# устанавливаем самую лёгкую версию JVM
FROM openjdk:17-jdk-alpine

LABEL maintainer="pemponce"

# указываем, где в нашем приложении лежит джарник
ARG JAR_FILE=build/libs/zakaz-0.0.1-SNAPSHOT.jar

# добавляем джарник в образ под именем questionbot.jar
ADD ${JAR_FILE} questionbot.jar

# команда запуска джарника
ENTRYPOINT ["java","-jar","/questionbot.jar"]
