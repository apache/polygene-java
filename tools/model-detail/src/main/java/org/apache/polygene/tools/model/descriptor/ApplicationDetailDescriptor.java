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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;

/**
 * Application Detail Descriptor.
 * <p>
 * Visitable hierarchy with Activators and Layers children.
 */
public final class ApplicationDetailDescriptor
    implements ActivateeDetailDescriptor, VisitableHierarchy<Object, Object>
{
    private final ApplicationDescriptor descriptor;
    private final List<ActivatorDetailDescriptor> activators = new LinkedList<>();
    private final SortedSet<LayerDetailDescriptor> layers;

    ApplicationDetailDescriptor( ApplicationDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "ApplicationDescriptor" );
        this.descriptor = descriptor;
        layers = new TreeSet<>( new UsedLayerComparator() );
    }

    /**
     * @return Descriptor of this {@code ApplicationDetailDescriptor}. Never return {@code null}.
     */
    public final ApplicationDescriptor descriptor()
    {
        return descriptor;
    }

    @Override
    public Iterable<ActivatorDetailDescriptor> activators()
    {
        return activators;
    }

    /**
     * @return Layers of this {@code ApplicationDetailDescriptor}. Never return {@code null}.
     */
    public final Iterable<LayerDetailDescriptor> layers()
    {
        return layers;
    }

    final void addActivator( ActivatorDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ActivatorDetailDescriptor" );
        descriptor.setApplication( this );
        activators.add( descriptor );
    }

    final void addLayer( LayerDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "LayerDetailDescriptor" );
        descriptor.setApplication( this );
        layers.add( descriptor );
    }

    @Override
    public final String toString()
    {
        return descriptor.name();
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
            for( LayerDetailDescriptor layer : layers )
            {
                if( !layer.accept( visitor ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }

    public JsonObject toJson()
    {
        JsonObjectBuilder appBuilder = Json.createObjectBuilder();
        String appName = descriptor().name();
        String version = descriptor().version();
        Application.Mode mode = descriptor().mode();
        appBuilder.add( "name", appName );
        appBuilder.add( "version", version );
        appBuilder.add( "mode", mode.toString() );

        JsonArrayBuilder layersBuilder = Json.createArrayBuilder();
        layers().forEach( layer -> layersBuilder.add( layer.toJson() ) );
        appBuilder.add( "layers", layersBuilder.build() );

        JsonArrayBuilder activatorsBuilder = Json.createArrayBuilder();
        activators().forEach( activator -> activatorsBuilder.add( activator.toJson() ) );
        appBuilder.add( "activators", activatorsBuilder.build() );

        return appBuilder.build();
    }

    private static class UsedLayerComparator
        implements Comparator<LayerDetailDescriptor>
    {

        @Override
        public int compare( LayerDetailDescriptor d1, LayerDetailDescriptor d2 )
        {
            if( d1.equals( d2 ))
            {
                return 0;
            }
            if( uses( d1, d2 ) )
            {
                return -1;
            }
            if( uses(d2, d1) )
            {
                return 1;
            }
            return 0;
        }

        // 0 = same layer
        // 1 = user uses used
        // 2 = not determination
        private boolean uses( LayerDetailDescriptor user, LayerDetailDescriptor used )
        {
            System.out.println("Compare " + user.usedLayers() + " : " + used.usedLayers());
            System.out.println("Compare " + user.usedBy() + " : " + used.usedBy());
            System.out.println("---");
            if( user.equals( used ))
            {
                return true;
            }
            for( LayerDetailDescriptor usedLayer : user.usedLayers() )
            {
                if( uses( usedLayer, used ) )
                {
                    return true;
                }
            }
            return false;
        }
    }
}
