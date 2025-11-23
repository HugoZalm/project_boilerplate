ARG LIQUIBASE_VERSION
FROM liquibase/liquibase:${LIQUIBASE_VERSION}
USER root
COPY changelog /liquibase/changelog
COPY liquibase.properties /liquibase/liquibase.properties
RUN chmod -R 755 /liquibase
USER liquibase
CMD ["--defaults-file=/liquibase/liquibase.properties", "update"]
