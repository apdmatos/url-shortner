FROM prom/prometheus

COPY conf/*.yml /etc/prometheus/

EXPOSE 9090

RUN promtool check config /etc/prometheus/*.yml