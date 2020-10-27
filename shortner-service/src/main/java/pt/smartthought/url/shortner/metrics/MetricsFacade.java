package pt.smartthought.url.shortner.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetricsFacade {
    private final io.prometheus.client.Histogram requestLatency;
    private final io.prometheus.client.Counter applicationRequests;

    @Autowired
    public MetricsFacade(CollectorRegistry collectorRegistry) {
        applicationRequests = Counter.build()
                .name("request_counter")
                .labelNames("path", "method", "status")
                .help("Metrics for application requests")
                .register(collectorRegistry);

        requestLatency = Histogram.build()
                .name("application_requests_latency")
                .buckets(0.005,0.01,0.025,0.05,0.075,0.1,0.25,0.5,0.75,1,2.5,5,7.5,10)
                .labelNames("path", "method", "status")
                .help("Prometheus histogram metric for application response times")
                .register(collectorRegistry);
    }

    public void registerRequest(String path, String method, String status) {
        applicationRequests.labels(path, method, status).inc();
    }

    public void registerRequestLatency(String path, String method, String statusCode, long timeTaken) {
        requestLatency.labels(path, method, statusCode).observe(timeTaken);
    }
}
