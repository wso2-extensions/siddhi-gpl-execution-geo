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
