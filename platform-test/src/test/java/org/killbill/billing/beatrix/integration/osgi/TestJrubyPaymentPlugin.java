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

package org.killbill.billing.beatrix.integration.osgi;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.killbill.billing.beatrix.integration.osgi.util.SetupBundleWithAssertion;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentPluginApi;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

import static org.testng.Assert.assertEquals;

public class TestJrubyPaymentPlugin extends TestOSGIBase {

    private static final String BUNDLE_TEST_RESOURCE_PREFIX = "killbill-payment-test";
    private static final String BUNDLE_TEST_RESOURCE = BUNDLE_TEST_RESOURCE_PREFIX + ".tar.gz";

    @BeforeClass(groups = "slow")
    public void beforeClass() throws Exception {
        super.beforeClass();

        final String killbillVersion = System.getProperty("killbill.version");
        final SetupBundleWithAssertion setupTest = new SetupBundleWithAssertion(BUNDLE_TEST_RESOURCE, osgiConfig, killbillVersion);
        setupTest.setupJrubyBundle();
    }

    @Test(groups = "slow")
    public void testProcessPayment() throws Exception {
        final PaymentPluginApi api = getTestApi(paymentPluginApiOSGIServiceRegistration, BUNDLE_TEST_RESOURCE_PREFIX);

        // TODO change plugin not to retrieve the account
        final DateTime beforeCall = new DateTime().toDateTime(DateTimeZone.UTC).minusSeconds(1);
        final PaymentTransactionInfoPlugin res = api.processPayment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, Currency.USD, ImmutableList.<PluginProperty>of(), callContext);
        final DateTime afterCall = new DateTime().toDateTime(DateTimeZone.UTC).plusSeconds(1);

        Assert.assertTrue(res.getAmount().compareTo(BigDecimal.TEN) == 0);
        Assert.assertTrue(res.getCreatedDate().compareTo(beforeCall) >= 0);
        Assert.assertTrue(res.getCreatedDate().compareTo(afterCall) <= 0);

        Assert.assertTrue(res.getEffectiveDate().compareTo(beforeCall) >= 0);
        Assert.assertTrue(res.getEffectiveDate().compareTo(afterCall) <= 0);

        assertEquals(res.getGatewayError(), "gateway_error");
        assertEquals(res.getGatewayErrorCode(), "gateway_error_code");

