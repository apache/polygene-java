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
package org.apache.zest.tools.model.descriptor;

import java.util.LinkedList;
import java.util.List;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.api.util.HierarchicalVisitor;
import org.apache.zest.api.util.VisitableHierarchy;

import static org.apache.zest.api.util.NullArgumentException.validateNotNull;

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
    private final List<LayerDetailDescriptor> layers = new LinkedList<>();

    ApplicationDetailDescriptor( ApplicationDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "ApplicationDescriptor", descriptor );
        this.descriptor = descriptor;
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
        validateNotNull( "ActivatorDetailDescriptor", descriptor );
        descriptor.setApplication( this );
        activators.add( descriptor );
    }

    final void addLayer( LayerDetailDescriptor descriptor )
    {
        validateNotNull( "LayerDetailDescriptor", descriptor );
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
}
