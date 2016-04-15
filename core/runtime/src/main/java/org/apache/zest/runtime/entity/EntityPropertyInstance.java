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
package org.apache.zest.runtime.entity;

import org.apache.zest.runtime.property.PropertyInfo;
import org.apache.zest.runtime.property.PropertyInstance;
import org.apache.zest.spi.entity.EntityState;

/**
 * {@code EntityPropertyInstance} represents a property whose value must be backed by an EntityState.
 */
public class EntityPropertyInstance<T>
    extends PropertyInstance<T>
{
    private final EntityState entityState;

    /**
     * Construct an instance of {@code PropertyInstance} with the specified arguments.
     *
     * @param aPropertyInfo The property info. This argument must not be {@code null}.
     * @param entityState EntityState
     */
    @SuppressWarnings( "unchecked" )
    public EntityPropertyInstance( PropertyInfo aPropertyInfo, EntityState entityState )
    {
        super( aPropertyInfo, (T) entityState.propertyValueOf( aPropertyInfo.qualifiedName() ) );
        this.entityState = entityState;
    }

    /**
     * Sets this property value.
     *
     * @param aNewValue The new value.
     */
    @Override
    public void set( T aNewValue )
    {
        super.set( aNewValue );
        entityState.setPropertyValue( model.qualifiedName(), aNewValue );
    }
}