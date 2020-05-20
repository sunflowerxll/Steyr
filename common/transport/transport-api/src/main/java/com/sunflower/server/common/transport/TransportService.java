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
package com.sunflower.server.common.transport;

import com.sunflower.server.gen.transport.TransportProtos.ClaimDeviceMsg;
import com.sunflower.server.gen.transport.TransportProtos.GetAttributeRequestMsg;
import com.sunflower.server.gen.transport.TransportProtos.GetOrCreateDeviceFromGatewayRequestMsg;
import com.sunflower.server.gen.transport.TransportProtos.GetOrCreateDeviceFromGatewayResponseMsg;
import com.sunflower.server.gen.transport.TransportProtos.GetTenantRoutingInfoRequestMsg;
import com.sunflower.server.gen.transport.TransportProtos.GetTenantRoutingInfoResponseMsg;
import com.sunflower.server.gen.transport.TransportProtos.PostAttributeMsg;
import com.sunflower.server.gen.transport.TransportProtos.PostTelemetryMsg;
import com.sunflower.server.gen.transport.TransportProtos.SessionEventMsg;
import com.sunflower.server.gen.transport.TransportProtos.SessionInfoProto;
import com.sunflower.server.gen.transport.TransportProtos.SubscribeToAttributeUpdatesMsg;
import com.sunflower.server.gen.transport.TransportProtos.SubscribeToRPCMsg;
import com.sunflower.server.gen.transport.TransportProtos.SubscriptionInfoProto;
import com.sunflower.server.gen.transport.TransportProtos.ToDeviceRpcResponseMsg;
import com.sunflower.server.gen.transport.TransportProtos.ToServerRpcRequestMsg;
import com.sunflower.server.gen.transport.TransportProtos.ValidateDeviceCredentialsResponseMsg;
import com.sunflower.server.gen.transport.TransportProtos.ValidateDeviceTokenRequestMsg;
import com.sunflower.server.gen.transport.TransportProtos.ValidateDeviceX509CertRequestMsg;

/**
 * Created by ashvayka on 04.10.18.
 */
public interface TransportService {

    GetTenantRoutingInfoResponseMsg getRoutingInfo(GetTenantRoutingInfoRequestMsg msg);

    void process(ValidateDeviceTokenRequestMsg msg,
                 TransportServiceCallback<ValidateDeviceCredentialsResponseMsg> callback);

    void process(ValidateDeviceX509CertRequestMsg msg,
                 TransportServiceCallback<ValidateDeviceCredentialsResponseMsg> callback);

    void process(GetOrCreateDeviceFromGatewayRequestMsg msg,
                 TransportServiceCallback<GetOrCreateDeviceFromGatewayResponseMsg> callback);

    boolean checkLimits(SessionInfoProto sessionInfo, Object msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, SessionEventMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, PostTelemetryMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, PostAttributeMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, GetAttributeRequestMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, SubscribeToAttributeUpdatesMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, SubscribeToRPCMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, ToDeviceRpcResponseMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, ToServerRpcRequestMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, SubscriptionInfoProto msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, ClaimDeviceMsg msg, TransportServiceCallback<Void> callback);

    void registerAsyncSession(SessionInfoProto sessionInfo, SessionMsgListener listener);

    void registerSyncSession(SessionInfoProto sessionInfo, SessionMsgListener listener, long timeout);

    void reportActivity(SessionInfoProto sessionInfo);

    void deregisterSession(SessionInfoProto sessionInfo);

}
