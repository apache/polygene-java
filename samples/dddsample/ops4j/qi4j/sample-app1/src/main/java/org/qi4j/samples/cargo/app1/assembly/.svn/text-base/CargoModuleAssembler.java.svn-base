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

import org.qi4j.samples.cargo.app1.model.cargo.Cargo;
import org.qi4j.samples.cargo.app1.model.cargo.Delivery;
import org.qi4j.samples.cargo.app1.model.cargo.HandlingActivity;
import org.qi4j.samples.cargo.app1.model.cargo.Itinerary;
import org.qi4j.samples.cargo.app1.model.cargo.Leg;
import org.qi4j.samples.cargo.app1.model.cargo.RouteSpecification;
import org.qi4j.samples.cargo.app1.model.cargo.TrackingId;
import org.qi4j.samples.cargo.app1.system.factories.DeliveryFactory;
import org.qi4j.samples.cargo.app1.system.factories.HandlingActivityFactory;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

/**
 *
 */
public class CargoModuleAssembler
        implements Assembler {

    public void assemble(ModuleAssembly module) 
            throws AssemblyException {
        module.addEntities(Cargo.class).visibleIn(Visibility.application);
        module.addValues(RouteSpecification.class).visibleIn(Visibility.application);
        module.addValues(
                TrackingId.class,
                HandlingActivity.class,
                Delivery.class,
                Itinerary.class,
                Leg.class
        ).visibleIn(Visibility.module);

        module.addServices(
                HandlingActivityFactory.class,
                DeliveryFactory.class
        ).visibleIn(Visibility.module);
    }
}