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
        description = "Geo within distance function",
        examples = @Example(description = "TBD", syntax = "TBD"),
        returnAttributes = @ReturnAttribute(description = "Return type is boolean", type = {DataType.BOOL})
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
