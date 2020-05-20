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
package com.sunflower.server.common.msg.plugin;

import lombok.Getter;
import lombok.ToString;
import com.sunflower.server.common.data.EntityType;
import com.sunflower.server.common.data.id.EntityId;
import com.sunflower.server.common.data.id.RuleChainId;
import com.sunflower.server.common.data.id.TenantId;
import com.sunflower.server.common.data.plugin.ComponentLifecycleEvent;
import com.sunflower.server.common.msg.MsgType;
import com.sunflower.server.common.msg.TbActorMsg;
import com.sunflower.server.common.msg.aware.TenantAwareMsg;
import com.sunflower.server.common.msg.cluster.ToAllNodesMsg;

import java.util.Optional;

/**
 * @author Andrew Shvayka
 */
@ToString
public class ComponentLifecycleMsg implements TenantAwareMsg, ToAllNodesMsg {
    @Getter
    private final TenantId tenantId;
    @Getter
    private final EntityId entityId;
    @Getter
    private final ComponentLifecycleEvent event;

    public ComponentLifecycleMsg(TenantId tenantId, EntityId entityId, ComponentLifecycleEvent event) {
        this.tenantId = tenantId;
        this.entityId = entityId;
        this.event = event;
    }

    public Optional<RuleChainId> getRuleChainId() {
        return entityId.getEntityType() == EntityType.RULE_CHAIN ? Optional.of((RuleChainId) entityId) : Optional.empty();
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.COMPONENT_LIFE_CYCLE_MSG;
    }
}
