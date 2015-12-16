/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.manual.recipes.createEntity;

import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;

// START SNIPPET: carFactoryMixin2
// START SNIPPET: carFactoryMixin1
public class CarEntityFactoryMixin
    implements CarEntityFactory
{

    // END SNIPPET: carFactoryMixin1
    @Structure
    UnitOfWorkFactory unitOfWorkFactory;

    // END SNIPPET: carFactoryMixin2
// START SNIPPET: carFactoryMixin3
    public CarEntityFactoryMixin( @Structure UnitOfWorkFactory unitOfWorkFactory )
    {
    }

    // END SNIPPET: carFactoryMixin3
// START SNIPPET: createCar
    public Car create( Manufacturer manufacturer, String model )
    {
        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
        EntityBuilder<Car> builder = uow.newEntityBuilder( Car.class );

        Car prototype = builder.instance();
        prototype.manufacturer().set( manufacturer );
        prototype.model().set( model );

        return builder.newInstance();
    }
// END SNIPPET: createCar
}

