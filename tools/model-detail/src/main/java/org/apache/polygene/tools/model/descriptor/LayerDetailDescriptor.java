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
package org.apache.polygene.tools.model.descriptor;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.polygene.api.structure.LayerDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;

/**
 * Layer Detail Descriptor.
 * <p>
 * Visitable hierarchy with Activators and Modules children.
 */
public final class LayerDetailDescriptor
    implements ActivateeDetailDescriptor, VisitableHierarchy<Object, Object>
{
    private final LayerDescriptor descriptor;
    private ApplicationDetailDescriptor application;
    private final List<LayerDetailDescriptor> usedLayers = new LinkedList<>();
    private final List<ActivatorDetailDescriptor> activators = new LinkedList<>();
    private final List<ModuleDetailDescriptor> modules = new LinkedList<>();

    LayerDetailDescriptor( LayerDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "LayerDescriptor" );
        this.descriptor = descriptor;
    }

    /**
     * @return Descriptor of this {@code LayerDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final LayerDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Used layers of this {@code LayerDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final Iterable<LayerDetailDescriptor> usedLayers()
    {
        return usedLayers;
    }

    /**
     * @return Layers that used this layer.
     */
    public final List<LayerDetailDescriptor> usedBy()
    {
        List<LayerDetailDescriptor> usedBy = new LinkedList<>();
        for( LayerDetailDescriptor layer : application.layers() )
        {
            if( layer.usedLayers.contains( this ) )
            {
                usedBy.add( layer );
            }
        }
        return usedBy;
    }

    @Override
    public Iterable<ActivatorDetailDescriptor> activators()
    {
        return activators;
    }

    /**
     * @return Modules of this {@code LayerDetailDescriptor}. Never return {@code null}.
     */
    public final Iterable<ModuleDetailDescriptor> modules()
    {
        return modules;
    }

    /**
     * @return Application that owns this {@code LayerDetailDescriptor}. Never return {@code null}.
     */
    public final ApplicationDetailDescriptor application()
    {
        return application;
    }

    final void setApplication( ApplicationDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ApplicationDetailDescriptor" );
        application = descriptor;
    }

    final void addActivator( ActivatorDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ActivatorDetailDescriptor" );
        descriptor.setLayer( this );
        activators.add( descriptor );
    }

    final void addUsedLayer( LayerDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "LayerDetailDescriptor" );
        usedLayers.add( descriptor );
    }

    final void addModule( ModuleDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ModuleDetailDescriptor" );
        descriptor.setLayer( this );
        modules.add( descriptor );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( ActivatorDetailDescriptor activator : activators )
            {
                if( !activator.accept( visitor ) )
                {
                    break;
                }
            }
            for( ModuleDetailDescriptor module : modules )
            {
                if( !module.accept( visitor ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }

    @Override
    public final String toString()
    {
        return descriptor.name();
    }

    public JsonObject toJson()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add( "name", descriptor().name() );
        {
            JsonArrayBuilder modulesBuilder = Json.createArrayBuilder();
            modules().forEach( module -> modulesBuilder.add( module.toJson() ) );
            builder.add( "modules", modulesBuilder.build() );
        }

        {
            JsonArrayBuilder usedLayersBuilder = Json.createArrayBuilder();
            usedLayers().forEach( layer -> usedLayersBuilder.add( layer.descriptor().name() ) );
            builder.add( "usedLayers", usedLayersBuilder.build() );
        }

        {
            JsonArrayBuilder activatorsBuilder = Json.createArrayBuilder();
            activators().forEach( activator -> activatorsBuilder.add( activator.toJson() ) );
            builder.add( "activators", activatorsBuilder.build() );
        }

        return builder.build();
    }
}
