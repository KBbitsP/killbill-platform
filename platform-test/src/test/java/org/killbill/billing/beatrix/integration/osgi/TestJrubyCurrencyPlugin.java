/*
 * Copyright 2010-2013 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
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

import org.joda.time.DateTime;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.currency.api.Rate;
import org.killbill.billing.currency.plugin.api.CurrencyPluginApi;
import org.killbill.billing.osgi.api.OSGIServiceRegistration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Test(enabled = false)
public class TestJrubyCurrencyPlugin extends TestOSGIBase {

    private final String BUNDLE_TEST_RESOURCE_PREFIX = "killbill-currency-plugin-test";
    private final String BUNDLE_TEST_RESOURCE = BUNDLE_TEST_RESOURCE_PREFIX + ".tar.gz";

    @Inject
    private OSGIServiceRegistration<CurrencyPluginApi> currencyPluginApiOSGIServiceRegistration;

    @BeforeClass(groups = "slow")
    public void beforeClass() throws Exception {

        // OSGIDataSourceConfig
//        super.beforeClass();

        // This is extracted from surefire system configuration-- needs to be added explicitly in IntelliJ for correct running
        final String killbillVersion = System.getProperty("killbill.version");

//        SetupBundleWithAssertion setupTest = new SetupBundleWithAssertion(BUNDLE_TEST_RESOURCE, osgiConfig, killbillVersion);
//        setupTest.setupJrubyBundle();
    }

    @Test(groups = "slow")
    public void testCurrencyApis() throws Exception {

        CurrencyPluginApi api = getTestPluginCurrencyApi();

        final Set<Currency> currencies = api.getBaseCurrencies();
        assertEquals(currencies.size(), 1);
        assertEquals(currencies.iterator().next(), Currency.USD);

        final DateTime res = api.getLatestConversionDate(Currency.USD);
        assertNotNull(res);

        final Set<Rate> rates = api.getCurrentRates(Currency.USD);
        assertEquals(rates.size(), 1);
        final Rate theRate = rates.iterator().next();
        assertEquals(theRate.getBaseCurrency(), Currency.USD);
        assertEquals(theRate.getCurrency(), Currency.BRL);
        Assert.assertTrue(theRate.getValue().compareTo(new BigDecimal("12.3")) == 0);

    }

    private CurrencyPluginApi getTestPluginCurrencyApi() {
        int retry = 5;

        // It is expected to have a nul result if the initialization of Killbill went faster than the registration of the plugin services
        CurrencyPluginApi result = null;
        do {
            result = currencyPluginApiOSGIServiceRegistration.getServiceForName(BUNDLE_TEST_RESOURCE_PREFIX);
            if (result == null) {
                try {
//                    log.info("Waiting for Killbill initialization to complete time = " + clock.getUTCNow());
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
            }
        } while (result == null && retry-- > 0);
        Assert.assertNotNull(result);
        return result;
    }

}
