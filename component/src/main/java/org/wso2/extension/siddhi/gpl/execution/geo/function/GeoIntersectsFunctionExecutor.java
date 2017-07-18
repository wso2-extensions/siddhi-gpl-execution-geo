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

import org.wso2.extension.siddhi.gpl.execution.geo.internal.util.IntersectsOperation;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;


/**
 * Calculate the geo intersection
 **/
@Extension(
        name = "intersects",
        namespace = "geo",
        description = "This function can be called using two sets of parameters. \n First method will return true " +
                "if the incoming event geo.json.geometry intersects the given geo.json.geometryFence else false." +
                "\n Second method will return true if the location pointed by longitude and latitude intersects the " +
                "given geo.json.geometryFence else false \n Please refer examples",
        parameters = {

                @Parameter(
                        name = "longitude",
                        description = "this will accepts the longitude value of the geo location as a double, " +
                                "This and the latitude value can be given instead of giving geo.json.geometry value ",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "latitude",
                        description = "this will accepts the latitude value of the geo location as a double. ",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "geo.json.geometry",
                        description = "this will accepts a json as a string which contains the geometry type and" +
                                " coordinates of a geo geometry. This can be given instead of the longitude and " +
                                "latitude values  ",
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
                        description = "returns true because geo.json.geometry intersects geo.json.geometry.fence",
                        syntax = "intersects( {'type':'Polygon','coordinates':[[[0.5, 0.5],[0.5, 1.5],[1.5, 1.5]," +
                                "[1.5, 0.5],[0.5, 0.5]]]} , {'type':'Polygon'," +
                                "'coordinates':[[[0, 0],[0, 1],[1, 1],[1, 0],[0, 0]]]} )"),
                @Example(
                        description = "returns true because location pointed by longitude and latitude intersects " +
                                "geo.json.geometry.fence",
                        syntax = " intersects(0.5. 0.5 , " +
                                "{'type':'Polygon','coordinates':[[[0, 0],[0, 1],[1, 1],[1, 0],[0, 0]]]})"

                )
        },

        returnAttributes = @ReturnAttribute(description = "Return type is boolean", type = {DataType.BOOL})
)
public class GeoIntersectsFunctionExecutor extends AbstractGeoOperationExecutor {
    public GeoIntersectsFunctionExecutor() {
        this.geoOperation = new IntersectsOperation();
    }
}

