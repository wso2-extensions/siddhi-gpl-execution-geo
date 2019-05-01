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
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.ReturnAttribute;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.event.ComplexEvent;
import io.siddhi.core.event.ComplexEventChunk;
import io.siddhi.core.event.stream.MetaStreamEvent;
import io.siddhi.core.event.stream.StreamEvent;
import io.siddhi.core.event.stream.StreamEventCloner;
import io.siddhi.core.event.stream.holder.StreamEventClonerHolder;
import io.siddhi.core.event.stream.populater.ComplexEventPopulater;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.executor.ExpressionExecutor;
import io.siddhi.core.query.processor.ProcessingMode;
import io.siddhi.core.query.processor.Processor;
import io.siddhi.core.query.processor.stream.StreamProcessor;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.query.api.definition.AbstractDefinition;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.definition.Attribute.Type;
import org.wso2.extension.siddhi.gpl.execution.geo.internal.util.GeoOperation;
import org.wso2.extension.siddhi.gpl.execution.geo.internal.util.WithinDistanceOperation;

import java.util.ArrayList;
import java.util.List;
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
public class GeoStationaryStreamProcessor extends StreamProcessor<State> {

    private GeoOperation geoOperation;
    private double radius;
    private ConcurrentHashMap<String, Geometry> map = new ConcurrentHashMap<String, Geometry>();
    private ArrayList<Attribute> attributeList;

    /**
     * The init method of the StreamProcessor, this method will be called before other methods
     *
     * @param metaStreamEvent              the stream event meta
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param configReader                 this hold the Stream Processor configuration reader.
     * @param streamEventClonerHolder      stream event cloner holder
     * @param outputExpectsExpiredEvents   is expired events sent as output
     * @param findToBeExecuted             find will be executed
     * @param siddhiQueryContext           current siddhi query context
     *
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected StateFactory<State> init(MetaStreamEvent metaStreamEvent, AbstractDefinition inputDefinition,
                                       ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                                       StreamEventClonerHolder streamEventClonerHolder,
                                       boolean outputExpectsExpiredEvents, boolean findToBeExecuted,
                                       SiddhiQueryContext siddhiQueryContext) {
        geoOperation = new WithinDistanceOperation();
        geoOperation.init(attributeExpressionExecutors, 1, attributeExpressionLength - 1);
        if (attributeExpressionExecutors[attributeExpressionLength - 1].getReturnType() != Type.DOUBLE) {
            throw new SiddhiAppCreationException("Last parameter should be a double");
        }
        radius = (Double) attributeExpressionExecutors[attributeExpressionLength - 1].execute(null);
        attributeList = new ArrayList<Attribute>(1);
        attributeList.add(new Attribute("stationary", Type.BOOL));
        return null;
    }

    /**
     * The main processing method that will be called upon event arrival
     *
     * @param streamEventChunk      the event chunk that need to be processed
     * @param nextProcessor         the next processor to which the success events need to be passed
     * @param streamEventCloner     helps to clone the incoming event for local storage or modification
     * @param complexEventPopulater helps to populate the events with the resultant attributes
     * @param state                 current processor state
     */
    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater,
                           State state) {
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

    @Override
    public List<Attribute> getReturnAttributes() {
        return attributeList;
    }

    @Override
    public ProcessingMode getProcessingMode() {
        return ProcessingMode.BATCH;
    }
}
