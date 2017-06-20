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

package org.wso2.extension.siddhi.gpl.execution.geo.stream;

import com.vividsolutions.jts.geom.Geometry;
import org.wso2.extension.siddhi.gpl.execution.geo.internal.util.GeoOperation;
import org.wso2.extension.siddhi.gpl.execution.geo.internal.util.WithinDistanceOperation;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.SiddhiAppContext;
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
        description = "Geo Proximity Stream Processor function",
        examples = @Example(description = "TBD", syntax = "TBD")
)
public class GeoProximityStreamProcessor extends StreamProcessor {

    private GeoOperation geoOperation;
    private double radius;
    private Map<String, Geometry> map = Collections.synchronizedMap(new HashMap<String, Geometry>());
    private Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    /**
     * The init method of the StreamProcessor, this method will be called before other methods
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param siddhiAppContext             the context of the execution plan
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors,
                                   ConfigReader configReader,
                                   SiddhiAppContext siddhiAppContext) {
        this.geoOperation = new WithinDistanceOperation();
        this.geoOperation.init(attributeExpressionExecutors, 1, attributeExpressionLength - 1);
        if (attributeExpressionExecutors[attributeExpressionLength - 1].getReturnType() != Type.DOUBLE) {
            throw new SiddhiAppCreationException("Last parameter should be a double");
        }
        radius = (Double) attributeExpressionExecutors[attributeExpressionLength - 1].execute(null);
        ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
        attributeList.add(new Attribute("proximityWith", Type.STRING));
        attributeList.add(new Attribute("inCloseProximity", Type.BOOL));
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

    /**
     * The main processing method that will be called upon event arrival
     *
     * @param streamEventChunk      the event chunk that need to be processed
     * @param nextProcessor         the next processor to which the success events need to be passed
     * @param streamEventCloner     helps to clone the incoming event for local storage or modification
     * @param complexEventPopulater helps to populate the events with the resultant attributes
     */
    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk,
                           Processor nextProcessor, StreamEventCloner streamEventCloner,
                           ComplexEventPopulater complexEventPopulater) {
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
}
