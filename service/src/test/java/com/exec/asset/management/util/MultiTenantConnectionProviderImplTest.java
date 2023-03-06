package com.exec.asset.management.util;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class MultiTenantConnectionProviderImplTest {

    private MultiTenantConnectionProviderImpl multiTenantConnectionProvider;

    @Test
    public void testSelectAnyDataSource() {
        DataSource first = mock(DataSource.class);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("test-tenant", first);
        multiTenantConnectionProvider = new MultiTenantConnectionProviderImpl(dataSourceMap);

        assertNotNull(multiTenantConnectionProvider.selectAnyDataSource());
    }

    @Test
    public void testSelectDataSourceValidTenant() {
        DataSource first = mock(DataSource.class);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("test-tenant", first);
        multiTenantConnectionProvider = new MultiTenantConnectionProviderImpl(dataSourceMap);
        assertNotNull(multiTenantConnectionProvider.selectDataSource("test-tenant"));
    }

    @Test
    public void testSelectDataSourceInvalidTenant() {
        DataSource first = mock(DataSource.class);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("test-tenant", first);
        multiTenantConnectionProvider = new MultiTenantConnectionProviderImpl(dataSourceMap);
        assertThrows(IllegalStateException.class, () -> multiTenantConnectionProvider.selectDataSource("test"));
    }
}
