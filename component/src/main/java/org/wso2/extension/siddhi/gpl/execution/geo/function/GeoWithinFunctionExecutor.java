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

import org.wso2.extension.siddhi.gpl.execution.geo.internal.util.WithinOperation;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;

/**
 * Check whether the given geo area is within the defined area
 **/
@Extension(
        name = "within",
        namespace = "geo",
        description = "This function can be call using two sets of parameters.\n This will returns true if " +
                "the location specified in terms of longitude and latitude is within the " +
                "geo.json.geometry.fence. \n Or returns true if the geo.json.geometry is within the " +
                "geo.json.geometry.fence.Returns false otherwise ",
        parameters = {
                @Parameter(
                        name = "longitude",
                        description = "longitude value of the geolocation ",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "latitude",
                        description = "latitude value of the geo location",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "geo.json.geometry",
                        description = "this will accepts a json as a string which contains the geometry type and" +
                                " coordinates of a geo geometry. This can be given instead of longitude and " +
                                "latitude values",
                        type = DataType.STRING
                ),
                @Parameter(
                        name = "geo.json.geometry.fence",
                        description = "this will accepts a json as a string which contains the geometry type and" +
                                " coordinates of a geo geometry fence",
                        type = DataType.STRING
                )

        },
        examples = {
                @Example(
                        description = "This will returns true since given longitude and latitude values are within" +
                                " the geo.json.geometry.fence ",
                        syntax = "within(0.5, 0.5, {'type':'Polygon'," +
                                "'coordinates':[[[0,0],[0,2],[1,2],[1,0],[0,0]]]} )"
                ),
                @Example(
                        description = "Returns true since geo.json.geometry is within the geo.json.geometry.fence.",
                        syntax = "within( {'type': 'Circle', 'radius': 110575, 'coordinates':[1.5, 1.5]} ," +
                                " {'type':'Polygon','coordinates':[[[0,0],[0,4],[3,4],[3,0],[0,0]]]} )")
        },
        returnAttributes = @ReturnAttribute(description = "Return type is boolean", type = {DataType.BOOL})
)
public class GeoWithinFunctionExecutor extends AbstractGeoOperationExecutor {
    public GeoWithinFunctionExecutor() {
        this.geoOperation = new WithinOperation();
    }
}
