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
import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.gpl.execution.geo.GeoTestCase;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;

public class GeoCrossesTestCase extends GeoTestCase {
    private static Logger logger = Logger.getLogger(GeoCrossesTestCase.class);

    @Test
    public void testCrosses() throws Exception {
        logger.info("TestCrosses");

        data.clear();
        expectedResult.clear();
        eventCount = 0;

        data.add(new Object[]{"km-4354", -0.5d, 0.5d});
        data.add(new Object[]{"km-4354", 1.5d, 0.5d});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", 1.5d, 0.25d});
        data.add(new Object[]{"tc-1729", 0.5d, 0.5d});
        expectedResult.add(true);
        data.add(new Object[]{"tc-1729", -0.5d, 0.5d});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", 1.3d, 0.1d});
        data.add(new Object[]{"km-4354", 3d, 0.25d});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", 4d, 0.25d});
        data.add(new Object[]{"km-4354", 1d, 0.5d});
        expectedResult.add(true);

        String siddhiApp = "@config(async = 'true')" +
                " define stream dataIn (id string, longitude double, latitude double);"
                + "@info(name = 'query1') from dataIn#geo:crosses(id,longitude,latitude,\"{'type':'Polygon'," +
                "'coordinates':[[[0, 0],[2, 0],[2, 1],[0, 1],[0, 0]]]}\") " +
                "select crosses \n" +
                "insert into dataOut";

        long start = System.currentTimeMillis();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        long end = System.currentTimeMillis();
        logger.info(String.format("Time to create siddhiAppRuntime: [%f sec]", ((end - start) / 1000f)));
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                for (Event event : inEvents) {
                    Boolean isWithin = (Boolean) event.getData(0);
                    AssertJUnit.assertEquals(expectedResult.get(eventCount++), isWithin);
                }
            }
        });
        siddhiAppRuntime.start();
        generateEvents(siddhiAppRuntime);
        Thread.sleep(1000);
        AssertJUnit.assertEquals(expectedResult.size(), eventCount);
    }
}

