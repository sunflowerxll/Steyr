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
package com.sunflower.server.dao.sqlts.hsql;

import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.sunflower.server.common.data.id.EntityId;
import com.sunflower.server.common.data.id.TenantId;
import com.sunflower.server.common.data.kv.TsKvEntry;
import com.sunflower.server.dao.model.sqlts.ts.TsKvEntity;
import com.sunflower.server.dao.sqlts.AbstractChunkedAggregationTimeseriesDao;
import com.sunflower.server.dao.timeseries.TimeseriesDao;
import com.sunflower.server.dao.util.HsqlDao;
import com.sunflower.server.dao.util.SqlTsDao;


@Component
@Slf4j
@SqlTsDao
@HsqlDao
public class JpaHsqlTimeseriesDao extends AbstractChunkedAggregationTimeseriesDao implements TimeseriesDao {

    @Override
    public ListenableFuture<Void> save(TenantId tenantId, EntityId entityId, TsKvEntry tsKvEntry, long ttl) {
        String strKey = tsKvEntry.getKey();
        Integer keyId = getOrSaveKeyId(strKey);
        TsKvEntity entity = new TsKvEntity();
        entity.setEntityId(entityId.getId());
        entity.setTs(tsKvEntry.getTs());
        entity.setKey(keyId);
        entity.setStrValue(tsKvEntry.getStrValue().orElse(null));
        entity.setDoubleValue(tsKvEntry.getDoubleValue().orElse(null));
        entity.setLongValue(tsKvEntry.getLongValue().orElse(null));
        entity.setBooleanValue(tsKvEntry.getBooleanValue().orElse(null));
        log.trace("Saving entity: {}", entity);
        return tsQueue.add(entity);
    }

}
