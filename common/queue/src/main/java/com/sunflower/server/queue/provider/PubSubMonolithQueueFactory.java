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
package com.sunflower.server.queue.provider;

import com.google.protobuf.util.JsonFormat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.sunflower.server.common.msg.queue.ServiceType;
import com.sunflower.server.gen.js.JsInvokeProtos.RemoteJsRequest;
import com.sunflower.server.gen.js.JsInvokeProtos.RemoteJsResponse;
import com.sunflower.server.gen.transport.TransportProtos.ToCoreMsg;
import com.sunflower.server.gen.transport.TransportProtos.ToCoreNotificationMsg;
import com.sunflower.server.gen.transport.TransportProtos.ToRuleEngineMsg;
import com.sunflower.server.gen.transport.TransportProtos.ToRuleEngineNotificationMsg;
import com.sunflower.server.gen.transport.TransportProtos.ToTransportMsg;
import com.sunflower.server.gen.transport.TransportProtos.TransportApiRequestMsg;
import com.sunflower.server.gen.transport.TransportProtos.TransportApiResponseMsg;
import com.sunflower.server.queue.TbQueueAdmin;
import com.sunflower.server.queue.TbQueueConsumer;
import com.sunflower.server.queue.TbQueueProducer;
import com.sunflower.server.queue.TbQueueRequestTemplate;
import com.sunflower.server.queue.common.DefaultTbQueueRequestTemplate;
import com.sunflower.server.queue.common.TbProtoJsQueueMsg;
import com.sunflower.server.queue.common.TbProtoQueueMsg;
import com.sunflower.server.queue.discovery.PartitionService;
import com.sunflower.server.queue.discovery.TbServiceInfoProvider;
import com.sunflower.server.queue.pubsub.TbPubSubAdmin;
import com.sunflower.server.queue.pubsub.TbPubSubConsumerTemplate;
import com.sunflower.server.queue.pubsub.TbPubSubProducerTemplate;
import com.sunflower.server.queue.pubsub.TbPubSubSettings;
import com.sunflower.server.queue.pubsub.TbPubSubSubscriptionSettings;
import com.sunflower.server.queue.settings.TbQueueCoreSettings;
import com.sunflower.server.queue.settings.TbQueueRemoteJsInvokeSettings;
import com.sunflower.server.queue.settings.TbQueueRuleEngineSettings;
import com.sunflower.server.queue.settings.TbQueueTransportApiSettings;
import com.sunflower.server.queue.settings.TbQueueTransportNotificationSettings;
import com.sunflower.server.queue.settings.TbRuleEngineQueueConfiguration;

import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnExpression("'${queue.type:null}'=='pubsub' && '${service.type:null}'=='monolith'")
public class PubSubMonolithQueueFactory implements TbCoreQueueFactory, TbRuleEngineQueueFactory {

    private final TbPubSubSettings pubSubSettings;
    private final TbQueueCoreSettings coreSettings;
    private final TbQueueRuleEngineSettings ruleEngineSettings;
    private final TbQueueTransportApiSettings transportApiSettings;
    private final TbQueueTransportNotificationSettings transportNotificationSettings;
    private final PartitionService partitionService;
    private final TbServiceInfoProvider serviceInfoProvider;
    private final TbQueueRemoteJsInvokeSettings jsInvokeSettings;

    private final TbQueueAdmin coreAdmin;
    private final TbQueueAdmin ruleEngineAdmin;
    private final TbQueueAdmin jsExecutorAdmin;
    private final TbQueueAdmin transportApiAdmin;
    private final TbQueueAdmin notificationAdmin;

