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

import java.math.BigDecimal;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.jruby.Ruby;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.api.OSGIPluginProperties;
import org.killbill.billing.osgi.api.config.PluginRubyConfig;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.plugin.api.GatewayNotification;
import org.killbill.billing.payment.plugin.api.HostedPaymentPageFormDescriptor;
import org.killbill.billing.payment.plugin.api.PaymentInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentPluginApi;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.payment.plugin.api.PaymentInfoPlugin;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.util.entity.Pagination;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

public class JRubyPaymentPlugin extends JRubyPlugin implements PaymentPluginApi {

    private volatile ServiceRegistration paymentInfoPluginRegistration;

    public JRubyPaymentPlugin(final PluginRubyConfig config, final BundleContext bundleContext, final LogService logger) {
        super(config, bundleContext, logger);
    }

    @Override
    public void startPlugin(final BundleContext context) {
        super.startPlugin(context);

        final Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("name", pluginMainClass);
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, pluginGemName);
        paymentInfoPluginRegistration = context.registerService(PaymentPluginApi.class.getName(), this, props);
    }

    @Override
    public void stopPlugin(final BundleContext context) {
        if (paymentInfoPluginRegistration != null) {
            paymentInfoPluginRegistration.unregister();
        }
        super.stopPlugin(context);
    }

    @Override
    public PaymentInfoPlugin authorizePayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<PaymentInfoPlugin>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public PaymentInfoPlugin doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).authorizePayment(kbAccountId, kbPaymentId, kbPaymentMethodId, amount, currency, properties, context);
            }
        });
    }

    @Override
    public PaymentInfoPlugin capturePayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<PaymentInfoPlugin>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public PaymentInfoPlugin doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).capturePayment(kbAccountId, kbPaymentId, kbPaymentMethodId, amount, currency, properties, context);
            }
        });
    }

    @Override
    public PaymentInfoPlugin processPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<PaymentInfoPlugin>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public PaymentInfoPlugin doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).processPayment(kbAccountId, kbPaymentId, kbPaymentMethodId, amount, currency, properties, context);
            }
        });
    }

    @Override
    public PaymentInfoPlugin voidPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<PaymentInfoPlugin>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public PaymentInfoPlugin doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).voidPayment(kbAccountId, kbPaymentId, kbPaymentMethodId, properties, context);
            }
        });
    }

    @Override
    public PaymentInfoPlugin creditPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<PaymentInfoPlugin>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public PaymentInfoPlugin doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).creditPayment(kbAccountId, kbPaymentId, kbPaymentMethodId, amount, currency, properties, context);
            }
        });
    }

    @Override
    public PaymentInfoPlugin getPaymentInfo(final UUID kbAccountId, final UUID kbPaymentId, final Iterable<PluginProperty> properties, final TenantContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<PaymentInfoPlugin>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public PaymentInfoPlugin doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).getPaymentInfo(kbAccountId, kbPaymentId, properties, context);
            }
        });
    }

    @Override
    public Pagination<PaymentInfoPlugin> searchPayments(final String searchKey, final Long offset, final Long limit, final Iterable<PluginProperty> properties, final TenantContext tenantContext) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<Pagination<PaymentInfoPlugin>>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public Pagination<PaymentInfoPlugin> doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).searchPayments(searchKey, offset, limit, properties, tenantContext);
            }
        });
    }

    @Override
    public PaymentInfoPlugin processRefund(final UUID kbAccountId, final UUID kbPaymentId, final BigDecimal refundAmount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<PaymentInfoPlugin>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public PaymentInfoPlugin doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).processRefund(kbAccountId, kbPaymentId, refundAmount, currency, properties, context);
            }
        });

    }

    @Override
    public List<PaymentInfoPlugin> getRefundInfo(final UUID kbAccountId, final UUID kbPaymentId, final Iterable<PluginProperty> properties, final TenantContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<List<PaymentInfoPlugin>>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public List<PaymentInfoPlugin> doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).getRefundInfo(kbAccountId, kbPaymentId, properties, context);
            }
        });
    }

    @Override
    public Pagination<PaymentInfoPlugin> searchRefunds(final String searchKey, final Long offset, final Long limit, final Iterable<PluginProperty> properties, final TenantContext tenantContext) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<Pagination<PaymentInfoPlugin>>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public Pagination<PaymentInfoPlugin> doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).searchRefunds(searchKey, offset, limit, properties, tenantContext);
            }
        });
    }

    @Override
    public void addPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final PaymentMethodPlugin paymentMethodProps, final boolean setDefault, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        callWithRuntimeAndChecking(new PluginCallback(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public Void doCall(final Ruby runtime) throws PaymentPluginApiException {
                ((PaymentPluginApi) pluginInstance).addPaymentMethod(kbAccountId, kbPaymentMethodId, paymentMethodProps, Boolean.valueOf(setDefault), properties, context);
                return null;
            }
        });
    }

    @Override
    public void deletePaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        callWithRuntimeAndChecking(new PluginCallback(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public Void doCall(final Ruby runtime) throws PaymentPluginApiException {
                ((PaymentPluginApi) pluginInstance).deletePaymentMethod(kbAccountId, kbPaymentMethodId, properties, context);
                return null;
            }
        });
    }

    @Override
    public PaymentMethodPlugin getPaymentMethodDetail(final UUID kbAccountId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties, final TenantContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<PaymentMethodPlugin>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public PaymentMethodPlugin doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).getPaymentMethodDetail(kbAccountId, kbPaymentMethodId, properties, context);
            }
        });
    }

    @Override
    public void setDefaultPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        callWithRuntimeAndChecking(new PluginCallback(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public Void doCall(final Ruby runtime) throws PaymentPluginApiException {
                ((PaymentPluginApi) pluginInstance).setDefaultPaymentMethod(kbAccountId, kbPaymentMethodId, properties, context);
                return null;
            }
        });
    }

    @Override
    public List<PaymentMethodInfoPlugin> getPaymentMethods(final UUID kbAccountId, final boolean refreshFromGateway, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<List<PaymentMethodInfoPlugin>>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public List<PaymentMethodInfoPlugin> doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).getPaymentMethods(kbAccountId, Boolean.valueOf(refreshFromGateway), properties, context);
            }
        });
    }

    @Override
    public Pagination<PaymentMethodPlugin> searchPaymentMethods(final String searchKey, final Long offset, final Long limit, final Iterable<PluginProperty> properties, final TenantContext tenantContext) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<Pagination<PaymentMethodPlugin>>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public Pagination<PaymentMethodPlugin> doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).searchPaymentMethods(searchKey, offset, limit, properties, tenantContext);
            }
        });
    }

    @Override
    public void resetPaymentMethods(final UUID kbAccountId, final List<PaymentMethodInfoPlugin> paymentMethods, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        callWithRuntimeAndChecking(new PluginCallback(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public Void doCall(final Ruby runtime) throws PaymentPluginApiException {
                ((PaymentPluginApi) pluginInstance).resetPaymentMethods(kbAccountId, paymentMethods, properties, context);
                return null;
            }
        });
    }

    @Override
    public HostedPaymentPageFormDescriptor buildFormDescriptor(final UUID kbAccountId, final Iterable<PluginProperty> customFields, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<HostedPaymentPageFormDescriptor>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public HostedPaymentPageFormDescriptor doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).buildFormDescriptor(kbAccountId, customFields, properties, context);
            }
        });
    }

    @Override
    public GatewayNotification processNotification(final String notification, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return callWithRuntimeAndChecking(new PluginCallback<GatewayNotification>(VALIDATION_PLUGIN_TYPE.PAYMENT) {
            @Override
            public GatewayNotification doCall(final Ruby runtime) throws PaymentPluginApiException {
                return ((PaymentPluginApi) pluginInstance).processNotification(notification, properties, context);
            }
        });
    }
}
