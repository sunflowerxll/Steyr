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
package com.sunflower.server.dao.sql.alarm;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import com.sunflower.server.common.data.EntityType;
import com.sunflower.server.dao.model.sql.AlarmEntity;
import com.sunflower.server.dao.util.SqlDao;

import java.util.List;

/**
 * Created by Valerii Sosliuk on 5/21/2017.
 */
@SqlDao
public interface AlarmRepository extends CrudRepository<AlarmEntity, String> {

    @Query("SELECT a FROM AlarmEntity a WHERE a.originatorId = :originatorId AND a.type = :alarmType ORDER BY startTs DESC")
    List<AlarmEntity> findLatestByOriginatorAndType(@Param("originatorId") String originatorId,
                                                    @Param("alarmType") String alarmType,
                                                    Pageable pageable);
}
