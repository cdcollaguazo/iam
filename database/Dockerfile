FROM alpine/psql:18.6

COPY ./bootstrap.sql /bootstrap.sql
COPY ./entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]