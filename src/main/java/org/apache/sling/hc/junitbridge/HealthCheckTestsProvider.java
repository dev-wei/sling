/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.sling.hc.junitbridge;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.junit.TestsProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;

/** Bridge Health Checks into the Sling JUnit server-side test
 *  framework, based on their tags.
 */
@Component
@Service
public class HealthCheckTestsProvider implements TestsProvider {

    private String servicePid;
    private long lastModified;
    private BundleContext bundleContext;
    
    public static final String TEST_NAME_PREFIX = "HealthChecks(";
    public static final String TEST_NAME_SUFFIX = ")";

    // TODO configurable
    private String [] tags = { 
        "script",
        "sling",
        "bundles,script",
        "bundles,-script"
    };
    
    @Activate
    protected void activate(ComponentContext ctx) {
        bundleContext = ctx.getBundleContext();
        servicePid = (String)ctx.getProperties().get(Constants.SERVICE_PID);
        lastModified = System.currentTimeMillis();
    }
    
    @Deactivate
    protected void deactivate() {
        bundleContext = null;
        servicePid = null;
        lastModified = -1;
    }
    
    @Override
    public Class<?> createTestClass(String testName) throws ClassNotFoundException {
        // The test name is like "Health Checks(foo,bar)" and we need just 'foo,bar'
        final String tagString = testName.substring(0, testName.length() - TEST_NAME_SUFFIX.length()).substring(TEST_NAME_PREFIX.length()); 
        JUnitTestBridge.setThreadContext(new TestBridgeContext(bundleContext, splitTags(tagString)));
        return JUnitTestBridge.class;
    }
    
    private String [] splitTags(String tags) {
        final List<String> result = new ArrayList<String>();
        for(String tag: tags.split(",")) {
            result.add(tag.trim());
        }
        return result.toArray(new String[]{});
    }

    @Override
    public String getServicePid() {
        return servicePid;
    }

    @Override
    public List<String> getTestNames() {
        final List<String> result = new ArrayList<String>();
        for(String t : tags) {
            result.add(TEST_NAME_PREFIX + t + TEST_NAME_SUFFIX);
        }
        return result;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }
}