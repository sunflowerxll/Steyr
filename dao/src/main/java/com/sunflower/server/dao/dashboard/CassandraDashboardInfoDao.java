/**
 * Copyright © 2016-2020 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sunflower.server.dao.dashboard;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.sunflower.server.common.data.DashboardInfo;
import com.sunflower.server.common.data.EntityType;
import com.sunflower.server.common.data.id.CustomerId;
import com.sunflower.server.common.data.id.TenantId;
import com.sunflower.server.common.data.page.TextPageLink;
import com.sunflower.server.common.data.page.TimePageLink;
import com.sunflower.server.common.data.relation.EntityRelation;
import com.sunflower.server.common.data.relation.RelationTypeGroup;
import com.sunflower.server.dao.DaoUtil;
import com.sunflower.server.dao.model.nosql.DashboardInfoEntity;
import com.sunflower.server.dao.nosql.CassandraAbstractSearchTextDao;
import com.sunflower.server.dao.relation.RelationDao;
import com.sunflower.server.dao.util.NoSqlDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.sunflower.server.dao.model.ModelConstants.DASHBOARD_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME;
import static com.sunflower.server.dao.model.ModelConstants.DASHBOARD_COLUMN_FAMILY_NAME;
import static com.sunflower.server.dao.model.ModelConstants.DASHBOARD_TENANT_ID_PROPERTY;

@Component
@Slf4j
@NoSqlDao
public class CassandraDashboardInfoDao extends CassandraAbstractSearchTextDao<DashboardInfoEntity, DashboardInfo> implements DashboardInfoDao {

    @Autowired
    private RelationDao relationDao;

    @Override
    protected Class<DashboardInfoEntity> getColumnFamilyClass() {
        return DashboardInfoEntity.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return DASHBOARD_COLUMN_FAMILY_NAME;
    }

    @Override
    public List<DashboardInfo> findDashboardsByTenantId(UUID tenantId, TextPageLink pageLink) {
        log.debug("Try to find dashboards by tenantId [{}] and pageLink [{}]", tenantId, pageLink);
        List<DashboardInfoEntity> dashboardEntities = findPageWithTextSearch(new TenantId(tenantId), DASHBOARD_BY_TENANT_AND_SEARCH_TEXT_COLUMN_FAMILY_NAME,
                Collections.singletonList(eq(DASHBOARD_TENANT_ID_PROPERTY, tenantId)),
                pageLink);

        log.trace("Found dashboards [{}] by tenantId [{}] and pageLink [{}]", dashboardEntities, tenantId, pageLink);
        return DaoUtil.convertDataList(dashboardEntities);
    }

    @Override
    public ListenableFuture<List<DashboardInfo>> findDashboardsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TimePageLink pageLink) {
        log.debug("Try to find dashboards by tenantId [{}], customerId[{}] and pageLink [{}]", tenantId, customerId, pageLink);

        ListenableFuture<List<EntityRelation>> relations = relationDao.findRelations(new TenantId(tenantId), new CustomerId(customerId), EntityRelation.CONTAINS_TYPE, RelationTypeGroup.DASHBOARD, EntityType.DASHBOARD, pageLink);

        return Futures.transformAsync(relations, input -> {
            List<ListenableFuture<DashboardInfo>> dashboardFutures = new ArrayList<>(input.size());
            for (EntityRelation relation : input) {
                dashboardFutures.add(findByIdAsync(new TenantId(tenantId), relation.getTo().getId()));
            }
            return Futures.successfulAsList(dashboardFutures);
        }, MoreExecutors.directExecutor());
    }

}
