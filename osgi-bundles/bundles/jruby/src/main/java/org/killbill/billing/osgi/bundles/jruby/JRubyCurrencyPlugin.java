/*
 * Copyright 2010-2013 Ning, Inc.
 * Copyright 2014 Groupon, Inc
 * Copyright 2014 The Billing Project, LLC
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
import java.util.Hashtable;
import java.util.Set;
import java.util.SortedSet;

import org.joda.time.DateTime;
import org.jruby.Ruby;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.currency.api.Rate;
import org.killbill.billing.currency.plugin.api.CurrencyPluginApi;
import org.killbill.billing.osgi.api.OSGIPluginProperties;
import org.killbill.billing.osgi.api.config.PluginRubyConfig;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

public class JRubyCurrencyPlugin extends JRubyPlugin implements CurrencyPluginApi {

    private volatile ServiceRegistration currencyPluginRegistration;

    public JRubyCurrencyPlugin(final PluginRubyConfig config, final BundleContext bundleContext, final LogService logger) {
        super(config, bundleContext, logger);
    }

    @Override
    public void startPlugin(final BundleContext context) {
        super.startPlugin(context);

        final Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("name", pluginMainClass);
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, pluginGemName);
        currencyPluginRegistration = context.registerService(CurrencyPluginApi.class.getName(), this, props);
    }

    @Override
    public void stopPlugin(final BundleContext context) {
        if (currencyPluginRegistration != null) {
            currencyPluginRegistration.unregister();
        }
        super.stopPlugin(context);
    }

    @Override
    public Set<Currency> getBaseCurrencies() {
        try {
            return callWithRuntimeAndChecking(new PluginCallback<Set<Currency>>(VALIDATION_PLUGIN_TYPE.CURRENCY) {
                @Override
                public Set<Currency> doCall(final Ruby runtime) throws PaymentPluginApiException {
                    return ((CurrencyPluginApi) pluginInstance).getBaseCurrencies();
                }
            });
        } catch (final PaymentPluginApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DateTime getLatestConversionDate(final Currency currency) {
        try {
            return callWithRuntimeAndChecking(new PluginCallback<DateTime>(VALIDATION_PLUGIN_TYPE.CURRENCY) {
                @Override
                public DateTime doCall(final Ruby runtime) throws PaymentPluginApiException {
                    return ((CurrencyPluginApi) pluginInstance).getLatestConversionDate(currency);
                }
            });
        } catch (final PaymentPluginApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SortedSet<DateTime> getConversionDates(final Currency currency) {
        try {
            return callWithRuntimeAndChecking(new PluginCallback<SortedSet<DateTime>>(VALIDATION_PLUGIN_TYPE.CURRENCY) {
                @Override
                public SortedSet<DateTime> doCall(final Ruby runtime) throws PaymentPluginApiException {
                    return ((CurrencyPluginApi) pluginInstance).getConversionDates(currency);
                }
            });
        } catch (final PaymentPluginApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Rate> getCurrentRates(final Currency currency) {
        try {
            return callWithRuntimeAndChecking(new PluginCallback<Set<Rate>>(VALIDATION_PLUGIN_TYPE.CURRENCY) {
                @Override
                public Set<Rate> doCall(final Ruby runtime) throws PaymentPluginApiException {
                    return ((CurrencyPluginApi) pluginInstance).getCurrentRates(currency);
                }
            });
        } catch (final PaymentPluginApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Rate> getRates(final Currency currency, final DateTime time) {
        try {
            return callWithRuntimeAndChecking(new PluginCallback<Set<Rate>>(VALIDATION_PLUGIN_TYPE.CURRENCY) {
                @Override
                public Set<Rate> doCall(final Ruby runtime) throws PaymentPluginApiException {
                    return ((CurrencyPluginApi) pluginInstance).getRates(currency, time);
                }
            });
        } catch (final PaymentPluginApiException e) {
            throw new RuntimeException(e);
        }
    }
}
