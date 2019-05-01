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
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.executor.ExpressionExecutor;
import io.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.query.api.definition.AbstractDefinition;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * geoLocationApproximate(location.recorder, latitude, longitude, sensor.proximity,
 * sensor.uuid, sensor.weight, timestamp)
 * <p>
 * This method computed the average location of the locationRecorder using the collection iBeacons which the location
 * recorder resides.
 * <p>
 * location.recorder - unique id of the object or item
 * latitude         - latitude value of the iBeacon
 * longitude        - longitude value of the iBeacon
 * sensor.proximity  - proximity which will be given by the iBeacon (eg: ENTER, RANGE, EXIT)
 * sensor.uuid       - unique id of the iBeacon
 * sensor.weight     - weight of the iBeacon which influence the averaging of the location (eg: approximate distance
 * from the beacon
 * timestamp        - timestamp of the log which will be used to remove iBeacon from one's collection when there is no
 * new log for 5 minutes
 * <p>
 * Accept Type(s) for geoLocationApproximate(location.recorder, latitude, longitude, sensor.proximity, sensor.uuid,
 * sensor.weight, timestamp);
 * location.recorder : STRING
 * latitude : DOUBLE
 * longitude : DOUBLE
 * sensor.proximity : STRING
 * sensor.uuid : STRING
 * sensor.weight : DOUBLE
 * timestamp : LONG
 * <p>
 * Return Type(s): DOUBLE, DOUBLE, BOOL
 */
/**
 * geoLocationApproximate(location.recorder, latitude, longitude, sensor.proximity,
 * sensor.uuid, sensor.weight, timestamp)
 * <p>
 * This method computed the average location of the locationRecorder using the collection iBeacons which the location
 * recorder resides.
 * <p>
 * location.recorder - unique id of the object or item
 * latitude         - latitude value of the iBeacon
 * longitude        - longitude value of the iBeacon
 * sensor.proximity  - proximity which will be given by the iBeacon (eg: ENTER, RANGE, EXIT)
 * sensor.uuid       - unique id of the iBeacon
 * sensor.weight     - weight of the iBeacon which influence the averaging of the location (eg: approximate distance
 * from the beacon
 * timestamp        - timestamp of the log which will be used to remove iBeacon from one's collection when there is no
 * new log for 5 minutes
 * <p>
 * Accept Type(s) for geoLocationApproximate(location.recorder, latitude, longitude, sensor.proximity, sensor.uuid,
 * sensor.weight, timestamp);
 * location.recorder : STRING
 * latitude : DOUBLE
 * longitude : DOUBLE
 * sensor.proximity : STRING
 * sensor.uuid : STRING
 * sensor.weight : DOUBLE
 * timestamp : LONG
 * <p>
 * Return Type(s): DOUBLE, DOUBLE, BOOL
 */
@Extension(
        name = "locationApproximate",
        namespace = "geo",
        description = "Geo Location Approximation compute the average location of the locationRecorder using the" +
                " collection iBeacons which the location recorder resides.",
        examples = @Example(
                description = "this will return 6.876657000000001 as the approximated location ",
                syntax = "geoLocationApproximate(\"person1\", 6.876657, 79.897648, \"ENTER\"," +
                        " \"uuid1\", 20.0d, 1452583935L)"
        ),
        parameters = {
                @Parameter(
                        name = "location.recorder",
                        description = "unique id of the object or item",
                        type = DataType.STRING
                ),
                @Parameter(
                        name = "latitude",
                        description = "latitude value of the iBeacon",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "longitude",
                        description = "longitude value of the iBeacon",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "sensor.proximity",
                        description = "proximity which will be given by the iBeacon (eg: ENTER, RANGE, EXIT)",
                        type = DataType.STRING
                ),
                @Parameter(
                        name = "sensor.uuid",
                        description = "unique id of the iBeacon",
                        type = DataType.STRING
                ),
                @Parameter(
                        name = "sensor.weight",
                        description = "weight of the iBeacon which influence the averaging of the location " +
                                "(eg: approximate distance from the iBeacon",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "timestamp",
                        description = "timestamp of the log which will be used to remove iBeacon from one's " +
                                "collection when there is no new log for 5 minutes",
                        type = DataType.LONG
                )
        }

)
public class GeoLocationApproximateStreamProcessor
        extends StreamFunctionProcessor<GeoLocationApproximateStreamProcessor.ExtensionState> {

    private Map<String, Map<String, BeaconValueHolder>> personSpecificRecordLocatorMaps;

    //locationRecorder,uuid -> BeaconValueHolder
    private ArrayList<Attribute> attributes;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    protected Object[] process(Object[] data) {
        if (data[0] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to geo:locationApproximate() " +
                    "function. First argument should be string");
        }
        if (data[1] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to geo:locationApproximate() " +
                    "function. Second argument should be double");
        }
        if (data[2] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to geo:locationApproximate() " +
                    "function. Third argument should be double");
        }
        if (data[3] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to geo:locationApproximate() " +
                    "function. Forth argument should be string");
        }
        if (data[4] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to geo:locationApproximate() " +
                    "function. Fifth argument should be string");
        }
        if (data[5] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to geo:locationApproximate() " +
                    "function. Sixth argument should be double");
        }
        if (data[6] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to geo:locationApproximate() " +
                    "function. Seventh argument should be long");
        }

        String locationRecorder = (String) data[0];
        double latitude = (Double) data[1];
        double longitude = (Double) data[2];
        String beaconProximity = (String) data[3];
        String uuid = (String) data[4];
        double weight = (Double) data[5]; //is calculated previously eg: distance
        long timestamp = (Long) data[6];

        if (personSpecificRecordLocatorMaps.get(locationRecorder) == null) {
            personSpecificRecordLocatorMaps.put(locationRecorder, new ConcurrentHashMap<String, BeaconValueHolder>());
        }
        //both "enter" and "range" attributes are there in the logs I retrieve when it comes to cleaning logs
        if ("ENTER".equalsIgnoreCase(beaconProximity) || "RANGE".equalsIgnoreCase(beaconProximity)) {
            if (personSpecificRecordLocatorMaps.get(locationRecorder).containsKey(uuid)) {
                BeaconValueHolder tempBeaconValue = personSpecificRecordLocatorMaps.get(locationRecorder).get(uuid);
                if (tempBeaconValue.getLastUpdatedTime() < timestamp) {
                    BeaconValueHolder beaconValueHolder = new BeaconValueHolder(latitude, longitude, timestamp, weight);
                    personSpecificRecordLocatorMaps.get(locationRecorder).put(uuid, beaconValueHolder);
                }
            } else {
                BeaconValueHolder beaconValueHolder = new BeaconValueHolder(latitude, longitude, timestamp, weight);
                personSpecificRecordLocatorMaps.get(locationRecorder).put(uuid, beaconValueHolder);
            }
        } else {
            if (personSpecificRecordLocatorMaps.get(locationRecorder).containsKey(uuid)) {
                BeaconValueHolder tempBeaconValue = personSpecificRecordLocatorMaps.get(locationRecorder).get(uuid);
                if (tempBeaconValue.getLastUpdatedTime() < timestamp) {
                    personSpecificRecordLocatorMaps.get(locationRecorder).remove(uuid);
                }
            }
        }

        int noOfSensors = personSpecificRecordLocatorMaps.get(locationRecorder).size();
        double sensorValues[][] = new double[noOfSensors][3];
        int actualNoOfSensors = 0;
        double totalWeight = 0;
        for (Map.Entry<String, BeaconValueHolder> beaconLocation : personSpecificRecordLocatorMaps
                .get(locationRecorder).entrySet()) {
            BeaconValueHolder beaconValueHolder = beaconLocation.getValue();
            long prevTimestamp = beaconValueHolder.getLastUpdatedTime();
            if ((timestamp - prevTimestamp) > 300000) {
                //if there is a beacon which has a log older than 5 minutes, removing the beacon assuming the
                //device has gone away from that beacon
                personSpecificRecordLocatorMaps.get(locationRecorder).remove(beaconLocation.getKey());
            } else {
                sensorValues[actualNoOfSensors][0] = beaconValueHolder.getLatitude();
                sensorValues[actualNoOfSensors][1] = beaconValueHolder.getLongitude();
                sensorValues[actualNoOfSensors][2] = beaconValueHolder.getBeaconDistance();
                totalWeight += beaconValueHolder.getBeaconDistance();
                actualNoOfSensors++;
            }
        }
        if (actualNoOfSensors == 0) {
            return new Object[]{latitude, longitude, false};
        }

        double tempLatitude, tempLongitude;
        double x = 0;
        double y = 0;
        double z = 0;
        for (int i = 0; i < actualNoOfSensors; i++) {
            weight = sensorValues[i][2] / totalWeight;
            tempLatitude = sensorValues[i][0] * Math.PI / 180.0;
            tempLongitude = sensorValues[i][1] * Math.PI / 180.0;
            x += Math.cos(tempLatitude) * Math.cos(tempLongitude) * weight;
            y += Math.cos(tempLatitude) * Math.sin(tempLongitude) * weight;
            z += Math.sin(tempLatitude) * weight;
        }
        longitude = Math.atan2(y, x) * 180 / Math.PI;
        double hyp = Math.sqrt(x * x + y * y);
        latitude = Math.atan2(z, hyp) * 180 / Math.PI;

        return new Object[]{latitude, longitude, true};
    }

    @Override
    protected Object[] process(Object data) {
        return new Object[0];
    }

    @Override
    protected StateFactory<ExtensionState> init(AbstractDefinition inputDefinition,
                                                ExpressionExecutor[] attributeExpressionExecutors,
                                                ConfigReader configReader,
                                                boolean outputExpectsExpiredEvents,
                                                SiddhiQueryContext siddhiQueryContext) {
        personSpecificRecordLocatorMaps = new ConcurrentHashMap<String, Map<String, BeaconValueHolder>>();
        if (attributeExpressionExecutors.length != 7) {
            throw new SiddhiAppValidationException("Invalid no of arguments passed to " +
                    "geo:locationApproximate() function, " +
                    "requires 7, but found " + attributeExpressionExecutors.length);
        }
        attributes = new ArrayList<Attribute>(3);
        attributes.add(new Attribute("averagedLatitude", Attribute.Type.DOUBLE));
        attributes.add(new Attribute("averagedLongitude", Attribute.Type.DOUBLE));
        attributes.add(new Attribute("averageExist", Attribute.Type.BOOL));
        return null;
    }

    @Override
    public List<Attribute> getReturnAttributes() {
        return attributes;
    }

    private static class BeaconValueHolder {
        private double latitude;
        private double longitude;
        private long lastUpdatedTime;
        private double beaconDistance;

        BeaconValueHolder(double latitude, double longitude, long lastUpdatedTime, double
                beaconDistance) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.lastUpdatedTime = lastUpdatedTime;
            this.beaconDistance = beaconDistance;
        }

        public long getLastUpdatedTime() {
            return lastUpdatedTime;
        }

        public double getBeaconDistance() {
            return beaconDistance;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    class ExtensionState extends State {

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public Map<String, Object> snapshot() {
            Map<String, Object> map = new HashMap<>();
            map.put("personSpecificRecordLocatorMaps", personSpecificRecordLocatorMaps);
            return map;
        }

        @Override
        public void restore(Map<String, Object> state) {
            personSpecificRecordLocatorMaps = (Map<String, Map<String, BeaconValueHolder>>)
                    state.get("personSpecificRecordLocatorMaps");
        }

    }
}