        assertEquals(res.getStatus(), PaymentPluginStatus.PROCESSED);
    }

    @Test(groups = "slow")
    public void testGetPaymentInfo() throws Exception {
        final PaymentPluginApi api = getTestApi(paymentPluginApiOSGIServiceRegistration, BUNDLE_TEST_RESOURCE_PREFIX);

        final DateTime beforeCall = new DateTime().toDateTime(DateTimeZone.UTC).minusSeconds(1);
        final PaymentTransactionInfoPlugin res = api.getPaymentInfo(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.<PluginProperty>of(), callContext).get(0);
        final DateTime afterCall = new DateTime().toDateTime(DateTimeZone.UTC).plusSeconds(1);

        Assert.assertTrue(res.getAmount().compareTo(BigDecimal.ZERO) == 0);
        Assert.assertTrue(res.getCreatedDate().compareTo(beforeCall) >= 0);
        Assert.assertTrue(res.getCreatedDate().compareTo(afterCall) <= 0);

        Assert.assertTrue(res.getEffectiveDate().compareTo(beforeCall) >= 0);
        Assert.assertTrue(res.getEffectiveDate().compareTo(afterCall) <= 0);

        assertEquals(res.getGatewayError(), "gateway_error");
        assertEquals(res.getGatewayErrorCode(), "gateway_error_code");

        assertEquals(res.getStatus(), PaymentPluginStatus.PROCESSED);
    }

    @Test(groups = "slow")
    public void testProcessRefund() throws Exception {
        final PaymentPluginApi api = getTestApi(paymentPluginApiOSGIServiceRegistration, BUNDLE_TEST_RESOURCE_PREFIX);

        final DateTime beforeCall = new DateTime().toDateTime(DateTimeZone.UTC).minusSeconds(1);
        final PaymentTransactionInfoPlugin res = api.processRefund(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, Currency.USD, ImmutableList.<PluginProperty>of(), callContext);
        final DateTime afterCall = new DateTime().toDateTime(DateTimeZone.UTC).plusSeconds(1);

        Assert.assertTrue(res.getAmount().compareTo(BigDecimal.TEN) == 0);
        Assert.assertTrue(res.getCreatedDate().compareTo(beforeCall) >= 0);
        Assert.assertTrue(res.getCreatedDate().compareTo(afterCall) <= 0);

        Assert.assertTrue(res.getEffectiveDate().compareTo(beforeCall) >= 0);
        Assert.assertTrue(res.getEffectiveDate().compareTo(afterCall) <= 0);

        assertEquals(res.getGatewayError(), "gateway_error");
        assertEquals(res.getGatewayErrorCode(), "gateway_error_code");

        assertEquals(res.getStatus(), PaymentPluginStatus.PROCESSED);
    }

    @Test(groups = "slow")
    public void testAddPaymentMethod() throws Exception {
        final PaymentPluginApi api = getTestApi(paymentPluginApiOSGIServiceRegistration, BUNDLE_TEST_RESOURCE_PREFIX);

        final PaymentMethodPlugin paymentMethodPlugin = Mockito.mock(PaymentMethodPlugin.class);
        Mockito.when(paymentMethodPlugin.getExternalPaymentMethodId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getType()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getCCName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getCCType()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getCCExpirationMonth()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getCCExpirationYear()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getCCLast4()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getAddress1()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getAddress2()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getCity()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getState()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getZip()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.getCountry()).thenReturn(UUID.randomUUID().toString());

        api.addPaymentMethod(UUID.randomUUID(), UUID.randomUUID(), paymentMethodPlugin, true, ImmutableList.<PluginProperty>of(), callContext);
    }

    @Test(groups = "slow")
    public void testDeletePaymentMethod() throws Exception {
        final PaymentPluginApi api = getTestApi(paymentPluginApiOSGIServiceRegistration, BUNDLE_TEST_RESOURCE_PREFIX);
        api.deletePaymentMethod(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.<PluginProperty>of(), callContext);
    }

    @Test(groups = "slow")
    public void testGetPaymentMethodDetail() throws Exception {
        final PaymentPluginApi api = getTestApi(paymentPluginApiOSGIServiceRegistration, BUNDLE_TEST_RESOURCE_PREFIX);
        final PaymentMethodPlugin res = api.getPaymentMethodDetail(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.<PluginProperty>of(), callContext);

        assertEquals(res.getExternalPaymentMethodId(), "external_payment_method_id");
        Assert.assertTrue(res.isDefaultPaymentMethod());
        assertEquals(res.getProperties().size(), 2);
        assertEquals(res.getProperties().get(0).getKey(), "key1");
        assertEquals(res.getProperties().get(0).getValue(), "value1");
        assertEquals(res.getProperties().get(1).getKey(), "key2");
        assertEquals(res.getProperties().get(1).getValue(), "value2");
    }

    @Test(groups = "slow")
    public void testSetDefaultPaymentMethod() throws Exception {
        final PaymentPluginApi api = getTestApi(paymentPluginApiOSGIServiceRegistration, BUNDLE_TEST_RESOURCE_PREFIX);
        api.setDefaultPaymentMethod(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.<PluginProperty>of(), callContext);
    }

    @Test(groups = "slow")
    public void testGetPaymentMethods() throws Exception {
        final PaymentPluginApi api = getTestApi(paymentPluginApiOSGIServiceRegistration, BUNDLE_TEST_RESOURCE_PREFIX);
        final UUID kbAccountId = UUID.randomUUID();
        final List<PaymentMethodInfoPlugin> res = api.getPaymentMethods(kbAccountId, true, ImmutableList.<PluginProperty>of(), callContext);

        assertEquals(res.size(), 1);

        final PaymentMethodInfoPlugin res0 = res.get(0);
        Assert.assertTrue(res0.isDefault());
        assertEquals(res0.getExternalPaymentMethodId(), "external_payment_method_id");
        assertEquals(res0.getAccountId(), kbAccountId);
        assertEquals(res0.getPaymentMethodId(), kbAccountId);
    }
}
