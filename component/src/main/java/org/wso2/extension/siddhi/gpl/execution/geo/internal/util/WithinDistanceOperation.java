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

package org.wso2.extension.siddhi.gpl.execution.geo.internal.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import org.wso2.siddhi.query.api.definition.Attribute;
/**
 * check whether the given area is within distance the another area
 * **/
public class WithinDistanceOperation extends GeoOperation {
    @Override
    public Object operation(Geometry a, Geometry b, Object[] data) {
        return a.isWithinDistance(b, (Double) data[data.length - 1] / GeometryUtils.TO_DEGREE);
    }

    @Override
    public Object operation(Geometry a, PreparedGeometry b, Object[] data) {
        return a.isWithinDistance(b.getGeometry(), (Double) data[data.length - 1] / GeometryUtils.TO_DEGREE);
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.BOOL;
    }
}
