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
import org.wso2.extension.siddhi.gpl.execution.geo.internal.util.WithinOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculate geo crosses
 **/
@Extension(
        name = "crosses",
        namespace = "geo",
        description = "Returns true when the  the specified object of which the location is specified  in terms of " +
                "\blongitude  and \b latitude crosses the geographic location specified in " +
                "\bgeo.json.geometry.fence. Returns false when the object crosses out of the location specified in " +
                "\bgeo.json.geometry.fence. \n Or Returns true when the object (i.e. geo.json.geometry) crosses the" +
                " specified geographic location (i.e. geo.json.geometry.fence). " +
                "Returns false when the object crosses out of \bgeo.json.geometry.fence. ",

        parameters = {
                @Parameter(
                        name = "id",
                        description = "location id",
                        type = DataType.STRING
                ),
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
                                " coordinates of a geo geometry.This can be given instead of the longitude and " +
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
        examples = {@Example(
                description = "This will return true since the specified location crosses the " +
                        "geo.json.geometry.fence",
                syntax = "crosses(km-4354, -0.5, 0.5, {'type':'Polygon'," +
                        "'coordinates':[[[0, 0],[2, 0],[2, 1],[0, 1],[0, 0]]]} )")
        },
        returnAttributes = @ReturnAttribute(
                name = "isCrossed",
                description = "This will return a boolean value",
                type = {DataType.BOOL}
        )
)
public class GeoCrossesStreamProcessor extends StreamProcessor<State> {

    private GeoOperation geoOperation;
    private Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private List<Attribute> attributeList;

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
            boolean within = (Boolean) geoOperation.process(data);

            String id = attributeExpressionExecutors[0].execute(complexEvent).toString();
            if (set.contains(id)) {
                if (!within) {
                    //alert out
                    complexEventPopulater.populateComplexEvent(complexEvent, new Object[]{within});
                    set.remove(id);
                } else {
                    streamEventChunk.remove();
                }
            } else {
                if (within) {
                    //alert in
                    complexEventPopulater.populateComplexEvent(complexEvent, new Object[]{within});
                    set.add(id);
                } else {
                    streamEventChunk.remove();
                }
            }
        }
        nextProcessor.process(streamEventChunk);
    }

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
        geoOperation = new WithinOperation();
        geoOperation.init(attributeExpressionExecutors, 1, attributeExpressionLength);
        attributeList = new ArrayList<Attribute>(1);
        attributeList.add(new Attribute("crosses", Type.BOOL));
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

    @Override
    public List<Attribute> getReturnAttributes() {
        return attributeList;
    }

    @Override
    public ProcessingMode getProcessingMode() {
        return ProcessingMode.BATCH;
    }
}
