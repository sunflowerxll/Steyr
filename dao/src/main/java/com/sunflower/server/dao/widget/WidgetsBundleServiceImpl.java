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
package com.sunflower.server.dao.widget;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sunflower.server.common.data.Tenant;
import com.sunflower.server.common.data.id.TenantId;
import com.sunflower.server.common.data.id.WidgetsBundleId;
import com.sunflower.server.common.data.page.TextPageData;
import com.sunflower.server.common.data.page.TextPageLink;
import com.sunflower.server.common.data.widget.WidgetsBundle;
import com.sunflower.server.dao.exception.DataValidationException;
import com.sunflower.server.dao.exception.IncorrectParameterException;
import com.sunflower.server.dao.model.ModelConstants;
import com.sunflower.server.dao.service.DataValidator;
import com.sunflower.server.dao.service.PaginatedRemover;
import com.sunflower.server.dao.service.Validator;
import com.sunflower.server.dao.tenant.TenantDao;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WidgetsBundleServiceImpl implements WidgetsBundleService {

    private static final int DEFAULT_WIDGETS_BUNDLE_LIMIT = 300;
    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_PAGE_LINK = "Incorrect page link ";

    @Autowired
    private WidgetsBundleDao widgetsBundleDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private WidgetTypeService widgetTypeService;

    @Override
    public WidgetsBundle findWidgetsBundleById(TenantId tenantId, WidgetsBundleId widgetsBundleId) {
        log.trace("Executing findWidgetsBundleById [{}]", widgetsBundleId);
        Validator.validateId(widgetsBundleId, "Incorrect widgetsBundleId " + widgetsBundleId);
        return widgetsBundleDao.findById(tenantId, widgetsBundleId.getId());
    }

    @Override
    public WidgetsBundle saveWidgetsBundle(WidgetsBundle widgetsBundle) {
        log.trace("Executing saveWidgetsBundle [{}]", widgetsBundle);
        widgetsBundleValidator.validate(widgetsBundle, WidgetsBundle::getTenantId);
        return widgetsBundleDao.save(widgetsBundle.getTenantId(), widgetsBundle);
    }

    @Override
    public void deleteWidgetsBundle(TenantId tenantId, WidgetsBundleId widgetsBundleId) {
        log.trace("Executing deleteWidgetsBundle [{}]", widgetsBundleId);
        Validator.validateId(widgetsBundleId, "Incorrect widgetsBundleId " + widgetsBundleId);
        WidgetsBundle widgetsBundle = findWidgetsBundleById(tenantId, widgetsBundleId);
        if (widgetsBundle == null) {
            throw new IncorrectParameterException("Unable to delete non-existent widgets bundle.");
        }
        widgetTypeService.deleteWidgetTypesByTenantIdAndBundleAlias(widgetsBundle.getTenantId(), widgetsBundle.getAlias());
        widgetsBundleDao.removeById(tenantId, widgetsBundleId.getId());
    }

    @Override
    public WidgetsBundle findWidgetsBundleByTenantIdAndAlias(TenantId tenantId, String alias) {
        log.trace("Executing findWidgetsBundleByTenantIdAndAlias, tenantId [{}], alias [{}]", tenantId, alias);
        Validator.validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        Validator.validateString(alias, "Incorrect alias " + alias);
        return widgetsBundleDao.findWidgetsBundleByTenantIdAndAlias(tenantId.getId(), alias);
    }

    @Override
    public TextPageData<WidgetsBundle> findSystemWidgetsBundlesByPageLink(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findSystemWidgetsBundles, pageLink [{}]", pageLink);
        Validator.validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        return new TextPageData<>(widgetsBundleDao.findSystemWidgetsBundles(tenantId, pageLink), pageLink);
    }

    @Override
    public List<WidgetsBundle> findSystemWidgetsBundles(TenantId tenantId) {
        log.trace("Executing findSystemWidgetsBundles");
        List<WidgetsBundle> widgetsBundles = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(DEFAULT_WIDGETS_BUNDLE_LIMIT);
        TextPageData<WidgetsBundle> pageData;
        do {
            pageData = findSystemWidgetsBundlesByPageLink(tenantId, pageLink);
            widgetsBundles.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());
        return widgetsBundles;
    }

    @Override
    public TextPageData<WidgetsBundle> findTenantWidgetsBundlesByTenantId(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findTenantWidgetsBundlesByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        Validator.validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        Validator.validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        return new TextPageData<>(widgetsBundleDao.findTenantWidgetsBundlesByTenantId(tenantId.getId(), pageLink), pageLink);
    }

    @Override
    public TextPageData<WidgetsBundle> findAllTenantWidgetsBundlesByTenantIdAndPageLink(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findAllTenantWidgetsBundlesByTenantIdAndPageLink, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        Validator.validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        Validator.validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        return new TextPageData<>(widgetsBundleDao.findAllTenantWidgetsBundlesByTenantId(tenantId.getId(), pageLink), pageLink);
    }

    @Override
    public List<WidgetsBundle> findAllTenantWidgetsBundlesByTenantId(TenantId tenantId) {
        log.trace("Executing findAllTenantWidgetsBundlesByTenantId, tenantId [{}]", tenantId);
        Validator.validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        List<WidgetsBundle> widgetsBundles = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(DEFAULT_WIDGETS_BUNDLE_LIMIT);
        TextPageData<WidgetsBundle> pageData;
        do {
            pageData = findAllTenantWidgetsBundlesByTenantIdAndPageLink(tenantId, pageLink);
            widgetsBundles.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());
        return widgetsBundles;
    }

    @Override
    public void deleteWidgetsBundlesByTenantId(TenantId tenantId) {
        log.trace("Executing deleteWidgetsBundlesByTenantId, tenantId [{}]", tenantId);
        Validator.validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        tenantWidgetsBundleRemover.removeEntities(tenantId, tenantId);
    }

    private DataValidator<WidgetsBundle> widgetsBundleValidator =
            new DataValidator<WidgetsBundle>() {

                @Override
                protected void validateDataImpl(TenantId tenantId, WidgetsBundle widgetsBundle) {
                    if (StringUtils.isEmpty(widgetsBundle.getTitle())) {
                        throw new DataValidationException("Widgets bundle title should be specified!");
                    }
                    if (widgetsBundle.getTenantId() == null) {
                        widgetsBundle.setTenantId(new TenantId(ModelConstants.NULL_UUID));
                    }
                    if (!widgetsBundle.getTenantId().getId().equals(ModelConstants.NULL_UUID)) {
                        Tenant tenant = tenantDao.findById(tenantId, widgetsBundle.getTenantId().getId());
                        if (tenant == null) {
                            throw new DataValidationException("Widgets bundle is referencing to non-existent tenant!");
                        }
                    }
                }

                @Override
                protected void validateCreate(TenantId tenantId, WidgetsBundle widgetsBundle) {
                    String alias = widgetsBundle.getAlias();
                    if (alias == null || alias.trim().isEmpty()) {
                        alias = widgetsBundle.getTitle().toLowerCase().replaceAll("\\W+", "_");
                    }
                    String originalAlias = alias;
                    int c = 1;
                    WidgetsBundle withSameAlias;
                    do {
                        withSameAlias = widgetsBundleDao.findWidgetsBundleByTenantIdAndAlias(widgetsBundle.getTenantId().getId(), alias);
                        if (withSameAlias != null) {
                            alias = originalAlias + (++c);
                        }
                    } while(withSameAlias != null);
                    widgetsBundle.setAlias(alias);
                }

                @Override
                protected void validateUpdate(TenantId tenantId, WidgetsBundle widgetsBundle) {
                    WidgetsBundle storedWidgetsBundle = widgetsBundleDao.findById(tenantId, widgetsBundle.getId().getId());
                    if (!storedWidgetsBundle.getTenantId().getId().equals(widgetsBundle.getTenantId().getId())) {
                        throw new DataValidationException("Can't move existing widgets bundle to different tenant!");
                    }
                    if (!storedWidgetsBundle.getAlias().equals(widgetsBundle.getAlias())) {
                        throw new DataValidationException("Update of widgets bundle alias is prohibited!");
                    }
                }

            };

    private PaginatedRemover<TenantId, WidgetsBundle> tenantWidgetsBundleRemover =
            new PaginatedRemover<TenantId, WidgetsBundle>() {

                @Override
                protected List<WidgetsBundle> findEntities(TenantId tenantId, TenantId id, TextPageLink pageLink) {
                    return widgetsBundleDao.findTenantWidgetsBundlesByTenantId(id.getId(), pageLink);
                }

                @Override
                protected void removeEntity(TenantId tenantId, WidgetsBundle entity) {
                    deleteWidgetsBundle(tenantId, new WidgetsBundleId(entity.getUuidId()));
                }
            };

}
