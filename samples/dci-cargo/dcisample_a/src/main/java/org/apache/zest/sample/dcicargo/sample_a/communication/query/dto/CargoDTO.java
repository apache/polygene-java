/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.dcicargo.sample_a.communication.query.dto;

import org.apache.zest.api.common.Optional;
import org.apache.zest.api.property.Property;
import org.apache.zest.library.conversion.values.Unqualified;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.cargo.Cargo;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.cargo.RouteSpecification;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.cargo.TrackingId;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.delivery.Delivery;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.itinerary.Itinerary;
import org.apache.zest.sample.dcicargo.sample_a.infrastructure.conversion.DTO;

/**
 * Cargo DTO
 *
 * We need the @Unqualified annotation since the CargoDTO interface has other properties than {@link Cargo}
 * so that properties can not be directly mapped when we convert from entity to immutable value DTO.
 * With the annotation, property access methods are compared by name instead.
 */
@Unqualified
public interface CargoDTO extends DTO
{
    Property<TrackingId> trackingId();

    // Associated Location entity in Cargo is converted to an immutable LocationDTO value object
    Property<LocationDTO> origin();

    Property<RouteSpecification> routeSpecification();

    Property<Delivery> delivery();

    @Optional
    Property<Itinerary> itinerary();
}