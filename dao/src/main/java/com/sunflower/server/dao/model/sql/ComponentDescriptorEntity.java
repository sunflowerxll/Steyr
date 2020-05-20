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
package com.sunflower.server.dao.model.sql;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.sunflower.server.common.data.id.ComponentDescriptorId;
import com.sunflower.server.common.data.plugin.ComponentDescriptor;
import com.sunflower.server.common.data.plugin.ComponentScope;
import com.sunflower.server.common.data.plugin.ComponentType;
import com.sunflower.server.dao.model.BaseSqlEntity;
import com.sunflower.server.dao.model.ModelConstants;
import com.sunflower.server.dao.model.SearchTextEntity;
import com.sunflower.server.dao.util.mapping.JsonStringType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = ModelConstants.COMPONENT_DESCRIPTOR_COLUMN_FAMILY_NAME)
public class ComponentDescriptorEntity extends BaseSqlEntity<ComponentDescriptor> implements SearchTextEntity<ComponentDescriptor> {

    @Enumerated(EnumType.STRING)
    @Column(name = ModelConstants.COMPONENT_DESCRIPTOR_TYPE_PROPERTY)
    private ComponentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = ModelConstants.COMPONENT_DESCRIPTOR_SCOPE_PROPERTY)
    private ComponentScope scope;

    @Column(name = ModelConstants.COMPONENT_DESCRIPTOR_NAME_PROPERTY)
    private String name;

    @Column(name = ModelConstants.COMPONENT_DESCRIPTOR_CLASS_PROPERTY, unique = true)
    private String clazz;

    @Type(type = "json")
    @Column(name = ModelConstants.COMPONENT_DESCRIPTOR_CONFIGURATION_DESCRIPTOR_PROPERTY)
    private JsonNode configurationDescriptor;

    @Column(name = ModelConstants.COMPONENT_DESCRIPTOR_ACTIONS_PROPERTY)
    private String actions;

    @Column(name = ModelConstants.SEARCH_TEXT_PROPERTY)
    private String searchText;

    public ComponentDescriptorEntity() {
    }

    public ComponentDescriptorEntity(ComponentDescriptor component) {
        if (component.getId() != null) {
            this.setUuid(component.getId().getId());
        }
        this.actions = component.getActions();
        this.type = component.getType();
        this.scope = component.getScope();
        this.name = component.getName();
        this.clazz = component.getClazz();
        this.configurationDescriptor = component.getConfigurationDescriptor();
        this.searchText = component.getName();
    }

    @Override
    public ComponentDescriptor toData() {
        ComponentDescriptor data = new ComponentDescriptor(new ComponentDescriptorId(this.getUuid()));
        data.setType(type);
        data.setScope(scope);
        data.setName(this.getName());
        data.setClazz(this.getClazz());
        data.setActions(this.getActions());
        data.setConfigurationDescriptor(configurationDescriptor);
        return data;
    }

    public String getSearchText() {
        return searchText;
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public String getSearchTextSource() {
        return getSearchText();
    }
}
