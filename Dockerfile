# back
# устанавливаем самую лёгкую версию JVM
FROM openjdk:17-jdk-alpine

# указываем ярлык. Например, разработчика образа и проч. Необязательный пункт.
LABEL maintainer="pemponce"

# указываем, где в нашем приложении лежит джарник
ARG JAR_FILE=build/libs/zakaz-0.0.1-SNAPSHOT.jar

# добавляем джарник в образ под именем rebounder-chain-backend.jar
ADD ${JAR_FILE} zakaz.jar

# команда запуска джарника
ENTRYPOINT ["java","-jar","/zakaz.jar"]