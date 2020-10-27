package pt.smartthought.url.shortner;

import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.hotspot.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pt.smartthought.url.shortner.metrics.PrometheusExporterRegister;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Slf4j
public class Application {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", System.getenv("DEPLOYMENT_ENVIRONMENT"));
		log.info("Spring active profile: {}", System.getProperty("spring.profiles.active"));
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public PrometheusExporterRegister exporterRegister(MetricRegistry metricRegistry) {
		List<Collector> collectors = new ArrayList<>();
		collectors.add(new MemoryPoolsExports());
		collectors.add(new MemoryAllocationExports());
		collectors.add(new BufferPoolsExports());
		collectors.add(new GarbageCollectorExports());
		collectors.add(new ClassLoadingExports());
		collectors.add(new VersionInfoExports());
		collectors.add(new StandardExports());
		collectors.add(new ThreadExports());
		collectors.add(new DropwizardExports(metricRegistry));
		return new PrometheusExporterRegister(collectors);
	}

	@Bean
	public CollectorRegistry collectorRegistry(PrometheusExporterRegister prometheusExporterRegister) {
		CollectorRegistry collectorRegistry = new CollectorRegistry();
		prometheusExporterRegister.getCollectors().forEach(collectorRegistry::register);
		return collectorRegistry;
	}

	@Bean
	public MetricRegistry metricRegistry(){
		return new MetricRegistry();
	}
}
