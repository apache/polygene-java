/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.samples.cargo.app1.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.samples.cargo.app1.model.cargo.Cargo;
import org.qi4j.samples.cargo.app1.model.cargo.Delivery;
import org.qi4j.samples.cargo.app1.model.cargo.HandlingActivity;
import org.qi4j.samples.cargo.app1.model.cargo.Itinerary;
import org.qi4j.samples.cargo.app1.model.cargo.Leg;
import org.qi4j.samples.cargo.app1.model.cargo.RouteSpecification;
import org.qi4j.samples.cargo.app1.model.cargo.TrackingId;
import org.qi4j.samples.cargo.app1.system.factories.CargoFactory;
import org.qi4j.samples.cargo.app1.system.factories.DeliveryFactory;
import org.qi4j.samples.cargo.app1.system.factories.HandlingActivityFactory;
import org.qi4j.samples.cargo.app1.system.factories.LegFactory;
import org.qi4j.samples.cargo.app1.system.factories.RouteSpecificationFactory;
import org.qi4j.samples.cargo.app1.system.repositories.CargoRepository;

/**
 *
 */
public class CargoModuleAssembler
    implements Assembler
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Cargo.class ).visibleIn( Visibility.module );

        module.values( RouteSpecification.class ).visibleIn( Visibility.module );

        module.services( RouteSpecificationFactory.class,
                         CargoFactory.class,
                         CargoRepository.class ).visibleIn( Visibility.application );


        module.values(
            TrackingId.class,
            HandlingActivity.class,
            Delivery.class,
            Itinerary.class,
            Leg.class
        ).visibleIn( Visibility.module );

        module.services( LegFactory.class ).visibleIn( Visibility.layer );


        module.services(
            HandlingActivityFactory.class,
            DeliveryFactory.class
        ).visibleIn( Visibility.module );
    }
}