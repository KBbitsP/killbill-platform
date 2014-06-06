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

import com.google.common.collect.ImmutableList;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.api.OSGIServiceRegistration;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.plugin.api.PaymentPluginApi;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiWithTestControl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.UUID;

public class TestPaymentOSGIWithTestPaymentBundle extends TestOSGIBase {

    // Same name the osgi-payment-test plugin uses to register its service
    public static final String OSGI_PLUGIN_NAME = "osgi-payment-plugin";

    private final String BUNDLE_TEST_RESOURCE = "killbill-osgi-bundles-test-payment";

    @Inject
    private OSGIServiceRegistration<PaymentPluginApi> paymentPluginApiOSGIServiceRegistration;

    @BeforeClass(groups = "slow")
    public void beforeClass() throws Exception {

//        super.beforeClass();

        // This is extracted from surefire system configuration-- needs to be added explicitly in IntelliJ for correct running
        final String killbillVersion = System.getProperty("killbill.version");
//        final SetupBundleWithAssertion setupTest = new SetupBundleWithAssertion(BUNDLE_TEST_RESOURCE, osgiConfig, killbillVersion);
//        setupTest.setupJavaBundle();

    }

    @BeforeMethod(groups = "slow")
    public void beforeMethod() throws Exception {
//        super.beforeMethod();
        getTestPluginPaymentApi().resetToNormalbehavior();
    }

    @Test(groups = "slow", enabled = false)
    public void testBasicProcessPaymentOK() throws Exception {
        final PaymentPluginApiWithTestControl paymentPluginApi = getTestPluginPaymentApi();
        paymentPluginApi.processPayment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, Currency.USD, ImmutableList.<PluginProperty>of(), callContext);
    }

    @Test(groups = "slow")
    public void testBasicProcessPaymentWithPaymentPluginApiException() throws Exception {

        boolean gotException = false;
        try {
            final PaymentPluginApiWithTestControl paymentPluginApi = getTestPluginPaymentApi();
            final PaymentPluginApiException e = new PaymentPluginApiException("test-error", "foo");

            paymentPluginApi.setPaymentPluginApiExceptionOnNextCalls(e);
            paymentPluginApi.processPayment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, Currency.USD, ImmutableList.<PluginProperty>of(), callContext);
            Assert.fail("Expected to fail with " + e.toString());
        } catch (final PaymentPluginApiException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
    }

    @Test(groups = "slow")
    public void testBasicProcessPaymentWithRuntimeException() throws Exception {

        boolean gotException = false;
        try {
            final PaymentPluginApiWithTestControl paymentPluginApi = getTestPluginPaymentApi();
            final RuntimeException e = new RuntimeException("test-error");

            paymentPluginApi.setPaymentRuntimeExceptionOnNextCalls(e);
            paymentPluginApi.processPayment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, Currency.USD, ImmutableList.<PluginProperty>of(), callContext);
            Assert.fail("Expected to fail with " + e.toString());
        } catch (final RuntimeException e) {
            gotException = true;
        }
        Assert.assertTrue(gotException);
    }

    @Test(groups = "slow")
    public void testIntegrationOK() throws Exception {
        setupIntegration(null, null);
    }

    @Test(groups = "slow")
    public void testIntegrationWithPaymentPluginApiException() throws Exception {
        final PaymentPluginApiException e = new PaymentPluginApiException("test-error", "foo");
        setupIntegration(e, null);
    }

    @Test(groups = "slow")
    public void testIntegrationWithRuntimeException() throws Exception {
        final RuntimeException e = new RuntimeException("test-error");
        setupIntegration(null, e);
    }

    private void setupIntegration(final PaymentPluginApiException expectedException, final RuntimeException expectedRuntimeException) throws Exception {

        final PaymentPluginApiWithTestControl paymentPluginApi = getTestPluginPaymentApi();

//        final AccountData accountData = getAccountData(1);
//        final Account account = createAccountWithOsgiPaymentMethod(accountData);
/*
        // We take april as it has 30 days (easier to play with BCD)
        // Set clock to the initial start date - we implicitly assume here that the account timezone is UTC
        clock.setDay(new LocalDate(2012, 4, 1));
        //
        // CREATE SUBSCRIPTION AND EXPECT BOTH EVENTS: NextEvent.CREATE NextEvent.INVOICE
        //
        final DefaultEntitlement baseEntitlement = createBaseEntitlementAndCheckForCompletion(account.getId(), "externalKey", "Shotgun", ProductCategory.BASE, BillingPeriod.MONTHLY, NextEvent.CREATE, NextEvent.INVOICE);
        //
        // ADD ADD_ON ON THE SAME DAY TO TRIGGER PAYMENT
        //

        final List<NextEvent> expectedEvents = new LinkedList<NextEvent>();
        expectedEvents.add(NextEvent.CREATE);
        expectedEvents.add(NextEvent.INVOICE);
        if (expectedException == null && expectedRuntimeException == null) {
            expectedEvents.add(NextEvent.PAYMENT);
        } else if (expectedException != null) {
            expectedEvents.add(NextEvent.PAYMENT_PLUGIN_ERROR);
            paymentPluginApi.setPaymentPluginApiExceptionOnNextCalls(expectedException);
        } else if (expectedRuntimeException != null) {
            expectedEvents.add(NextEvent.PAYMENT_PLUGIN_ERROR);
            paymentPluginApi.setPaymentRuntimeExceptionOnNextCalls(expectedRuntimeException);
        }

        final DefaultEntitlement aoEntitlement = addAOEntitlementAndCheckForCompletion(baseEntitlement.getBundleId(), "Telescopic-Scope", ProductCategory.ADD_ON, BillingPeriod.MONTHLY,
                                                                                       expectedEvents.toArray(new NextEvent[expectedEvents.size()]));

        final Invoice invoice = invoiceChecker.checkInvoice(account.getId(), 2, callContext, new ExpectedInvoiceItemCheck(new LocalDate(2012, 4, 1), new LocalDate(2012, 5, 1), InvoiceItemType.RECURRING, new BigDecimal("399.95")));

        if (expectedException == null && expectedRuntimeException == null) {
            paymentChecker.checkPayment(account.getId(), 1, callContext, new ExpectedPaymentCheck(new LocalDate(2012, 4, 1), new BigDecimal("399.95"), PaymentStatus.SUCCESS, invoice.getId(), Currency.USD));
        } else if (expectedException != null) {
            paymentChecker.checkPayment(account.getId(), 1, callContext, new ExpectedPaymentCheck(new LocalDate(2012, 4, 1), new BigDecimal("399.95"), PaymentStatus.PLUGIN_FAILURE, invoice.getId(), Currency.USD));
        } else if (expectedRuntimeException != null) {
            paymentChecker.checkPayment(account.getId(), 1, callContext, new ExpectedPaymentCheck(new LocalDate(2012, 4, 1), new BigDecimal("399.95"), PaymentStatus.PLUGIN_FAILURE, invoice.getId(), Currency.USD));
        }
        */
    }

    private PaymentPluginApiWithTestControl getTestPluginPaymentApi() {
        final PaymentPluginApiWithTestControl result = (PaymentPluginApiWithTestControl) paymentPluginApiOSGIServiceRegistration.getServiceForName(OSGI_PLUGIN_NAME);
        Assert.assertNotNull(result);
        return result;
    }
}
