/*
 * Copyright (C) 2015 WSO2 Inc. (http://wso2.com)
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

package org.wso2.gpl.siddhi.extensions.geo.function;

import org.wso2.gpl.siddhi.extensions.geo.internal.util.IntersectsOperation;

public class GeoIntersectsFunctionExecutor extends AbstractGeoOperationExecutor {
    public GeoIntersectsFunctionExecutor(){
        this.geoOperation = new IntersectsOperation();
    }
}

