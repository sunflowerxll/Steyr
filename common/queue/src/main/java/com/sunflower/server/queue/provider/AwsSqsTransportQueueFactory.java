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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import com.sunflower.server.gen.transport.TransportProtos.ToCoreMsg;
import com.sunflower.server.gen.transport.TransportProtos.ToRuleEngineMsg;
import com.sunflower.server.gen.transport.TransportProtos.ToTransportMsg;
import com.sunflower.server.gen.transport.TransportProtos.TransportApiRequestMsg;
import com.sunflower.server.gen.transport.TransportProtos.TransportApiResponseMsg;
import com.sunflower.server.queue.TbQueueAdmin;
import com.sunflower.server.queue.TbQueueConsumer;
import com.sunflower.server.queue.TbQueueProducer;
import com.sunflower.server.queue.TbQueueRequestTemplate;
import com.sunflower.server.queue.common.DefaultTbQueueRequestTemplate;
import com.sunflower.server.queue.common.TbProtoQueueMsg;
import com.sunflower.server.queue.discovery.TbServiceInfoProvider;
import com.sunflower.server.queue.settings.TbQueueCoreSettings;
import com.sunflower.server.queue.settings.TbQueueTransportApiSettings;
import com.sunflower.server.queue.settings.TbQueueTransportNotificationSettings;
import com.sunflower.server.queue.sqs.TbAwsSqsAdmin;
import com.sunflower.server.queue.sqs.TbAwsSqsConsumerTemplate;
import com.sunflower.server.queue.sqs.TbAwsSqsProducerTemplate;
import com.sunflower.server.queue.sqs.TbAwsSqsQueueAttributes;
import com.sunflower.server.queue.sqs.TbAwsSqsSettings;

import javax.annotation.PreDestroy;

@Component
@ConditionalOnExpression("'${queue.type:null}'=='aws-sqs' && ('${service.type:null}'=='monolith' || '${service.type:null}'=='tb-transport')")
@Slf4j
public class AwsSqsTransportQueueFactory implements TbTransportQueueFactory {
    private final TbQueueTransportApiSettings transportApiSettings;
    private final TbQueueTransportNotificationSettings transportNotificationSettings;
    private final TbAwsSqsSettings sqsSettings;
    private final TbQueueCoreSettings coreSettings;
    private final TbServiceInfoProvider serviceInfoProvider;

    private final TbQueueAdmin coreAdmin;
    private final TbQueueAdmin transportApiAdmin;
    private final TbQueueAdmin notificationAdmin;

    public AwsSqsTransportQueueFactory(TbQueueTransportApiSettings transportApiSettings,
                                       TbQueueTransportNotificationSettings transportNotificationSettings,
                                       TbAwsSqsSettings sqsSettings,
                                       TbServiceInfoProvider serviceInfoProvider,
                                       TbQueueCoreSettings coreSettings,
                                       TbAwsSqsQueueAttributes sqsQueueAttributes) {
        this.transportApiSettings = transportApiSettings;
        this.transportNotificationSettings = transportNotificationSettings;
        this.sqsSettings = sqsSettings;
        this.serviceInfoProvider = serviceInfoProvider;
        this.coreSettings = coreSettings;

        this.coreAdmin = new TbAwsSqsAdmin(sqsSettings, sqsQueueAttributes.getCoreAttributes());
        this.transportApiAdmin = new TbAwsSqsAdmin(sqsSettings, sqsQueueAttributes.getTransportApiAttributes());
        this.notificationAdmin = new TbAwsSqsAdmin(sqsSettings, sqsQueueAttributes.getNotificationsAttributes());
    }

    @Override
    public TbQueueRequestTemplate<TbProtoQueueMsg<TransportApiRequestMsg>, TbProtoQueueMsg<TransportApiResponseMsg>> createTransportApiRequestTemplate() {
        TbAwsSqsProducerTemplate<TbProtoQueueMsg<TransportApiRequestMsg>> producerTemplate =
                new TbAwsSqsProducerTemplate<>(transportApiAdmin, sqsSettings, transportApiSettings.getRequestsTopic());

        TbAwsSqsConsumerTemplate<TbProtoQueueMsg<TransportApiResponseMsg>> consumerTemplate =
                new TbAwsSqsConsumerTemplate<>(transportApiAdmin, sqsSettings,
                        transportApiSettings.getResponsesTopic() + "_" + serviceInfoProvider.getServiceId(),
                        msg -> new TbProtoQueueMsg<>(msg.getKey(), TransportApiResponseMsg.parseFrom(msg.getData()), msg.getHeaders()));

        DefaultTbQueueRequestTemplate.DefaultTbQueueRequestTemplateBuilder
                <TbProtoQueueMsg<TransportApiRequestMsg>, TbProtoQueueMsg<TransportApiResponseMsg>> templateBuilder = DefaultTbQueueRequestTemplate.builder();
        templateBuilder.queueAdmin(transportApiAdmin);
        templateBuilder.requestTemplate(producerTemplate);
        templateBuilder.responseTemplate(consumerTemplate);
        templateBuilder.maxPendingRequests(transportApiSettings.getMaxPendingRequests());
        templateBuilder.maxRequestTimeout(transportApiSettings.getMaxRequestsTimeout());
        templateBuilder.pollInterval(transportApiSettings.getResponsePollInterval());
        return templateBuilder.build();
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToRuleEngineMsg>> createRuleEngineMsgProducer() {
        return new TbAwsSqsProducerTemplate<>(transportApiAdmin, sqsSettings, transportApiSettings.getRequestsTopic());
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToCoreMsg>> createTbCoreMsgProducer() {
        return new TbAwsSqsProducerTemplate<>(coreAdmin, sqsSettings, coreSettings.getTopic());
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<ToTransportMsg>> createTransportNotificationsConsumer() {
        return new TbAwsSqsConsumerTemplate<>(notificationAdmin, sqsSettings, transportNotificationSettings.getNotificationsTopic() + "_" + serviceInfoProvider.getServiceId(),
                msg -> new TbProtoQueueMsg<>(msg.getKey(), ToTransportMsg.parseFrom(msg.getData()), msg.getHeaders()));
    }

    @PreDestroy
    private void destroy() {
        if (coreAdmin != null) {
            coreAdmin.destroy();
        }
        if (transportApiAdmin != null) {
            transportApiAdmin.destroy();
        }
        if (notificationAdmin != null) {
            notificationAdmin.destroy();
        }
    }
}
