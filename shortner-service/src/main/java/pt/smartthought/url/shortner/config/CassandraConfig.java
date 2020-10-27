package pt.smartthought.url.shortner.config;

import com.datastax.driver.core.ConsistencyLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.sky.cqlmigrate.CassandraLockConfig;
import uk.sky.cqlmigrate.CqlMigrator;
import uk.sky.cqlmigrate.CqlMigratorFactory;

import java.time.Duration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "cassandra")
public class CassandraConfig {

    private boolean migrationEnabled;

    private String contactPoints;

    private Integer port;

    private String username;

    private String password;

    private String keyspace;

    private String consistencyLevel;

    private String localDc;

    private Integer constantReconnectionDelayMs;

    private Integer connectionTimeoutMs;

    private Integer socketReadTimeoutMs;

    private Integer poolWaitTimeoutMs;

    private Integer poolMinConnections;

    private Integer poolMaxConnections;

    private LocksConfiguration locking;

    private String schemaPath;

    public ConsistencyLevel getConsistencyLevel() {
        return ConsistencyLevel.valueOf(this.consistencyLevel.toUpperCase());
    }

    public String[] getContactPoints() {
        return contactPoints.split(",");
    }

    @Getter
    @Setter
    @Configuration
    public static class LocksConfiguration {

        private long pollingInterval;

        private long timeout;
    }

    @Bean
    public CqlMigrator getCqlMigrator(CassandraConfig cassandraConfiguration) throws Exception {
        CassandraConfig.LocksConfiguration locksConfiguration = cassandraConfiguration.getLocking();

        return CqlMigratorFactory.create(CassandraLockConfig.builder()
                .withPollingInterval(Duration.ofMillis(locksConfiguration.getPollingInterval()))
                .withTimeout(Duration.ofMillis(locksConfiguration.getTimeout()))
                .build());
    }
}
