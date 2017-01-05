/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.gpl.execution.geo.stream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.extension.siddhi.gpl.execution.geo.GeoTestCase;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;

import java.util.ArrayList;
import java.util.HashMap;

public class GeoProximityTestCase extends GeoTestCase {
    private static Logger logger = Logger.getLogger(GeoProximityTestCase.class);
    private ArrayList<HashMap<String, Boolean>> expectedResultList = new ArrayList<HashMap<String, Boolean>>();
    private HashMap<String, Boolean> map1 = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> map2 = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> map3 = new HashMap<String, Boolean>();

    @Test
    public void testProximity() throws Exception {
        logger.info("TestProximity");

        data.clear();
        eventCount = 0;
        data.add(new Object[]{"1", 0d, 0d});
        data.add(new Object[]{"2", 1d, 1d});
        data.add(new Object[]{"3", 2d, 2d});
        data.add(new Object[]{"1", 1.5d, 1.5d});
        map1.put("2", true);
        map1.put("3", true);
        expectedResultList.add(map1);

        data.add(new Object[]{"1", 1.6d, 1.6d});
        data.add(new Object[]{"2", 5d, 5d});
        map2.put("1", false);
        expectedResultList.add(map2);

        data.add(new Object[]{"1", 2d, 2d});
        data.add(new Object[]{"1", 5.5d, 5.5d});
        map3.put("2", true);
        map3.put("3", false);
        expectedResultList.add(map3);

        String executionPlan = "@config(async = 'true')" +
                "define stream dataIn (id string, longitude double, latitude double);"
                +
                "@info(name = 'query1') " +
                "from dataIn#geo:proximity(id,longitude,latitude, 110574.61087757687) " +
                "select inCloseProximity, proximityWith \n" +
                "insert into dataOut";

        long start = System.currentTimeMillis();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        long end = System.currentTimeMillis();
        logger.info(String.format("Time to create ExecutionPlanRunTime: [%f sec]", ((end - start) / 1000f)));
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    Boolean proximity = (Boolean) event.getData(0);
                    String other = (String) event.getData(1);
                    Assert.assertTrue(expectedResultList.get(eventCount).containsKey(other));
                    Assert.assertEquals(expectedResultList.get(eventCount).get(other), proximity);
                }
                eventCount++;
            }
        });
        executionPlanRuntime.start();
        generateEvents(executionPlanRuntime);
        Thread.sleep(1000);
        Assert.assertEquals(expectedResultList.size(), eventCount);
    }
}