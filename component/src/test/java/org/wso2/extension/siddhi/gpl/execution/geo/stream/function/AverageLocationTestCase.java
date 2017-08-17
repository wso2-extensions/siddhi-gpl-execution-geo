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
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;


public class AverageLocationTestCase {
    private static final Logger log = Logger.getLogger(AverageLocationTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testAverageLocationWithSameWeightsTestCase() throws InterruptedException {
        log.info("testAverageLocation with same weight TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "@config(async = 'true') " +
                        "define stream cleanedStream (locationRecorder string, latitude double, longitude double, " +
                        "beaconProximity string, uuid string, weight double, timestamp long); " +
                        "@info(name = 'query1') " +
                        "from cleanedStream#geo:locationApproximate(locationRecorder, latitude, longitude, " +
                        "beaconProximity, uuid, weight, timestamp) " +
                        "select * " +
                        "insert into dataOut;");

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(6.876657000000001, event.getData(7));
                        AssertJUnit.assertEquals(79.897648, event.getData(8));
                        eventArrived = true;
                    } else if (count == 2) {
                        AssertJUnit.assertEquals(6.797727042508542, event.getData(7));
                        AssertJUnit.assertEquals(80.13557409252783, event.getData(8));
                        eventArrived = true;
                    } else if (count == 3) {
                        AssertJUnit.assertEquals(6.853572272662002, event.getData(7));
                        AssertJUnit.assertEquals(true, 80.34826512892124 == (Double) event.getData(8) ||
                                80.34826512892126 == (Double) event.getData(8));
                        eventArrived = true;
                    } else if (count == 4) {
                        AssertJUnit.assertEquals(true, 8.026326160526303 == (Double) event.getData(7) ||
                                8.0263261605263 == (Double) event.getData(7));
                        AssertJUnit.assertEquals(80.42794459517538, event.getData(8));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cleanedStream");
        siddhiAppRuntime.start();
        //nugegods, ratnapura, nuwara eliya, vavniya --> thbuttegama
        inputHandler.send(new Object[]{"person1", 6.876657, 79.897648, "ENTER", "uuid1", 20.0d, 1452583935L});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 6.718681, 80.373422, "ENTER", "uuid2", 20.0d, 1452583937L});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 6.964981, 80.773796, "ENTER", "uuid3", 20.0d, 1452583939L});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 8.729925, 80.475966, "ENTER", "uuid4", 100.0d, 1452583941L});
        Thread.sleep(100);

        AssertJUnit.assertEquals(4, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testAverageLocationWithDifferentWeightsTestCase2() throws InterruptedException {
        log.info("testAverageLocation with different weights TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "@config(async = 'true') " +
                        "define stream cleanedStream (locationRecorder string, latitude double, longitude double, " +
                        "beaconProximity string, uuid string, weight double, timestamp long); " +
                        "@info(name = 'query1') " +
                        "from cleanedStream#geo:locationApproximate(locationRecorder, latitude, longitude, " +
                        "beaconProximity, uuid, weight, timestamp) " +
                        "select * " +
                        "insert into dataOut;");

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(6.876657000000001, event.getData(7));
                        AssertJUnit.assertEquals(79.897648, event.getData(8));
                        eventArrived = true;
                    } else if (count == 2) {
                        AssertJUnit.assertEquals(6.797727042508542, event.getData(7));
                        AssertJUnit.assertEquals(80.13557409252783, event.getData(8));
                        eventArrived = true;
                    } else if (count == 3) {
                        AssertJUnit.assertEquals(6.853572272662002, event.getData(7));
                        AssertJUnit.assertEquals(true, 80.34826512892124 == (Double) event.getData(8) ||
                                80.34826512892126 == (Double) event.getData(8));
                        eventArrived = true;
                    } else if (count == 4) {
                        AssertJUnit.assertEquals(7.322639705655454, event.getData(7));
                        AssertJUnit.assertEquals(80.38008364787895, event.getData(8));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cleanedStream");
        siddhiAppRuntime.start();
        //nugegods, ratnapura, nuwara eliya, vavniya --> kegalle
        inputHandler.send(new Object[]{"person1", 6.876657, 79.897648, "ENTER", "uuid1", 20.0d, 1452583935L});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 6.718681, 80.373422, "ENTER", "uuid2", 20.0d, 1452583937L});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 6.964981, 80.773796, "ENTER", "uuid3", 20.0d, 1452583939L});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 8.729925, 80.475966, "ENTER", "uuid4", 20.0d, 1452583941L});
        Thread.sleep(100);

        AssertJUnit.assertEquals(4, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }
}
