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
package com.sunflower.server.actors.ruleChain;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import com.sunflower.rule.engine.api.TbNode;
import com.sunflower.rule.engine.api.TbNodeConfiguration;
import com.sunflower.server.actors.ActorSystemContext;
import com.sunflower.server.actors.shared.ComponentMsgProcessor;
import com.sunflower.server.common.data.id.RuleNodeId;
import com.sunflower.server.common.data.id.TenantId;
import com.sunflower.server.common.data.plugin.ComponentLifecycleState;
import com.sunflower.server.common.data.rule.RuleNode;
import com.sunflower.server.common.msg.queue.PartitionChangeMsg;
import com.sunflower.server.common.msg.queue.RuleNodeException;

/**
 * @author Andrew Shvayka
 */
public class RuleNodeActorMessageProcessor extends ComponentMsgProcessor<RuleNodeId> {

    private final String ruleChainName;
    private final ActorRef self;
    private RuleNode ruleNode;
    private TbNode tbNode;
    private DefaultTbContext defaultCtx;

    RuleNodeActorMessageProcessor(TenantId tenantId, String ruleChainName, RuleNodeId ruleNodeId, ActorSystemContext systemContext
            , ActorRef parent, ActorRef self) {
        super(systemContext, tenantId, ruleNodeId);
        this.ruleChainName = ruleChainName;
        this.self = self;
        this.ruleNode = systemContext.getRuleChainService().findRuleNodeById(tenantId, entityId);
        this.defaultCtx = new DefaultTbContext(systemContext, new RuleNodeCtx(tenantId, parent, self, ruleNode));
    }

    @Override
    public void start(ActorContext context) throws Exception {
        tbNode = initComponent(ruleNode);
        if (tbNode != null) {
            state = ComponentLifecycleState.ACTIVE;
        }
    }

    @Override
    public void onUpdate(ActorContext context) throws Exception {
        RuleNode newRuleNode = systemContext.getRuleChainService().findRuleNodeById(tenantId, entityId);
        boolean restartRequired = state != ComponentLifecycleState.ACTIVE ||
                !(ruleNode.getType().equals(newRuleNode.getType()) && ruleNode.getConfiguration().equals(newRuleNode.getConfiguration()));
        this.ruleNode = newRuleNode;
        this.defaultCtx.updateSelf(newRuleNode);
        if (restartRequired) {
            if (tbNode != null) {
                tbNode.destroy();
            }
            start(context);
        }
    }

    @Override
    public void stop(ActorContext context) {
        if (tbNode != null) {
            tbNode.destroy();
        }
        context.stop(self);
    }

    @Override
    public void onPartitionChangeMsg(PartitionChangeMsg msg) {
        if (tbNode != null) {
            tbNode.onPartitionChangeMsg(defaultCtx, msg);
        }
    }

    public void onRuleToSelfMsg(RuleNodeToSelfMsg msg) throws Exception {
        checkActive(msg.getMsg());
        if (ruleNode.isDebugMode()) {
            systemContext.persistDebugInput(tenantId, entityId, msg.getMsg(), "Self");
        }
        try {
            tbNode.onMsg(defaultCtx, msg.getMsg());
        } catch (Exception e) {
            defaultCtx.tellFailure(msg.getMsg(), e);
        }
    }

    void onRuleChainToRuleNodeMsg(RuleChainToRuleNodeMsg msg) throws Exception {
        checkActive(msg.getMsg());
        if (ruleNode.isDebugMode()) {
            systemContext.persistDebugInput(tenantId, entityId, msg.getMsg(), msg.getFromRelationType());
        }
        try {
            tbNode.onMsg(msg.getCtx(), msg.getMsg());
        } catch (Exception e) {
            msg.getCtx().tellFailure(msg.getMsg(), e);
        }
    }

    @Override
    public String getComponentName() {
        return ruleNode.getName();
    }

    private TbNode initComponent(RuleNode ruleNode) throws Exception {
        TbNode tbNode = null;
        if (ruleNode != null) {
            Class<?> componentClazz = Class.forName(ruleNode.getType());
            tbNode = (TbNode) (componentClazz.newInstance());
            tbNode.init(defaultCtx, new TbNodeConfiguration(ruleNode.getConfiguration()));
        }
        return tbNode;
    }

    @Override
    protected RuleNodeException getInactiveException() {
        return new RuleNodeException("Rule Node is not active! Failed to initialize.", ruleChainName, ruleNode);
    }
}
