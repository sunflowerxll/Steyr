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
package com.sunflower.server.dao.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.sunflower.server.common.data.id.ComponentDescriptorId;
import com.sunflower.server.common.data.id.TenantId;
import com.sunflower.server.common.data.page.TextPageData;
import com.sunflower.server.common.data.page.TextPageLink;
import com.sunflower.server.common.data.plugin.ComponentDescriptor;
import com.sunflower.server.common.data.plugin.ComponentScope;
import com.sunflower.server.common.data.plugin.ComponentType;

/**
 * @author Andrew Shvayka
 */
public interface ComponentDescriptorService {

    ComponentDescriptor saveComponent(TenantId tenantId, ComponentDescriptor component);

    ComponentDescriptor findById(TenantId tenantId, ComponentDescriptorId componentId);

    ComponentDescriptor findByClazz(TenantId tenantId, String clazz);

    TextPageData<ComponentDescriptor> findByTypeAndPageLink(TenantId tenantId, ComponentType type, TextPageLink pageLink);

    TextPageData<ComponentDescriptor> findByScopeAndTypeAndPageLink(TenantId tenantId, ComponentScope scope, ComponentType type, TextPageLink pageLink);

    boolean validate(TenantId tenantId, ComponentDescriptor component, JsonNode configuration);

    void deleteByClazz(TenantId tenantId, String clazz);

}
