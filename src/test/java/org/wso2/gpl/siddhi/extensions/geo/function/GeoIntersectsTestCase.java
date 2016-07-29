/*
 * Copyright (C) 2015 WSO2 Inc. (http://wso2.com)
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

package org.wso2.gpl.siddhi.extensions.geo.function;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.gpl.siddhi.extensions.geo.GeoTestCase;

public class GeoIntersectsTestCase extends GeoTestCase {
    private static Logger logger = Logger.getLogger(GeoIntersectsTestCase.class);

    @Test
    public void testGeometry() throws Exception {
        logger.info("TestGeometry");

        data.clear();
        expectedResult.clear();
        eventCount = 0;

        data.add(new Object[]{"{'type':'Polygon','coordinates':[[[0.5, 0.5],[0.5, 1.5],[1.5, 1.5],[1.5, 0.5],[0.5, 0.5]]]}"});
        expectedResult.add(true);
        data.add(new Object[]{"{'type':'Circle','coordinates':[-1, -1], 'radius':221148}"});
        expectedResult.add(true);
        data.add(new Object[]{"{'type':'Point','coordinates':[2, 0]}"});
        expectedResult.add(false);
        data.add(new Object[]{"{'type':'Polygon','coordinates':[[[2, 2],[2, 1],[1, 1],[1, 2],[2, 2]]]}"});
        expectedResult.add(true);

        String executionPlan = "@config(async = 'true') define stream dataIn (geometry string);"
                + "@info(name = 'query1') from dataIn" +
                " select geo:intersects(geometry, \"{'type':'Polygon','coordinates':[[[0, 0],[0, 1],[1, 1],[1, 0],[0, 0]]]}\") as intersects \n" +
                " insert into dataOut";

        long start = System.currentTimeMillis();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        long end = System.currentTimeMillis();
        logger.info(String.format("Time to create ExecutionPlanRunTime: [%f sec]", ((end - start) / 1000f)));
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                for (Event event : inEvents) {
                    logger.info(event);
                    Boolean isWithin = (Boolean) event.getData(0);
                    Assert.assertEquals(expectedResult.get(eventCount++), isWithin);
                }
            }
        });
        executionPlanRuntime.start();
        generateEvents(executionPlanRuntime);
        Thread.sleep(1000);
        Assert.assertEquals(expectedResult.size(), eventCount);
    }
}