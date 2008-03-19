/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.injection;

import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.service.ServiceLocator;

/**
 * TODO
 */
public class StructureContext
{
    private CompositeBuilderFactory compositeBuilderFactory;
    private ObjectBuilderFactory objectBuilderFactory;
    private UnitOfWorkFactory unitOfWorkFactory;
    private ServiceLocator serviceLocator;

    public StructureContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, UnitOfWorkFactory unitOfWorkFactory, ServiceLocator serviceLocator )
    {
        this.compositeBuilderFactory = compositeBuilderFactory;
        this.objectBuilderFactory = objectBuilderFactory;
        this.unitOfWorkFactory = unitOfWorkFactory;
        this.serviceLocator = serviceLocator;
    }

    public CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return compositeBuilderFactory;
    }

    public ObjectBuilderFactory getObjectBuilderFactory()
    {
        return objectBuilderFactory;
    }

    public UnitOfWorkFactory getUnitOfWorkFactory()
    {
        return unitOfWorkFactory;
    }

    public ServiceLocator getServiceLocator()
    {
        return serviceLocator;
    }
}
