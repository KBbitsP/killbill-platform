/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2021 Equinix, Inc
 * Copyright 2014-2021 The Billing Project, LLC
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

package org.killbill.billing.platform.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.killbill.billing.osgi.api.OSGIConfigProperties;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

import static org.killbill.billing.platform.config.DefaultKillbillConfigSource.ENVIRONMENT_VARIABLE_PREFIX;

public class TestDefaultKillbillConfigSource {

    private static final String ENABLE_JASYPT_PROPERTY = "org.killbill.server.enableJasypt";
    private static final String JASYPT_ENCRYPTOR_PASSWORD_PROPERTY = "JASYPT_ENCRYPTOR_PASSWORD";
    private static final String JASYPT_ENCRYPTOR_ALGORITHM_PROPERTY = "JASYPT_ENCRYPTOR_ALGORITHM";
    private static final String JASYPT_PASSWORD = "top_secret!";
    private static final String JASYPT_ALGORITHM = "PBEWITHMD5ANDDES";
    private static final String ENCRYPTED_PROPERTY_1 = "test.encrypted.property1";
    private static final String ENCRYPTED_PROPERTY_2 = "test.encrypted.property2";

    @BeforeMethod(groups = "fast")
    public void setup() {
        // Clean out the properties we set in the tests,
        // this is only necessary because the DefaultKillBillConfigSource constructor we're using ends up
        // setting this.properties to System.getProperties(), which doesn't automatically get reset between tests.
        System.clearProperty(ENABLE_JASYPT_PROPERTY);
        System.clearProperty(JASYPT_PASSWORD);
        System.clearProperty(JASYPT_ALGORITHM);
        System.clearProperty(ENCRYPTED_PROPERTY_1);
        System.clearProperty(ENCRYPTED_PROPERTY_2);
    }

    @Test(groups = "fast")
    public void testGetProperties() throws URISyntaxException, IOException {
        final Map<String, String> configuration = new HashMap<>();
        configuration.put("1", "A");
        configuration.put("2", "B");

        final OSGIConfigProperties configSource = new DefaultKillbillConfigSource(null, configuration);

        Assert.assertNotNull(configSource.getProperties());
        Assert.assertNotEquals(configSource.getProperties().size(), 0);
        Assert.assertEquals(configSource.getProperties().getProperty("1"), "A");
    }

    @Test(groups = "fast")
    public void testFromEnvVariableName() throws IOException, URISyntaxException {
        final DefaultKillbillConfigSource configSource = new DefaultKillbillConfigSource();

        Assert.assertEquals(configSource.fromEnvVariableName(""), "");
        Assert.assertEquals(configSource.fromEnvVariableName(ENVIRONMENT_VARIABLE_PREFIX + "org_killbill_billing_osgi_dao_prepStmtCacheSize"), "org.killbill.billing.osgi.dao.prepStmtCacheSize");
        // Note! This won't work: we don't support underscores in property keys
        //Assert.assertEquals(configSource.fromEnvVariableName("org_killbill_billing_osgi_dao_prepStmtCacheSize"), "org.killbill.billing.osgi.dao_prepStmtCacheSize");
        Assert.assertEquals(configSource.fromEnvVariableName(ENVIRONMENT_VARIABLE_PREFIX + "org_killbill_billing_osgi_dao__prepStmtCacheSize"), "org.killbill.billing.osgi.dao..prepStmtCacheSize");
    }

    @Test(groups = "fast")
    public void testJasyptDisabledByDefault() throws IOException, URISyntaxException {
        final DefaultKillbillConfigSource configSource = new DefaultKillbillConfigSource();

        final String enableJasyptString = configSource.getString(ENABLE_JASYPT_PROPERTY);

        Assert.assertFalse(Boolean.parseBoolean(enableJasyptString));
    }

    @Test(groups = "fast")
    public void testDecyptionExplicitlyDisabled() throws IOException, URISyntaxException {
        final String unencryptedValue = "myPropertyValue";
        final String encryptedValue = encString(unencryptedValue);

        final Map<String, String> properties = Map.of(ENABLE_JASYPT_PROPERTY, "false",
                                                      ENCRYPTED_PROPERTY_1, encryptedValue,
                                                      JASYPT_ENCRYPTOR_PASSWORD_PROPERTY, JASYPT_PASSWORD,
                                                      JASYPT_ENCRYPTOR_ALGORITHM_PROPERTY, JASYPT_ALGORITHM);

        final DefaultKillbillConfigSource configSource = new DefaultKillbillConfigSource(properties);

        final String actualValue = configSource.getString(ENCRYPTED_PROPERTY_1);

        Assert.assertEquals(encryptedValue, actualValue);
    }

