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

import org.wso2.extension.siddhi.gpl.execution.geo.internal.util.WithinDistanceOperation;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * Calculate the geo is within the distance
 **/
@Extension(
        name = "withinDistance",
        namespace = "geo",
        description = "This function can be call using two sets of parameters. \n First method will returns true if " +
                "the location specified in terms of longitude and latitude is within " +
                "distance of the geoJSONGeometryFence. Returns false otherwise. \n Second method will return true if" +
                "the area given by geoJSONGeometry is within distance of the geoJSONGeometryFence. " +
                "\n please refer examples ",
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
                        description = "This will returns true because the location specified in terms of longitude" +
                                " and latitude is within the distance of the geoJSONGeometryFence.",
                        syntax = "withindistance( 0.5 , 0.5, " +
                                "{'type':'Polygon'," +
                                "'coordinates':[[[0, 0],[0, 1],[1, 1],[1, 0],[0, 0]]]}, 110574.61087757687)"
                ),
                @Example(
                        description = "This will returns true because geoJSONGeometry is within the distance of" +
                                " geoJSONGeometryFence.",
                        syntax = "withindistance( {'type':'Polygon','coordinates':[[[0.5, 0.5],[0.5, 1.5],[1.5, 1.5]," +
                                "[1.5, 0.5],[0.5, 0.5]]]} , {'type':'Polygon'," +
                                "'coordinates':[[[0, 0],[0, 1],[1, 1],[1, 0],[0, 0]]]}, 110574.61087757687)"
                )
        },

        returnAttributes = @ReturnAttribute(
                description = "Return type is boolean",
                type = {DataType.BOOL}
        )
)
public class GeoWithinDistanceFunctionExecutor extends AbstractGeoOperationExecutor {
    public GeoWithinDistanceFunctionExecutor() {
        this.geoOperation = new WithinDistanceOperation();
    }

    /**
     * The initialization method for FunctionExecutor, this method will be called before the other methods
     *
     * @param attributeExpressionExecutors are the executors of each function parameters
     * @param siddhiAppContext             the context of the execution plan
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
                        ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        this.geoOperation.init(attributeExpressionExecutors, 0, attributeExpressionExecutors.length);
        if (attributeExpressionExecutors[attributeExpressionExecutors.length - 1]
                .getReturnType() != Attribute.Type.DOUBLE) {
            throw new SiddhiAppCreationException("Last argument should be a double");
        }
    }
}
