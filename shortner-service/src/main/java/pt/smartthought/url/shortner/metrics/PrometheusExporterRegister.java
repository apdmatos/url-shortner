package pt.smartthought.url.shortner.metrics;

import io.prometheus.client.Collector;

import java.util.List;

public class PrometheusExporterRegister {

    private List<Collector> collectors;

    public PrometheusExporterRegister(List<Collector> collectors) {
        for (Collector collector : collectors) {
            try {
                collector.register();
            } catch(Exception e){}
        }
        this.collectors = collectors;
    }

    public List<Collector> getCollectors() {
        return collectors;
    }

}
