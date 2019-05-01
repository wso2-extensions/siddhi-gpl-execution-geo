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
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.executor.ExpressionExecutor;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.query.api.definition.Attribute;
import org.wso2.extension.siddhi.gpl.execution.geo.internal.util.WithinDistanceOperation;

/**
 * Calculate the geo is within the distance
 **/
@Extension(
        name = "withinDistance",
        namespace = "geo",
        description = "This function can be call using two sets of parameters. \n First method will returns true if " +
                "the location specified in terms of longitude and latitude is within " +
                "distance of the geo.json.geometry.fence. Returns false otherwise. \n Second method will return true" +
                " if the area given by geo.json.geometry is within distance of the geo.json.geometry.fence. " +
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
                                " and latitude is within the distance of the geo.json.geometry.fence.",
                        syntax = "withindistance( 0.5 , 0.5, " +
                                "{'type':'Polygon'," +
                                "'coordinates':[[[0, 0],[0, 1],[1, 1],[1, 0],[0, 0]]]}, 110574.61087757687)"
                ),
                @Example(
                        description = "This will returns true because geo.json.geometry is within the distance of" +
                                " geo.json.geometry.fence.",
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
     * @param siddhiQueryContext             the context of the execution plan
     */
    @Override
    protected StateFactory<State> init(ExpressionExecutor[] attributeExpressionExecutors,
                                       ConfigReader configReader, SiddhiQueryContext siddhiQueryContext) {
        this.geoOperation.init(attributeExpressionExecutors, 0, attributeExpressionExecutors.length);
        if (attributeExpressionExecutors[attributeExpressionExecutors.length - 1]
                .getReturnType() != Attribute.Type.DOUBLE) {
            throw new SiddhiAppCreationException("Last argument should be a double");
        }
        return null;
    }
}
