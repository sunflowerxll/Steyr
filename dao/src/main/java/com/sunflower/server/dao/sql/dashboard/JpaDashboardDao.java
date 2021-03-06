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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import com.sunflower.server.common.data.Dashboard;
import com.sunflower.server.dao.dashboard.DashboardDao;
import com.sunflower.server.dao.model.sql.DashboardEntity;
import com.sunflower.server.dao.sql.JpaAbstractSearchTextDao;
import com.sunflower.server.dao.util.SqlDao;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
@Component
@SqlDao
public class JpaDashboardDao extends JpaAbstractSearchTextDao<DashboardEntity, Dashboard> implements DashboardDao {

    @Autowired
    DashboardRepository dashboardRepository;

    @Override
    protected Class<DashboardEntity> getEntityClass() {
        return DashboardEntity.class;
    }

    @Override
    protected CrudRepository<DashboardEntity, String> getCrudRepository() {
        return dashboardRepository;
    }
}
