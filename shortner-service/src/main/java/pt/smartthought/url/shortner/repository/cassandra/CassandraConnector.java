package pt.smartthought.url.shortner.repository.cassandra;


import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.LoggingRetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import pt.smartthought.url.shortner.config.CassandraConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class CassandraConnector implements AutoCloseable, ApplicationListener<ContextClosedEvent> {

	private final CassandraConfig config;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final SchemaMigrator schemaMigrator;
	private volatile Session session;

	private Map<String, PreparedStatement> preparedStatements = new ConcurrentHashMap<>();

	@SuppressWarnings("unused")
	@PostConstruct
	public void init() {
		createSession(2);
	}

	@SuppressWarnings("unused")
	@PreDestroy
	public void close() {
		log.info("Closing cluster resource.");
		scheduler.shutdownNow();
		if (session != null) {
			session.close();
			session.getCluster().close();
			session = null;
			preparedStatements.clear();
		}
		log.info("Cluster resource closed.");
	}

	public <V> V withPreparedStatement(String query, ConsistencyLevel level, BiFunction<Session, PreparedStatement, V> biFunction) {
		if (session != null) {
			PreparedStatement stmt = preparedStatements.computeIfAbsent(query, k -> session.prepare(query));
			stmt.setConsistencyLevel(level);
			return biFunction.apply(session, stmt);
		}

		return null;
	}

	public void withPreparedStatement(String query, ConsistencyLevel level, BiConsumer<Session, PreparedStatement> biConsumer) {
		if (session != null) {
			PreparedStatement stmt = preparedStatements.computeIfAbsent(query, k -> session.prepare(query));
			stmt.setConsistencyLevel(level);
			biConsumer.accept(session, stmt);
		}
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		try {
			close();
		} catch (Exception e) {
			log.error("Error while closing the Cassandra Connector", e);
		}
	}

	public static void logCassandraClusterConfiguration(Cluster cluster) {

		if(!log.isDebugEnabled())
			return;

		Configuration clusterCfg = cluster.getConfiguration();

		MetricsOptions metricsOpt = clusterCfg.getMetricsOptions();
		log.debug("Metrics Options enabled:{}  JMX Reporting enabled:{}", metricsOpt.isEnabled(), metricsOpt.isJMXReportingEnabled());

		ProtocolOptions protoOpt = clusterCfg.getProtocolOptions();
		log.debug("Protocol Options auth:{} compression:{} maxSchemaWaitTimeout:{} protocolVersion:{} port:{}",
				protoOpt.getAuthProvider().toString(),
				protoOpt.getCompression().toString(),
				protoOpt.getMaxSchemaAgreementWaitSeconds(),
				protoOpt.getProtocolVersion(),
				protoOpt.getPort());

		SocketOptions sockOpt = clusterCfg.getSocketOptions();
		log.debug("Socket Options connectTimeoutMs:{} readTimeoutMs:{} keepAlive:{} soLingerTimeout:{} TCPnoDelay:{} receiveBufSize:{}, sendBufSize:{}",
				sockOpt.getConnectTimeoutMillis(),
				sockOpt.getReadTimeoutMillis(),
				sockOpt.getKeepAlive(),
				sockOpt.getSoLinger(),
				sockOpt.getTcpNoDelay(),
				sockOpt.getReceiveBufferSize(),
				sockOpt.getSendBufferSize());

		PoolingOptions poolOpt = clusterCfg.getPoolingOptions();
		log.debug("Pooling Options heartbeatIntervalSec:{} idleTimeoutSec:{} poolTimeoutMs:{}",
				poolOpt.getHeartbeatIntervalSeconds(),
				poolOpt.getIdleTimeoutSeconds(),
				poolOpt.getPoolTimeoutMillis());

		log.debug("LOCAL Connections core:{}  max:{}",
				poolOpt.getCoreConnectionsPerHost( HostDistance.LOCAL ),
				poolOpt.getMaxConnectionsPerHost( HostDistance.LOCAL ));
		log.debug("REMOTE Connections core:{}  max:{}",
				poolOpt.getCoreConnectionsPerHost( HostDistance.REMOTE ),
				poolOpt.getMaxConnectionsPerHost( HostDistance.REMOTE ));

		QueryOptions queryOpt = clusterCfg.getQueryOptions();
		log.debug("Query Options fetchSize:{}  serialConsistencyLevel:{} maxPendRefreshSchemaReqs:{} maxPendRefreshNodeListReqs:{} maxPendRefreshNodeReqs:{} refreshSchemaIntervalMs:{} refreshNodeListIntervalMs:{} refreshNodeIntervalMs:{}",
				queryOpt.getFetchSize(),
				queryOpt.getSerialConsistencyLevel().name(),
				queryOpt.getMaxPendingRefreshSchemaRequests(),
				queryOpt.getMaxPendingRefreshNodeListRequests(),
				queryOpt.getMaxPendingRefreshNodeRequests(),
				queryOpt.getRefreshSchemaIntervalMillis(),
				queryOpt.getRefreshNodeListIntervalMillis(),
				queryOpt.getRefreshNodeIntervalMillis());
	}

	private void createSession(final int delay) {
		scheduler.schedule(() -> createSessionTask(delay), delay, SECONDS);
	}

	private void createSessionTask(final int delay) {
		Cluster cluster = null;
		try {
			cluster = initCluster(config);
			session = cluster.connect(config.getKeyspace());
			log.info("Successfully created cassandra session");

			logCassandraClusterConfiguration(cluster);

			runMigration(delay);
		} catch ( DriverException | Error e) {
			log.error("Error connecting to cassandra {}", e.getLocalizedMessage());
			if(cluster != null) {
				cluster.close();
			}
			createSession(delay);
		}
	}

	private void runMigration(int delay) {
		scheduler.schedule(() -> runMigrationTask(delay), delay, SECONDS);
	}

	private void runMigrationTask(final int delay) {
		if (!config.isMigrationEnabled()) {
			log.info("Database migration disabled... Skipping");
			return;
		}

		try {
			schemaMigrator.migrate(session);
			log.info("Successfully migrate the database");
		} catch (Exception e) {
			log.error("Failed to perform schema migration. ", e);
			runMigration(delay);
		}
	}

	private static Cluster initCluster(CassandraConfig config) {
		return initCluster(config, config.getPoolMinConnections(), config.getPoolMaxConnections() );
	}

	private static Cluster initCluster(CassandraConfig config, Integer poolMinConnections, Integer poolMaxConnections) {

		SocketOptions sockOpt = new SocketOptions();
		sockOpt.setConnectTimeoutMillis(config.getConnectionTimeoutMs());
		sockOpt.setReadTimeoutMillis(config.getSocketReadTimeoutMs());

		PoolingOptions poolOpt = new PoolingOptions();
		poolOpt.setPoolTimeoutMillis(config.getPoolWaitTimeoutMs());

		// NOTE: Use same connection limits for LOCAL and REMOTE nodes
		poolOpt.setConnectionsPerHost( HostDistance.LOCAL, poolMinConnections, poolMaxConnections);
		poolOpt.setConnectionsPerHost( HostDistance.REMOTE, poolMinConnections, poolMaxConnections);

		// NOTE: explicitly NOT including a keyspace.
		return Cluster.builder()
			.withTimestampGenerator(new AtomicMonotonicTimestampGenerator())
			.addContactPoints(config.getContactPoints())
			.withPort(config.getPort())
			.withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder()
					.withLocalDc(config.getLocalDc())
					.build())
			.withRetryPolicy(new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE))
			.withReconnectionPolicy(new ConstantReconnectionPolicy(config.getConstantReconnectionDelayMs()))
			.withSocketOptions(sockOpt)
			.withPoolingOptions(poolOpt)
			.withCredentials(config.getUsername(), config.getPassword())
			.build();
	}
}
