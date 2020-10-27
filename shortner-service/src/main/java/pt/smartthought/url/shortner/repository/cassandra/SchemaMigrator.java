package pt.smartthought.url.shortner.repository.cassandra;

import com.datastax.driver.core.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pt.smartthought.url.shortner.config.CassandraConfig;
import uk.sky.cqlmigrate.CqlMigrator;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class SchemaMigrator {

    private final CqlMigrator cqlMigrator;
    private final CassandraConfig cassandraConfiguration;

    public void migrate(Session session) throws Exception {
        String keyspace = cassandraConfiguration.getKeyspace();

        List<Path> uris = new ArrayList<>(1);

        uris.add(Path.of(new File(cassandraConfiguration.getSchemaPath()).toURI()));

        cqlMigrator.migrate(session, keyspace, uris);
    }
}

