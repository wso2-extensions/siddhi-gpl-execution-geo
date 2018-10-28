/*
 * Copyright (C) 2017 WSO2 Inc. (http://wso2.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wso2.extension.siddhi.gpl.execution.geo.stream;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.gpl.execution.geo.GeoTestCase;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GeoProximityTestCase extends GeoTestCase {
    private static Logger logger = Logger.getLogger(GeoProximityTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;
    private ArrayList<HashMap<String, Boolean>> expectedResultList = new ArrayList<HashMap<String, Boolean>>();
    private HashMap<String, Boolean> map1 = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> map2 = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> map3 = new HashMap<String, Boolean>();

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }

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

        String siddhiApp = "@config(async = 'true')" +
                "define stream dataIn (id string, longitude double, latitude double);"
                +
                "@info(name = 'query1') " +
                "from dataIn#geo:proximity(id,longitude,latitude, 110574.61087757687) " +
                "select inCloseProximity, proximityWith \n" +
                "insert into dataOut";

        long start = System.currentTimeMillis();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        long end = System.currentTimeMillis();
        logger.info(String.format("Time to create siddhiAppRuntime: [%f sec]", ((end - start) / 1000f)));
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count.incrementAndGet();
                for (Event event : inEvents) {
                    Boolean proximity = (Boolean) event.getData(0);
                    String other = (String) event.getData(1);
                    AssertJUnit.assertTrue(expectedResultList.get(eventCount).containsKey(other));
                    AssertJUnit.assertEquals(expectedResultList.get(eventCount).get(other), proximity);
                    eventArrived = true;
                }
                eventCount++;
            }
        });
        siddhiAppRuntime.start();
        generateEvents(siddhiAppRuntime);
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertEquals(expectedResultList.size(), count.get());
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }
}
