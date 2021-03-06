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
package com.sunflower.server.service.queue.processing;

import com.sunflower.server.gen.transport.TransportProtos.ToRuleEngineMsg;
import com.sunflower.server.queue.common.TbProtoQueueMsg;

import java.util.UUID;

public class IdMsgPair {
    final UUID uuid;
    final TbProtoQueueMsg<ToRuleEngineMsg> msg;

    public IdMsgPair(UUID uuid, TbProtoQueueMsg<ToRuleEngineMsg> msg) {
        this.uuid = uuid;
        this.msg = msg;
    }
}
