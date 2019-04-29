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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculating the geo proximity
 **/
@Extension(
        name = "proximity",
        namespace = "geo",
        description = "This will returns true when two objects (specified in terms of longitude and latitude) are " +
                "within the specified radius to another object. Returns false when the specified object moves out of " +
                "the specified radius. The proximityWith optional attribute indicates the ID of the object that the " +
                "object specified is in close proximity with. proximityID is a unique ID for the two objects" +
                " in close proximity.",
        parameters = {
                @Parameter(
                        name = "id",
                        description = "id of the object",
                        type = DataType.STRING
                ),
                @Parameter(
                        name = "longitude",
                        description = "longitude value of the location",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "latitude",
                        description = "latitude value of the location",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "geo.json.geometry.fence",
                        description = "string value of the json object which contains the details of the geo" +
                                " geometry fence.",
                        type = DataType.STRING

                ),
                @Parameter(
                        name = "radius",
                        description = "specific radius as a double value",
                        type = DataType.DOUBLE
                )
        },
        examples = @Example(
                description = "This will return true since given longitude and latitude is within the radius",
                syntax = "proximity(1, 0, 0, 110574.61087757687)"),
        returnAttributes = @ReturnAttribute(
                name = "isWithinProximity",
                description = "This will return a boolean value",
                type = DataType.BOOL
        )
)
public class GeoProximityStreamProcessor extends StreamProcessor<State> {

    private GeoOperation geoOperation;
    private double radius;
    private Map<String, Geometry> map = Collections.synchronizedMap(new HashMap<String, Geometry>());
    private Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
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
        this.geoOperation = new WithinDistanceOperation();
        this.geoOperation.init(attributeExpressionExecutors, 1, attributeExpressionLength - 1);
        if (attributeExpressionExecutors[attributeExpressionLength - 1].getReturnType() != Type.DOUBLE) {
            throw new SiddhiAppCreationException("Last parameter should be a double");
        }
        radius = (Double) attributeExpressionExecutors[attributeExpressionLength - 1].execute(null);
        attributeList = new ArrayList<Attribute>();
        attributeList.add(new Attribute("proximityWith", Type.STRING));
        attributeList.add(new Attribute("inCloseProximity", Type.BOOL));
        return null;
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
            StreamEvent streamEvent = streamEventChunk.next();
            Geometry currentGeometry, previousGeometry;
            Object[] data = new Object[attributeExpressionLength - 1];
            for (int i = 1; i < attributeExpressionLength; i++) {
                data[i - 1] = attributeExpressionExecutors[i].execute(streamEvent);
            }
            String currentId = attributeExpressionExecutors[0].execute(streamEvent).toString();
            String previousId;
            currentGeometry = geoOperation.getCurrentGeometry(data);
            if (!map.containsKey(currentId)) {
                map.put(currentId, currentGeometry);
            }
            for (Map.Entry<String, Geometry> entry : map.entrySet()) {
                previousId = entry.getKey();
                if (!previousId.equals(currentId)) {
                    previousGeometry = entry.getValue();
                    boolean within = (Boolean) geoOperation.operation(currentGeometry,
                            previousGeometry, new Object[]{radius});
                    String key = makeCompositeKey(currentId, previousId);
                    boolean contains = set.contains(key);
                    if (contains) {
                        if (!within) {
                            //alert out
                            StreamEvent newStreamEvent = streamEventCloner.copyStreamEvent(streamEvent);
                            complexEventPopulater.populateComplexEvent(newStreamEvent,
                                    new Object[]{previousId, within});
                            streamEventChunk.insertBeforeCurrent(newStreamEvent);
                            set.remove(key);
                        }
                    } else {
                        if (within) {
                            //alert in
                            StreamEvent newStreamEvent = streamEventCloner.copyStreamEvent(streamEvent);
                            complexEventPopulater.populateComplexEvent(newStreamEvent,
                                    new Object[]{previousId, within});
                            streamEventChunk.insertBeforeCurrent(newStreamEvent);
                            set.add(key);
                        }
                    }
                }
            }
            streamEventChunk.remove();
        }
        nextProcessor.process(streamEventChunk);
    }

    /*
        private Object[] toOutput(Geometry geometry, boolean within) {
            if (geoOperation.point) {
                return new Object[]{((Point) geometry).getX(), ((Point) geometry).getY(), within};
            } else {
                return new Object[]{GeometryUtils.geometrytoJSON(geometry), within};
            }
        }*/
    public String makeCompositeKey(String key1, String key2) {
        if (key1.compareToIgnoreCase(key2) < 0) {
            return key1 + "~" + key2;
        } else {
            return key2 + "~" + key1;
        }
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
