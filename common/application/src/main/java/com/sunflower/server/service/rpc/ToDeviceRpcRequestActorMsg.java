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
package com.sunflower.server.service.rpc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import com.sunflower.rule.engine.api.msg.ToDeviceActorNotificationMsg;
import com.sunflower.server.common.data.id.DeviceId;
import com.sunflower.server.common.data.id.TenantId;
import com.sunflower.server.common.msg.MsgType;
import com.sunflower.server.common.msg.rpc.ToDeviceRpcRequest;

/**
 * Created by ashvayka on 16.04.18.
 */
@ToString
@RequiredArgsConstructor
public class ToDeviceRpcRequestActorMsg implements ToDeviceActorNotificationMsg {

    @Getter
    private final String serviceId;
    @Getter
    private final ToDeviceRpcRequest msg;

    @Override
    public DeviceId getDeviceId() {
        return msg.getDeviceId();
    }

    @Override
    public TenantId getTenantId() {
        return msg.getTenantId();
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.DEVICE_RPC_REQUEST_TO_DEVICE_ACTOR_MSG;
    }
}
