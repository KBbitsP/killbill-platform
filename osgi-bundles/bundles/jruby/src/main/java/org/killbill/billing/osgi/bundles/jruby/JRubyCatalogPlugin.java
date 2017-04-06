/*
 * Copyright 2015 Groupon, Inc
 * Copyright 2015 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.osgi.bundles.jruby;

import java.util.Dictionary;

import org.joda.time.DateTime;
import org.jruby.Ruby;
import org.killbill.billing.catalog.plugin.api.CatalogPluginApi;
import org.killbill.billing.catalog.plugin.api.VersionedPluginCatalog;
import org.killbill.billing.osgi.api.config.PluginRubyConfig;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

public class JRubyCatalogPlugin extends JRubyNotificationPlugin implements CatalogPluginApi {

    public JRubyCatalogPlugin(final PluginRubyConfig config, final BundleContext bundleContext, final LogService logger, final OSGIConfigPropertiesService configProperties) {
        super(config, bundleContext, logger, configProperties);
    }

    @Override
    protected ServiceRegistration doRegisterService(final BundleContext context, final Dictionary<String, Object> props) {
        return context.registerService(CatalogPluginApi.class.getName(), this, props);
    }

    @Override
    public DateTime getLatestCatalogVersion(final Iterable<PluginProperty> iterable, final TenantContext tenantContext) {
        return null;
    }

    @Override
    public VersionedPluginCatalog getVersionedPluginCatalog(final Iterable<PluginProperty> pluginProperties, final TenantContext tenantContext) {
        return callWithRuntimeAndChecking(new PluginCallback<VersionedPluginCatalog, RuntimeException>() {
            @Override
            public VersionedPluginCatalog doCall(final Ruby runtime) throws RuntimeException {
                return ((CatalogPluginApi) pluginInstance).getVersionedPluginCatalog(pluginProperties, tenantContext);
            }
        });
    }
}
