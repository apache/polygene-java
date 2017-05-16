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

package org.apache.polygene.runtime.bootstrap;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.ConfigurationDeclaration;

import static java.util.Arrays.asList;

/**
 * Declaration of a Composite. Created by {@link org.apache.polygene.bootstrap.ModuleAssembly#configurations(Class[])}.
 */
public final class ConfigurationDeclarationImpl
    implements ConfigurationDeclaration
{
    private final Iterable<EntityAssemblyImpl> entities;
    private final Iterable<ValueAssemblyImpl> values;

    public ConfigurationDeclarationImpl( Iterable<EntityAssemblyImpl> entities, Iterable<ValueAssemblyImpl> values  )
    {
        this.entities = entities;
        this.values = values;
    }

    @Override
    public ConfigurationDeclaration setMetaInfo( Object info )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.metaInfo.set( info );
        }
        for( ValueAssemblyImpl value : values )
        {
            value.metaInfo.set( info );
        }
        return this;
    }

    @Override
    public ConfigurationDeclaration visibleIn( Visibility visibility )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.visibility = visibility;
        }
        for( ValueAssemblyImpl value : values )
        {
            value.visibility = visibility;
        }
        return this;
    }

    @Override
    public ConfigurationDeclaration withConcerns( Class<?>... concerns )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.concerns.addAll( asList( concerns ) );
        }
        for( ValueAssemblyImpl value : values )
        {
            value.concerns.addAll( asList( concerns ) );
        }
        return this;
    }

    @Override
    public ConfigurationDeclaration withSideEffects( Class<?>... sideEffects )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.sideEffects.addAll( asList( sideEffects ) );
        }
        for( ValueAssemblyImpl value : values )
        {
            value.sideEffects.addAll( asList( sideEffects ) );
        }
        return this;
    }

    @Override
    public ConfigurationDeclaration withMixins( Class<?>... mixins )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.mixins.addAll( asList( mixins ) );
        }
        for( ValueAssemblyImpl value : values )
        {
            value.mixins.addAll( asList( mixins ) );
        }
        return this;
    }

    @Override
    public ConfigurationDeclaration withTypes( Class<?>... types )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.types.addAll( asList( types ) );
        }
        for( ValueAssemblyImpl value : values )
        {
            value.types.addAll( asList( types ) );
        }
        return this;
    }
}