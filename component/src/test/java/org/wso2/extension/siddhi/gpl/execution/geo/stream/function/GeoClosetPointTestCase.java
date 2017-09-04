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

package org.wso2.extension.siddhi.gpl.execution.geo.stream.function;

import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.gpl.execution.geo.GeoTestCase;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test case for geo closet point
 **/
public class GeoClosetPointTestCase extends GeoTestCase {
    private static Logger logger = Logger.getLogger(GeoClosetPointTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testClosestPoints() throws Exception {
        logger.info("testClosestPoints");

        data.clear();
        expectedResult.clear();
//        eventCount = 0;
        data.add(new Object[]{"km-4354", 0.5d, 0.5d});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", 2d, 2d});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", -0.5d, 1.5d});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", 0.5d, 1.25d});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", 0.0d, 0.0d});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", -1d, -1d});
        expectedResult.add(false);

        String siddhiApp = "@config(async = 'true') " +
                "define stream dataIn (id string, longitude double, latitude double);"
                + "@info(name = 'query1') from dataIn#geo:closestPoints(longitude,latitude,\"{'type':'Polygon'," +
                "'coordinates':[[[0,0],[0,2],[1,2],[1,0],[0,0]]]}\") " +
                "select closestPointOf1From2Latitude, closestPointOf1From2Longitude, closestPointOf2From1Latitude, " +
                "closestPointOf2From1Longitude \n" +
                "insert into dataOut";

        long start = System.currentTimeMillis();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        final long end = System.currentTimeMillis();
        logger.info(String.format("Time to create siddhiAppRuntime: [%f sec]", ((end - start) / 1000f)));
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount++;
                    count.incrementAndGet();
                    switch (count.get()) {
                        case 1:
                            AssertJUnit.assertArrayEquals(new Object[]{0.5, 0.5, 0.5, 0.5}, event.getData());
                            eventArrived = true;
                            break;
                        case 2:
                            AssertJUnit.assertArrayEquals(new Object[]{2.0, 2.0, 1.0, 2.0}, event.getData());
                            eventArrived = true;
                            break;
                        case 3:
                            AssertJUnit.assertArrayEquals(new Object[]{-0.5, 1.5, 0.0, 1.5}, event.getData());
                            eventArrived = true;
                            break;
                        case 4:
                            AssertJUnit.assertArrayEquals(new Object[]{0.5, 1.25, 0.5, 1.25}, event.getData());
                            eventArrived = true;
                            break;
                        case 5:
                            AssertJUnit.assertArrayEquals(new Object[]{0.0, 0.0, 0.0, 0.0}, event.getData());
                            eventArrived = true;
                            break;
                        case 6:
                            AssertJUnit.assertArrayEquals(new Object[]{-1.0, -1.0, 0.0, 0.0}, event.getData());
                            eventArrived = true;
                            break;

                    }
                }
            }
        });
        siddhiAppRuntime.start();
        generateEvents(siddhiAppRuntime);
        SiddhiTestHelper.waitForEvents(100, 6, count, 60000);
        AssertJUnit.assertEquals(expectedResult.size(), count.get());
        AssertJUnit.assertTrue(eventArrived);
    }


    @Test
    public void testClosestPointsGeometry() throws Exception {
        logger.info("testClosestPointsGeometry");

        data.clear();
        expectedResult.clear();
        data.add(new Object[]{"km-4354", "{'type':'Polygon'," +
                "'coordinates':[[[0.5, 0.5],[0.5,1.5],[0.75,1.5],[0.75,0.5],[0.5,0.5]]]}"});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", "{'type': 'Circle', 'radius': 110575, 'coordinates':[1.5, 1.5]}"});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", "{'type': 'Circle', 'radius': 110575, 'coordinates':[10, 1.5]}"});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", "{'type':'Point', 'coordinates':[-1,1]}"});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", "{'type':'MultiPolygon'," +
                "'coordinates':[[[[0.5, 0.5],[0.5,1.5],[0.75,1.5],[5,5],[0.5,0.5]]], " +
                "[[[1, 1],[1,2],[2,2],[2,1],[1,1]]]]}"});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", "{'type':'Point', 'coordinates':[10,10]}"});
        expectedResult.add(true);

        String siddhiApp = "@config(async = 'true') define stream dataIn (id string, geometry string);"
                + "@info(name = 'query1') from dataIn#geo:closestPoints(geometry,\"{'type':'Polygon'," +
                "'coordinates':[[[0,0],[0,4],[3,4],[3,0],[0,0]]]}\") " +
                "select closestPointOf1From2Latitude, closestPointOf1From2Longitude, " +
                "closestPointOf2From1Latitude, closestPointOf2From1Longitude \n" +
                "insert into dataOut";

        long start = System.currentTimeMillis();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        long end = System.currentTimeMillis();
        logger.info(String.format("Time to create siddhiAppRuntime: [%f sec]", ((end - start) / 1000f)));
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    switch (count.get()) {
                        case 1:
                            AssertJUnit.assertArrayEquals(new Object[]{0.5, 0.5, 0.5, 0.5}, event.getData());
                            eventArrived = true;
                            break;
                        case 2:
                            AssertJUnit.assertArrayEquals(new Object[]{2.5000035190937595, 1.5,
                                    2.5000035190937595, 1.5}, event.getData());
                            eventArrived = true;
                            break;
                        case 3:
                            AssertJUnit.assertArrayEquals(new Object[]{8.99999648090624, 1.5000000000000007,
                                    3.0, 1.5000000000000009}, event.getData());
                            eventArrived = true;
                            break;
                        case 4:
                            AssertJUnit.assertArrayEquals(new Object[]{-1.0, 1.0, 0.0, 1.0}, event.getData());
                            eventArrived = true;
                            break;
                        case 5:
                            AssertJUnit.assertArrayEquals(new Object[]{0.5, 0.5, 0.5, 0.5}, event.getData());
                            eventArrived = true;
                            break;
                        case 6:
                            AssertJUnit.assertArrayEquals(new Object[]{10.0, 10.0, 3.0, 4.0}, event.getData());
                            eventArrived = true;
                            break;

                    }
                }
            }
        });
        siddhiAppRuntime.start();
        generateEvents(siddhiAppRuntime);
        SiddhiTestHelper.waitForEvents(100, 6, count, 60000);
        AssertJUnit.assertEquals(expectedResult.size(), count.get());
        AssertJUnit.assertTrue(eventArrived);
    }
}
