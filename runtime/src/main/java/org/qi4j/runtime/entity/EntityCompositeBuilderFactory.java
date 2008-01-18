/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity;

import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.structure.ModuleCompositeBuilderFactory;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.entity.EntityStore;

/**
 * TODO
 */
public class EntityCompositeBuilderFactory
    extends ModuleCompositeBuilderFactory
{
    private EntitySessionInstance entitySession;
    private EntityStore store;

    public EntityCompositeBuilderFactory( ModuleInstance moduleInstance, EntitySessionInstance entitySession, EntityStore store )
    {
        super( moduleInstance );
        this.store = store;
        this.entitySession = entitySession;
    }

    @Override protected <T extends Composite> CompositeBuilder<T> createBuilder( ModuleInstance moduleInstance, CompositeContext compositeContext )
    {
        // Create a builder
        CompositeBuilder<T> builder = new EntitySessionCompositeBuilder<T>( moduleInstance, compositeContext, entitySession, store );
        return builder;
    }
}