    public PubSubMonolithQueueFactory(TbPubSubSettings pubSubSettings,
                                      TbQueueCoreSettings coreSettings,
                                      TbQueueRuleEngineSettings ruleEngineSettings,
                                      TbQueueTransportApiSettings transportApiSettings,
                                      TbQueueTransportNotificationSettings transportNotificationSettings,
                                      PartitionService partitionService,
                                      TbServiceInfoProvider serviceInfoProvider,
                                      TbPubSubSubscriptionSettings pubSubSubscriptionSettings,
                                      TbQueueRemoteJsInvokeSettings jsInvokeSettings) {
        this.pubSubSettings = pubSubSettings;
        this.coreSettings = coreSettings;
        this.ruleEngineSettings = ruleEngineSettings;
        this.transportApiSettings = transportApiSettings;
        this.transportNotificationSettings = transportNotificationSettings;
        this.partitionService = partitionService;
        this.serviceInfoProvider = serviceInfoProvider;

        this.coreAdmin = new TbPubSubAdmin(pubSubSettings, pubSubSubscriptionSettings.getCoreSettings());
        this.ruleEngineAdmin = new TbPubSubAdmin(pubSubSettings, pubSubSubscriptionSettings.getRuleEngineSettings());
        this.jsExecutorAdmin = new TbPubSubAdmin(pubSubSettings, pubSubSubscriptionSettings.getJsExecutorSettings());
        this.transportApiAdmin = new TbPubSubAdmin(pubSubSettings, pubSubSubscriptionSettings.getTransportApiSettings());
        this.notificationAdmin = new TbPubSubAdmin(pubSubSettings, pubSubSubscriptionSettings.getNotificationsSettings());
        this.jsInvokeSettings = jsInvokeSettings;
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToTransportMsg>> createTransportNotificationsMsgProducer() {
        return new TbPubSubProducerTemplate<>(notificationAdmin, pubSubSettings, transportNotificationSettings.getNotificationsTopic());
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToRuleEngineMsg>> createRuleEngineMsgProducer() {
        return new TbPubSubProducerTemplate<>(ruleEngineAdmin, pubSubSettings, ruleEngineSettings.getTopic());

    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToRuleEngineNotificationMsg>> createRuleEngineNotificationsMsgProducer() {
        return new TbPubSubProducerTemplate<>(ruleEngineAdmin, pubSubSettings, ruleEngineSettings.getTopic());
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToCoreMsg>> createTbCoreMsgProducer() {
        return new TbPubSubProducerTemplate<>(coreAdmin, pubSubSettings, coreSettings.getTopic());
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToCoreNotificationMsg>> createTbCoreNotificationsMsgProducer() {
        return new TbPubSubProducerTemplate<>(coreAdmin, pubSubSettings, coreSettings.getTopic());
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<ToRuleEngineMsg>> createToRuleEngineMsgConsumer(TbRuleEngineQueueConfiguration configuration) {
        return new TbPubSubConsumerTemplate<>(ruleEngineAdmin, pubSubSettings, ruleEngineSettings.getTopic(),
                msg -> new TbProtoQueueMsg<>(msg.getKey(), ToRuleEngineMsg.parseFrom(msg.getData()), msg.getHeaders()));
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<ToRuleEngineNotificationMsg>> createToRuleEngineNotificationsMsgConsumer() {
        return new TbPubSubConsumerTemplate<>(notificationAdmin, pubSubSettings,
                partitionService.getNotificationsTopic(ServiceType.TB_RULE_ENGINE, serviceInfoProvider.getServiceId()).getFullTopicName(),
                msg -> new TbProtoQueueMsg<>(msg.getKey(), ToRuleEngineNotificationMsg.parseFrom(msg.getData()), msg.getHeaders()));
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<ToCoreMsg>> createToCoreMsgConsumer() {
        return new TbPubSubConsumerTemplate<>(coreAdmin, pubSubSettings, coreSettings.getTopic(),
                msg -> new TbProtoQueueMsg<>(msg.getKey(), ToCoreMsg.parseFrom(msg.getData()), msg.getHeaders()));
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<ToCoreNotificationMsg>> createToCoreNotificationsMsgConsumer() {
        return new TbPubSubConsumerTemplate<>(notificationAdmin, pubSubSettings,
                partitionService.getNotificationsTopic(ServiceType.TB_CORE, serviceInfoProvider.getServiceId()).getFullTopicName(),
                msg -> new TbProtoQueueMsg<>(msg.getKey(), ToCoreNotificationMsg.parseFrom(msg.getData()), msg.getHeaders()));
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<TransportApiRequestMsg>> createTransportApiRequestConsumer() {
        return new TbPubSubConsumerTemplate<>(transportApiAdmin, pubSubSettings, transportApiSettings.getRequestsTopic(),
                msg -> new TbProtoQueueMsg<>(msg.getKey(), TransportApiRequestMsg.parseFrom(msg.getData()), msg.getHeaders()));
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<TransportApiResponseMsg>> createTransportApiResponseProducer() {
        return new TbPubSubProducerTemplate<>(transportApiAdmin, pubSubSettings, transportApiSettings.getResponsesTopic());
    }

    @Override
    @Bean
    public TbQueueRequestTemplate<TbProtoJsQueueMsg<RemoteJsRequest>, TbProtoQueueMsg<RemoteJsResponse>> createRemoteJsRequestTemplate() {
        TbQueueProducer<TbProtoJsQueueMsg<RemoteJsRequest>> producer = new TbPubSubProducerTemplate<>(jsExecutorAdmin, pubSubSettings, jsInvokeSettings.getRequestTopic());
        TbQueueConsumer<TbProtoQueueMsg<RemoteJsResponse>> consumer = new TbPubSubConsumerTemplate<>(jsExecutorAdmin, pubSubSettings,
                jsInvokeSettings.getResponseTopic() + "." + serviceInfoProvider.getServiceId(),
                msg -> {
                    RemoteJsResponse.Builder builder = RemoteJsResponse.newBuilder();
                    JsonFormat.parser().ignoringUnknownFields().merge(new String(msg.getData(), StandardCharsets.UTF_8), builder);
                    return new TbProtoQueueMsg<>(msg.getKey(), builder.build(), msg.getHeaders());
                });

        DefaultTbQueueRequestTemplate.DefaultTbQueueRequestTemplateBuilder
                <TbProtoJsQueueMsg<RemoteJsRequest>, TbProtoQueueMsg<RemoteJsResponse>> builder = DefaultTbQueueRequestTemplate.builder();
        builder.queueAdmin(jsExecutorAdmin);
        builder.requestTemplate(producer);
        builder.responseTemplate(consumer);
        builder.maxPendingRequests(jsInvokeSettings.getMaxPendingRequests());
        builder.maxRequestTimeout(jsInvokeSettings.getMaxRequestsTimeout());
        builder.pollInterval(jsInvokeSettings.getResponsePollInterval());
        return builder.build();
    }

    @PreDestroy
    private void destroy() {
        if (coreAdmin != null) {
            coreAdmin.destroy();
        }
        if (ruleEngineAdmin != null) {
            ruleEngineAdmin.destroy();
        }
        if (jsExecutorAdmin != null) {
            jsExecutorAdmin.destroy();
        }
        if (transportApiAdmin != null) {
            transportApiAdmin.destroy();
        }
        if (notificationAdmin != null) {
            notificationAdmin.destroy();
        }
    }
}
