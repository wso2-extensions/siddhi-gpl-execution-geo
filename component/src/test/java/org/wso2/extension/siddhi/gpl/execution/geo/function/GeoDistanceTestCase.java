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

package org.wso2.extension.siddhi.gpl.execution.geo.function;

import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;


public class GeoDistanceTestCase {
    private static final Logger log = Logger.getLogger(GeoDistanceTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testGeoDistanceTestCase() throws InterruptedException {
        log.info("testGeoDistance TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "@config(async = 'true') " +
                        "define stream cleanedStream (latitude double, longitude double, prevLatitude double, " +
                        "prevLongitude double); " +
                        "@info(name = 'query1') " +
                        "from cleanedStream " +
                        "select geo:distance(latitude, longitude, prevLatitude, prevLongitude) as distance " +
                        "insert into dataOut;");

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(2322119.848252557, event.getData(0));
                        eventArrived = true;
                    } else if (count == 2) {
                        AssertJUnit.assertEquals(871946.8734223971, event.getData(0));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cleanedStream");
        siddhiAppRuntime.start();
        //getting distance near equator
        inputHandler.send(new Object[]{8.116553, 77.523679, 9.850047, 98.597177});
        Thread.sleep(500);
        //getting distance away from equator
        inputHandler.send(new Object[]{54.432063, 19.669778, 59.971487, 29.958951});
        Thread.sleep(100);

        AssertJUnit.assertEquals(2, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }
}


