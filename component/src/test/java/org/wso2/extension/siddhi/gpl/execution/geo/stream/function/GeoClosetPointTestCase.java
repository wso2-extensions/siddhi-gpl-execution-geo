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

package org.wso2.extension.siddhi.gpl.execution.geo.stream.function;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import org.wso2.extension.siddhi.gpl.execution.geo.GeoTestCase;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;

/**Test case for geo closet point**/
public class GeoClosetPointTestCase extends GeoTestCase {
    private static Logger logger = Logger.getLogger(GeoClosetPointTestCase.class);



    @Test
    public void testClosestPoints() throws Exception {
        logger.info("testClosestPoints");

        data.clear();
        expectedResult.clear();
        eventCount = 0;
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
                    switch (eventCount) {
                        case 1:
                            AssertJUnit.assertArrayEquals(new Object[]{0.5, 0.5, 0.5, 0.5}, event.getData());
                            break;
                        case 2:
                            AssertJUnit.assertArrayEquals(new Object[]{2.0, 2.0, 1.0, 2.0}, event.getData());
                            break;
                        case 3:
                            AssertJUnit.assertArrayEquals(new Object[]{-0.5, 1.5, 0.0, 1.5}, event.getData());
                            break;
                        case 4:
                            AssertJUnit.assertArrayEquals(new Object[]{0.5, 1.25, 0.5, 1.25}, event.getData());
                            break;
                        case 5:
                            AssertJUnit.assertArrayEquals(new Object[]{0.0, 0.0, 0.0, 0.0}, event.getData());
                            break;
                        case 6:
                            AssertJUnit.assertArrayEquals(new Object[]{-1.0, -1.0, 0.0, 0.0}, event.getData());
                            break;

                    }
                }
            }
        });
        siddhiAppRuntime.start();
        generateEvents(siddhiAppRuntime);
        Thread.sleep(1000);
        AssertJUnit.assertEquals(expectedResult.size(), eventCount);
    }


    @Test
    public void testClosestPointsGeometry() throws Exception {
        logger.info("testClosestPointsGeometry");

        data.clear();
        expectedResult.clear();
        eventCount = 0;
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
                    eventCount++;
                    switch (eventCount) {
                        case 1:
                            AssertJUnit.assertArrayEquals(new Object[]{0.5, 0.5, 0.5, 0.5}, event.getData());
                            break;
                        case 2:
                            AssertJUnit.assertArrayEquals(new Object[]{2.5000035190937595, 1.5,
                                    2.5000035190937595, 1.5}, event.getData());
                            break;
                        case 3:
                            AssertJUnit.assertArrayEquals(new Object[]{8.99999648090624, 1.5000000000000007,
                                    3.0, 1.5000000000000009}, event.getData());
                            break;
                        case 4:
                            AssertJUnit.assertArrayEquals(new Object[]{-1.0, 1.0, 0.0, 1.0}, event.getData());
                            break;
                        case 5:
                            AssertJUnit.assertArrayEquals(new Object[]{0.5, 0.5, 0.5, 0.5}, event.getData());
                            break;
                        case 6:
                            AssertJUnit.assertArrayEquals(new Object[]{10.0, 10.0, 3.0, 4.0}, event.getData());
                            break;

                    }
                }
            }
        });
        siddhiAppRuntime.start();
        generateEvents(siddhiAppRuntime);
        Thread.sleep(1000);
        AssertJUnit.assertEquals(expectedResult.size(), eventCount);
    }
}
