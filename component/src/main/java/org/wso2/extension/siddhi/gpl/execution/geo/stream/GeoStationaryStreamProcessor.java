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

package org.wso2.extension.siddhi.gpl.execution.geo.stream;

import com.vividsolutions.jts.geom.Geometry;
import org.wso2.extension.siddhi.gpl.execution.geo.internal.util.GeoOperation;
import org.wso2.extension.siddhi.gpl.execution.geo.internal.util.WithinDistanceOperation;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.Attribute.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculating the geo stationary
 **/

@Extension(
        name = "stationary",
        namespace = "geo",
        description = "Returns true when the object (defined in terms of  longitude  and latitude) becomes stationary" +
                " within the specified radius. Returns false when the object moves out of the specified radius.",
        parameters = {
                @Parameter(
                        name = "id",
                        description = "object id which is defined in terms of longitude and latitude",
                        type = DataType.STRING
                ),
                @Parameter(
                        name = "longitude",
                        description = "longitude of the location as a double",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "latitude",
                        description = "latitude of the location as a double",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "geo.json.geometry.fence",
                        description = "string value of the json object which contains the details of the geo geometry" +
                                " fence. This is ",
                        optional = true,
                        defaultValue = " ",
                        type = DataType.STRING

                ),
                @Parameter(
                        name = "radius",
                        description = "specific radius as a double value",
                        type = DataType.DOUBLE
                )
        },
        examples = @Example(
                description = "returns true",
                syntax = "stationary(km-4354,0,0, 110574.61087757687)"
        ),
        returnAttributes = @ReturnAttribute(
                name = "isStationary",
                description = "This will return a boolean value",
                type = DataType.BOOL
        )
)
public class GeoStationaryStreamProcessor extends StreamProcessor {

    private GeoOperation geoOperation;
    private double radius;
    private ConcurrentHashMap<String, Geometry> map = new ConcurrentHashMap<String, Geometry>();

    /**
     * The init method of the StreamProcessor, this method will be called before other methods
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param siddhiAppContext             the context of the execution plan
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[]
            attributeExpressionExecutors, ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        geoOperation = new WithinDistanceOperation();
        geoOperation.init(attributeExpressionExecutors, 1, attributeExpressionLength - 1);
        if (attributeExpressionExecutors[attributeExpressionLength - 1].getReturnType() != Type.DOUBLE) {
            throw new SiddhiAppCreationException("Last parameter should be a double");
        }
        radius = (Double) attributeExpressionExecutors[attributeExpressionLength - 1].execute(null);
        ArrayList<Attribute> attributeList = new ArrayList<Attribute>(1);
        attributeList.add(new Attribute("stationary", Type.BOOL));
        return attributeList;
    }

    /**
     * This will be called only once, to acquire required resources
     * after initializing the system and before processing the events.
     */
    @Override
    public void start() {

    }

    /**
     * This will be called only once, to release the acquired resources
     * before shutting down the system.
     */
    @Override
    public void stop() {

    }

    /**
     * The serializable state of the element, that need to be
     * persisted for the reconstructing the element to the same state
     * on a different point of time
     *
     * @return stateful objects of the element as an array
     */
    @Override
    public Map<String, Object> currentState() {
        return new HashMap<String, Object>();
    }

    /**
     * The serialized state of the element, for reconstructing
     * the element to the same state as if was on a previous point of time.
     *
     * @param state the stateful objects of the element as an array on
     *              the same order provided by currentState().
     */
    @Override
    public void restoreState(Map<String, Object> state) {

    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        while (streamEventChunk.hasNext()) {
            ComplexEvent complexEvent = streamEventChunk.next();

            Object[] data = new Object[attributeExpressionLength - 1];
            for (int i = 1; i < attributeExpressionLength; i++) {
                data[i - 1] = attributeExpressionExecutors[i].execute(complexEvent);
            }
            Geometry currentGeometry = geoOperation.getCurrentGeometry(data);
            String id = attributeExpressionExecutors[0].execute(complexEvent).toString();
            Geometry previousGeometry = map.get(id);

            if (previousGeometry == null) {
                currentGeometry.setUserData(false);
                map.putIfAbsent(id, currentGeometry);
                streamEventChunk.remove();
                continue;

            }
            boolean stationary = (Boolean) geoOperation.operation(currentGeometry,
                    previousGeometry, new Object[]{radius});

            if ((Boolean) previousGeometry.getUserData()) {
                if (!stationary) {
                    //alert out
                    complexEventPopulater.populateComplexEvent(complexEvent, new Object[]{stationary});
                    currentGeometry.setUserData(stationary);
                    map.put(id, currentGeometry);
                } else {
                    streamEventChunk.remove();
                }
            } else {
                if (stationary) {
                    //alert in
                    previousGeometry.setUserData(stationary);
                    complexEventPopulater.populateComplexEvent(complexEvent, new Object[]{stationary});
                } else {
                    currentGeometry.setUserData(stationary);
                    map.put(id, currentGeometry);
                    streamEventChunk.remove();
                }
            }
        }
        nextProcessor.process(streamEventChunk);
    }
}
