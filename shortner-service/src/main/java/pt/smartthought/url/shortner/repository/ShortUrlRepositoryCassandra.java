package pt.smartthought.url.shortner.repository;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import pt.smartthought.url.shortner.config.CassandraConfig;
import pt.smartthought.url.shortner.domain.ShortUrlRepository;
import pt.smartthought.url.shortner.domain.ShortUrl;
import pt.smartthought.url.shortner.repository.cassandra.CassandraConnector;

import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

/**
 * Cassandra implementation of the repository.
 * Cassandra scales really well for millions of accesses. implements sharding and operates in active active mode out
 * of the box.
 *
 * I think it's the best fit for the exercise, to operate in scale
 */
@Repository
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Slf4j
public class ShortUrlRepositoryCassandra implements ShortUrlRepository {

    private static final String INSERT_URL = QueryBuilder.insertInto("short_url")
            .value("id", QueryBuilder.bindMarker(":id"))
            .value("original_url", QueryBuilder.bindMarker(":original_url"))
            .toString();

    private static final String SELECT_URL = QueryBuilder.select("id", "original_url")
            .from("short_url")
                .where(eq("id", QueryBuilder.bindMarker(":id")))
            .toString();

    private final CassandraConnector connector;
    private final CassandraConfig cassandraConfig;

    @Override
    public void save(ShortUrl shortUrl) {
        connector.withPreparedStatement(INSERT_URL, cassandraConfig.getConsistencyLevel(), (session, stmt) -> {
            BoundStatement bound = stmt.bind()
                    .setUUID(":id", shortUrl.getShortUrl())
                    .setString(":original_url", shortUrl.getOriginalUrl());


            ResultSet result = session.execute(bound);
            if (result == null || !result.wasApplied()) {
                throw new RuntimeException("Couldn't applu batch statement to insert a new url");
            }
        });
    }

    @Override
    public ShortUrl get(UUID shortUrl) {
        ResultSet result = connector
                .withPreparedStatement(SELECT_URL, cassandraConfig.getConsistencyLevel(), (session, stmt) -> {
                    BoundStatement bound = stmt.bind()
                            .setUUID(":id", shortUrl);

                    return session.execute(bound);
                });

        if (result != null){
            Row row = result.all().stream()
                    .findFirst()
                    .orElse(null);

            if (row == null){
                return null;
            }

            return new ShortUrl(
                    row.getUUID("id"),
                    row.getString("original_url")
            );
        }

        return null;
    }
}