    @Test(groups = "fast", expectedExceptions = IllegalArgumentException.class)
    public void testDecryptEmptyPassword() throws IOException, URISyntaxException {
        final String encryptedValue = encString("myPropertyValue");

        final Map<String, String> properties = Map.of(ENABLE_JASYPT_PROPERTY, "true",
                                                      ENCRYPTED_PROPERTY_1, encryptedValue,
                                                      JASYPT_ENCRYPTOR_PASSWORD_PROPERTY, "",
                                                      JASYPT_ENCRYPTOR_ALGORITHM_PROPERTY, JASYPT_ALGORITHM);

        new DefaultKillbillConfigSource(properties);
    }

    @Test(groups = "fast", expectedExceptions = IllegalArgumentException.class)
    public void testDecryptEmptyAlgorithm() throws IOException, URISyntaxException {
        final String encryptedValue = encString("myPropertyValue");

        final Map<String, String> properties = Map.of(ENABLE_JASYPT_PROPERTY, "true",
                                                      ENCRYPTED_PROPERTY_1, encryptedValue,
                                                      JASYPT_ENCRYPTOR_PASSWORD_PROPERTY, JASYPT_PASSWORD,
                                                      JASYPT_ENCRYPTOR_ALGORITHM_PROPERTY, "");

        new DefaultKillbillConfigSource(properties);
    }

    @Test(groups = "fast", expectedExceptions = EncryptionOperationNotPossibleException.class)
    public void testDecryptInvalidJasyptString() throws IOException, URISyntaxException {
        final String encryptedValue = "ENC(notAValidEncryptedString!)";

        final Map<String, String> properties = Map.of(ENABLE_JASYPT_PROPERTY, "true",
                                                      ENCRYPTED_PROPERTY_1, encryptedValue,
                                                      JASYPT_ENCRYPTOR_PASSWORD_PROPERTY, JASYPT_PASSWORD,
                                                      JASYPT_ENCRYPTOR_ALGORITHM_PROPERTY, JASYPT_ALGORITHM);

        new DefaultKillbillConfigSource(properties);
    }

    @Test(groups = "fast", expectedExceptions = EncryptionOperationNotPossibleException.class)
    public void testDecryptEmptyJasyptString() throws IOException, URISyntaxException {
        final String encryptedValue = "ENC()";

        final Map<String, String> properties = Map.of(ENABLE_JASYPT_PROPERTY, "true",
                                                      ENCRYPTED_PROPERTY_1, encryptedValue,
                                                      JASYPT_ENCRYPTOR_PASSWORD_PROPERTY, JASYPT_PASSWORD,
                                                      JASYPT_ENCRYPTOR_ALGORITHM_PROPERTY, JASYPT_ALGORITHM);

        new DefaultKillbillConfigSource(properties);
    }

    @Test(groups = "fast")
    public void testDecryptJasyptPropertySuccessfully() throws IOException, URISyntaxException {
        final String unencryptedValue1 = "myPropertyValue";
        final String encryptedValue1 = encString(unencryptedValue1);
        final String unencryptedValue2 = "myOtherPropertyValue";
        final String encryptedValue2 = encString(unencryptedValue2);

        final Map<String, String> properties = Map.of(ENABLE_JASYPT_PROPERTY, "true",
                                                      ENCRYPTED_PROPERTY_1, encryptedValue1,
                                                      ENCRYPTED_PROPERTY_2, encryptedValue2,
                                                      JASYPT_ENCRYPTOR_PASSWORD_PROPERTY, JASYPT_PASSWORD,
                                                      JASYPT_ENCRYPTOR_ALGORITHM_PROPERTY, JASYPT_ALGORITHM);

        final DefaultKillbillConfigSource configSource = new DefaultKillbillConfigSource(properties);

        final String actualValue1 = configSource.getString(ENCRYPTED_PROPERTY_1);
        final String actualValue2 = configSource.getString(ENCRYPTED_PROPERTY_2);

        Assert.assertEquals(unencryptedValue1, actualValue1);
        Assert.assertEquals(unencryptedValue2, actualValue2);
    }

    private String encString(final String unencryptedValue) {
        return "ENC(" + encrypt(unencryptedValue, JASYPT_ALGORITHM, JASYPT_PASSWORD) + ")";
    }

    private String encrypt(final String unencryptedValue, final String jasyptAlgorithm, final String jasyptPassword) {
        final StandardPBEStringEncryptor encryptor = setupEncryptor(jasyptPassword, jasyptAlgorithm);
        return encryptor.encrypt(unencryptedValue);
    }

    private StandardPBEStringEncryptor setupEncryptor(final String password, final String algorithm) {
        final StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(password);
        encryptor.setAlgorithm(algorithm);
        return encryptor;
    }
}
