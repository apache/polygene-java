/*
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.tools.model.descriptor;

import java.util.LinkedList;
import java.util.List;
import org.qi4j.api.structure.ModuleDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class ModuleDetailDescriptor
    implements ActivateeDetailDescriptor
{
    private final ModuleDescriptor descriptor;
    private LayerDetailDescriptor layer;
    private final List<ActivatorDetailDescriptor> activators = new LinkedList<>();
    private final List<ServiceDetailDescriptor> services = new LinkedList<>();
    private final List<ImportedServiceDetailDescriptor> importedServices = new LinkedList<>();
    private final List<EntityDetailDescriptor> entities = new LinkedList<>();
    private final List<CompositeDetailDescriptor> composites = new LinkedList<>();
    private final List<ValueDetailDescriptor> values = new LinkedList<>();
    private final List<ObjectDetailDescriptor> objects = new LinkedList<>();

    ModuleDetailDescriptor( ModuleDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "ModuleDescriptor", descriptor );
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
     * @return Composites of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     */
    public final Iterable<CompositeDetailDescriptor> composites()
    {
        return composites;
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
        validateNotNull( "LayerDetailDescriptor", descriptor );
        layer = descriptor;
    }

    final void addActivator( ActivatorDetailDescriptor descriptor )
    {
        validateNotNull( "ActivatorDetailDescriptor", descriptor );
        descriptor.setModule( this );
        activators.add( descriptor );
    }

    final void addService( ServiceDetailDescriptor descriptor )
    {
        validateNotNull( "ServiceDetailDescriptor", descriptor );
        descriptor.setModule( this );
        services.add( descriptor );
    }

    final void addImportedService( ImportedServiceDetailDescriptor descriptor )
    {
        validateNotNull( "ImportedServiceDetailDescriptor", descriptor );
        descriptor.setModule( this );
        importedServices.add( descriptor );
    }

    final void addEntity( EntityDetailDescriptor descriptor )
    {
        validateNotNull( "EntityDetailDescriptor", descriptor );
        descriptor.setModule( this );
        entities.add( descriptor );
    }

    final void addValue( ValueDetailDescriptor descriptor )
    {
        validateNotNull( "ValueDetailDescriptor", descriptor );
        descriptor.setModule( this );
        values.add( descriptor );
    }

    final void addComposite( CompositeDetailDescriptor descriptor )
    {
        validateNotNull( "CompositeDetailDescriptor", descriptor );
        descriptor.setModule( this );
        composites.add( descriptor );
    }

    final void addObject( ObjectDetailDescriptor descriptor )
    {
        validateNotNull( "ObjectDetailDescriptor", descriptor );
        descriptor.setModule( this );
        objects.add( descriptor );
    }

    @Override
    public final String toString()
    {
        return descriptor.name();
    }

}
