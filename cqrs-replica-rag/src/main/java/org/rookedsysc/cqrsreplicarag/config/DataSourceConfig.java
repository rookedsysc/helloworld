package org.rookedsysc.cqrsreplicarag.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    private static final String WRITE_DATASOURCE = "writeDataSource";
    private static final String READ_DATASOURCE = "readDataSource";

    @Bean(WRITE_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(READ_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSource readDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @DependsOn({WRITE_DATASOURCE, READ_DATASOURCE})
    public DataSource routingDataSource(
        @Qualifier(WRITE_DATASOURCE) final DataSource writeDataSource,
        @Qualifier(READ_DATASOURCE) final DataSource readDataSource) {
        final RoutingDataSource routingDataSource = new RoutingDataSource();
        final Map<Object, Object> datasource = new HashMap<>();
        datasource.put(DataSourceType.WRITE.toString(), writeDataSource);
        datasource.put(DataSourceType.READ.toString(), readDataSource);
        routingDataSource.setTargetDataSources(datasource);
        routingDataSource.setDefaultTargetDataSource(writeDataSource);
        return routingDataSource;
    }

    @Primary
    @Bean
    @DependsOn("routingDataSource")
    public LazyConnectionDataSourceProxy dataSource(final DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
