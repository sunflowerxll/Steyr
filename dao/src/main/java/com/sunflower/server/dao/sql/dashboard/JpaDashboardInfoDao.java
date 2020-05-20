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
package com.sunflower.server.dao.sql.dashboard;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import com.sunflower.server.common.data.DashboardInfo;
import com.sunflower.server.common.data.EntityType;
import com.sunflower.server.common.data.UUIDConverter;
import com.sunflower.server.common.data.id.CustomerId;
import com.sunflower.server.common.data.id.TenantId;
import com.sunflower.server.common.data.page.TextPageLink;
import com.sunflower.server.common.data.page.TimePageLink;
import com.sunflower.server.common.data.relation.EntityRelation;
import com.sunflower.server.common.data.relation.RelationTypeGroup;
import com.sunflower.server.dao.DaoUtil;
import com.sunflower.server.dao.dashboard.DashboardInfoDao;
import com.sunflower.server.dao.model.sql.DashboardInfoEntity;
import com.sunflower.server.dao.relation.RelationDao;
import com.sunflower.server.dao.sql.JpaAbstractSearchTextDao;
import com.sunflower.server.dao.util.SqlDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.sunflower.server.dao.model.ModelConstants.NULL_UUID_STR;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
@Slf4j
@Component
@SqlDao
public class JpaDashboardInfoDao extends JpaAbstractSearchTextDao<DashboardInfoEntity, DashboardInfo> implements DashboardInfoDao {

    @Autowired
    private RelationDao relationDao;

    @Autowired
    private DashboardInfoRepository dashboardInfoRepository;

    @Override
    protected Class getEntityClass() {
        return DashboardInfoEntity.class;
    }

    @Override
    protected CrudRepository getCrudRepository() {
        return dashboardInfoRepository;
    }

    @Override
    public List<DashboardInfo> findDashboardsByTenantId(UUID tenantId, TextPageLink pageLink) {
        return DaoUtil.convertDataList(dashboardInfoRepository
                .findByTenantId(
                        UUIDConverter.fromTimeUUID(tenantId),
                        Objects.toString(pageLink.getTextSearch(), ""),
                        pageLink.getIdOffset() == null ? NULL_UUID_STR : UUIDConverter.fromTimeUUID(pageLink.getIdOffset()),
                        PageRequest.of(0, pageLink.getLimit())));
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
