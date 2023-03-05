package com.exec.asset.management.util;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;

public class MultiTenantConnectionProviderImpl extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private final Map<String, DataSource> dataSources = new HashMap<>();

    public MultiTenantConnectionProviderImpl(Map<String, DataSource> dataSources) {
        this.dataSources.putAll(dataSources);
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return dataSources.values().iterator().next();
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        DataSource dataSource = dataSources.get(tenantIdentifier);
        if (dataSource == null) {
            throw new IllegalStateException("Tenant not found: " + tenantIdentifier);
        }
        return dataSource;
    }
}
