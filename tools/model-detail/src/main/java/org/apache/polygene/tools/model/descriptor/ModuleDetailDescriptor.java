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
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;

/**
 * Module Detail Descriptor.
 * <p>
 * Visitable hierarchy with Activators and Composites children.
 */
public final class ModuleDetailDescriptor
    implements ActivateeDetailDescriptor, VisitableHierarchy<Object, Object>
{
    private final ModuleDescriptor descriptor;
    private LayerDetailDescriptor layer;
    private final List<ActivatorDetailDescriptor> activators = new LinkedList<>();
    private final List<ServiceDetailDescriptor> services = new LinkedList<>();
    private final List<ImportedServiceDetailDescriptor> importedServices = new LinkedList<>();
    private final List<EntityDetailDescriptor> entities = new LinkedList<>();
    private final List<TransientDetailDescriptor> transients = new LinkedList<>();
    private final List<ValueDetailDescriptor> values = new LinkedList<>();
    private final List<ObjectDetailDescriptor> objects = new LinkedList<>();

    ModuleDetailDescriptor( ModuleDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "ModuleDescriptor" );
        this.descriptor = descriptor;
    }

    /**
     * @return Descriptor of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     */
    public final ModuleDescriptor descriptor()
    {
        return descriptor;
    }

    @Override
    public Iterable<ActivatorDetailDescriptor> activators()
    {
        return activators;
    }

    /**
     * @return Services of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     */
    public final Iterable<ServiceDetailDescriptor> services()
    {
        return services;
    }

    /**
     * @return Values of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     */
    public final Iterable<ImportedServiceDetailDescriptor> importedServices()
    {
        return importedServices;
    }

    /**
     * @return Entities of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     */
    public final Iterable<EntityDetailDescriptor> entities()
    {
        return entities;
    }

    /**
     * @return Values of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     */
    public final Iterable<ValueDetailDescriptor> values()
    {
        return values;
    }

    /**
     * @return Transients of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     */
    public final Iterable<TransientDetailDescriptor> transients()
    {
        return transients;
    }

    /**
     * @return Objects of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     */
    public final Iterable<ObjectDetailDescriptor> objects()
    {
        return objects;
    }

    /**
     * @return Layer that own this {@code ModuleDetailDescriptor}. Never return {@code null}.
     */
    public final LayerDetailDescriptor layer()
    {
        return layer;
    }

    final void setLayer( LayerDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "LayerDetailDescriptor" );
        layer = descriptor;
    }

    final void addActivator( ActivatorDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ActivatorDetailDescriptor" );
        descriptor.setModule( this );
        activators.add( descriptor );
    }

    final void addService( ServiceDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ServiceDetailDescriptor" );
        descriptor.setModule( this );
        services.add( descriptor );
    }

    final void addImportedService( ImportedServiceDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ImportedServiceDetailDescriptor" );
        descriptor.setModule( this );
        importedServices.add( descriptor );
    }

    final void addEntity( EntityDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "EntityDetailDescriptor" );
        descriptor.setModule( this );
        entities.add( descriptor );
    }

    final void addValue( ValueDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ValueDetailDescriptor" );
        descriptor.setModule( this );
        values.add( descriptor );
    }

    final void addTransient( TransientDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "TransientDetailDescriptor" );
        descriptor.setModule( this );
        transients.add( descriptor );
    }

    final void addObject( ObjectDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ObjectDetailDescriptor" );
        descriptor.setModule( this );
        objects.add( descriptor );
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
            for( ServiceDetailDescriptor service : services )
            {
                if( !service.accept( visitor ) )
                {
                    break;
                }
            }
            for( ImportedServiceDetailDescriptor importedService : importedServices )
            {
                if( !importedService.accept( visitor ) )
                {
                    break;
                }
            }
            for( EntityDetailDescriptor entity : entities )
            {
                if( !entity.accept( visitor ) )
                {
                    break;
                }
            }
            for( ValueDetailDescriptor value : values )
            {
                if( !value.accept( visitor ) )
                {
                    break;
                }
            }
            for( TransientDetailDescriptor composite : transients )
            {
                if( !composite.accept( visitor ) )
                {
                    break;
                }
            }
            for( ObjectDetailDescriptor object : objects )
            {
                if( !object.accept( visitor ) )
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
}
