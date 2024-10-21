FROM amazoncorretto:17 AS build
COPY default /default

FROM amazoncorretto:17 AS runtime
COPY --from=build /default /default


WORKDIR /default

ENV LC_ALL=C.UTF-8

ENV TZ=Europe/Moscow

RUN chmod +x /default/run.sh
ENTRYPOINT ["/default/run.sh"]