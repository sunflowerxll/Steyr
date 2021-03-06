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
package com.sunflower.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.sunflower.server.common.data.Tenant;
import com.sunflower.server.common.data.exception.ThingsboardException;
import com.sunflower.server.common.data.id.TenantId;
import com.sunflower.server.common.data.page.TextPageData;
import com.sunflower.server.common.data.page.TextPageLink;
import com.sunflower.server.common.data.plugin.ComponentLifecycleEvent;
import com.sunflower.server.dao.tenant.TenantService;
import com.sunflower.server.queue.util.TbCoreComponent;
import com.sunflower.server.service.install.InstallScripts;
import com.sunflower.server.service.security.permission.Operation;
import com.sunflower.server.service.security.permission.Resource;

@RestController
@TbCoreComponent
@RequestMapping("/api")
@Slf4j
public class TenantController extends BaseController {

    @Autowired
    private InstallScripts installScripts;

    @Autowired
    private TenantService tenantService;

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/{tenantId}", method = RequestMethod.GET)
    @ResponseBody
    public Tenant getTenantById(@PathVariable("tenantId") String strTenantId) throws ThingsboardException {
        checkParameter("tenantId", strTenantId);
        try {
            TenantId tenantId = new TenantId(toUUID(strTenantId));
            checkTenantId(tenantId, Operation.READ);
            return checkNotNull(tenantService.findTenantById(tenantId));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenant", method = RequestMethod.POST)
    @ResponseBody
    public Tenant saveTenant(@RequestBody Tenant tenant) throws ThingsboardException {
        try {
            boolean newTenant = tenant.getId() == null;

            Operation operation = newTenant ? Operation.CREATE : Operation.WRITE;

            accessControlService.checkPermission(getCurrentUser(), Resource.TENANT, operation,
                    tenant.getId(), tenant);
            tenant = checkNotNull(tenantService.saveTenant(tenant));
            if (newTenant) {
                installScripts.createDefaultRuleChains(tenant.getId());
            }
            return tenant;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenant/{tenantId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteTenant(@PathVariable("tenantId") String strTenantId) throws ThingsboardException {
        checkParameter("tenantId", strTenantId);
        try {
            TenantId tenantId = new TenantId(toUUID(strTenantId));
            checkTenantId(tenantId, Operation.DELETE);
            tenantService.deleteTenant(tenantId);
            tbClusterService.onEntityStateChange(tenantId, tenantId, ComponentLifecycleEvent.DELETED);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenants", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Tenant> getTenants(@RequestParam int limit,
                                           @RequestParam(required = false) String textSearch,
                                           @RequestParam(required = false) String idOffset,
                                           @RequestParam(required = false) String textOffset) throws ThingsboardException {
        try {
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            return checkNotNull(tenantService.findTenants(pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
