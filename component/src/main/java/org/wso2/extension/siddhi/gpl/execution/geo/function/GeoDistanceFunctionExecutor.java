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

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.ReturnAttribute;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.executor.ExpressionExecutor;
import io.siddhi.core.executor.function.FunctionExecutor;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.exception.SiddhiAppValidationException;

/**
 * geo:distance(location1Latitude, location1Longitude, location2Latitude, location2Longitude)
 * <p>
 * This method gives the distance between two geo locations in meters
 * <p>
 * location1Latitude  - latitude value of 1st location
 * location1Longitude - longitude value of 1st location
 * location2Latitude  - latitude value of 2nd location
 * location2Longitude - longitude value of 2nd location
 * <p>
 * Accept Type(s) for geo:distance(location1Latitude, location1Longitude, location2Latitude, location2Longitude);
 * location1Latitude : DOUBLE
 * location1Longitude : DOUBLE
 * location2Latitude : DOUBLE
 * location2Longitude : DOUBLE
 * <p>
 * Return Type(s): DOUBLE
 */
@Extension(
        name = "distance",
        namespace = "geo",
        description = "This method gives the distance between two geo locations in meters",
        examples = @Example(
                description = "This will returns the distance in meters",
                syntax = "geo:distance(latitude, longitude, prevLatitude, prevLongitude)"),
        parameters = {
                @Parameter(name = "location1.latitude",
                        description = "latitude value of 1st location",
                        type = DataType.DOUBLE,
                        optional = false
                ),
                @Parameter(name = "location1.longitude",
                        description = "longitude value of 1st location",
                        type = DataType.DOUBLE,
                        optional = false
                ),
                @Parameter(name = "location2.latitude",
                        description = "latitude value of 2nd location",
                        type = DataType.DOUBLE,
                        optional = false
                ),
                @Parameter(name = "location2.longitude",
                        description = "longitude value of 2nd location",
                        type = DataType.DOUBLE,
                        optional = false
                )
        },
        returnAttributes = @ReturnAttribute(
                description = "Distance between two given geo location in meters",
                type = {DataType.DOUBLE})
)
public class GeoDistanceFunctionExecutor extends FunctionExecutor {
    Attribute.Type returnType = Attribute.Type.DOUBLE;

    @Override
    protected StateFactory init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                                SiddhiQueryContext siddhiQueryContext) {
        if (attributeExpressionExecutors.length != 4) {
            throw new SiddhiAppValidationException("Invalid no of arguments passed to " +
                    "geo:distance() function, " +
                    "requires 4, but found " + attributeExpressionExecutors.length);
        }
        return null;
    }

    @Override
    protected Object execute(Object[] data, State state) {
        if (data[0] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to geo:distance() " +
                    "function. First argument should be double");
        }
        if (data[1] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to geo:distance() " +
                    "function. Second argument should be double");
        }
        if (data[2] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to geo:distance() " +
                    "function. Third argument should be double");
        }
        if (data[3] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to geo:distance() " +
                    "function. Fourth argument should be double");
        }

        double latitude = (Double) data[0];
        double longitude = (Double) data[1];
        double prevLatitude = (Double) data[2];
        double prevLongitude = (Double) data[3];

        int r = 6371000; // Radius of the earth in m
        latitude = latitude * (Math.PI / 180);
        prevLatitude = prevLatitude * (Math.PI / 180);
        longitude = longitude * (Math.PI / 180);
        prevLongitude = prevLongitude * (Math.PI / 180);
        double dlon = prevLongitude - longitude;
        double dlat = prevLatitude - latitude;
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(latitude) * Math.cos(prevLatitude) *
                Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    @Override
    protected Object execute(Object data, State state) {
        return null;
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

}
