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

package org.wso2.extension.siddhi.gpl.execution.geo;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.stream.input.InputHandler;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;

import java.util.ArrayList;

public abstract class GeoTestCase {
    protected static SiddhiManager siddhiManager;
    private static Logger logger = Logger.getLogger(GeoTestCase.class);
    protected static ArrayList<Object[]> data;
    protected static ArrayList<Boolean> expectedResult;
    protected static int eventCount;

    @BeforeClass
    public static void setUp() throws Exception {
        logger.info("Init Siddhi"); // Create Siddhi Manager
        siddhiManager = new SiddhiManager();
        data = new ArrayList<Object[]>();
        expectedResult = new ArrayList<Boolean>();
    }

    protected void generateEvents(SiddhiAppRuntime siddhiAppRuntime) throws Exception {
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("dataIn");
        for (Object[] dataLine : data) {
            inputHandler.send(dataLine);
        }
    }
}
