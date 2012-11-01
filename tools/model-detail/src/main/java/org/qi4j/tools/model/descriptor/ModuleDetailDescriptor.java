/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
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
{
    private final ModuleDescriptor descriptor;
    private LayerDetailDescriptor layer;
    private final List<ServiceDetailDescriptor> services;
    private final List<ImportedServiceDetailDescriptor> importedServices;
    private final List<EntityDetailDescriptor> entities;
    private final List<CompositeDetailDescriptor> composites;
    private final List<ValueDetailDescriptor> values;
    private final List<ObjectDetailDescriptor> objects;

    ModuleDetailDescriptor( ModuleDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;

        services = new LinkedList<ServiceDetailDescriptor>();
        importedServices = new LinkedList<ImportedServiceDetailDescriptor>();
        entities = new LinkedList<EntityDetailDescriptor>();
        composites = new LinkedList<CompositeDetailDescriptor>();
        values = new LinkedList<ValueDetailDescriptor>();
        objects = new LinkedList<ObjectDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final ModuleDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Services of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final Iterable<ServiceDetailDescriptor> services()
    {
        return services;
    }

    /**
     * @return Values of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.7
     */
    public final Iterable<ImportedServiceDetailDescriptor> importedServices()
    {
        return importedServices;
    }

    /**
     * @return Entities of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final Iterable<EntityDetailDescriptor> entities()
    {
        return entities;
    }

    /**
     * @return Values of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.7
     */
    public final Iterable<ValueDetailDescriptor> values()
    {
        return values;
    }

    /**
     * @return Composites of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final Iterable<CompositeDetailDescriptor> composites()
    {
        return composites;
    }

    /**
     * @return Objects of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final Iterable<ObjectDetailDescriptor> objects()
    {
        return objects;
    }

    /**
     * @return Layer that own this {@code ModuleDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final LayerDetailDescriptor layer()
    {
        return layer;
    }

    final void setLayer( LayerDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        layer = aDescriptor;
    }

    final void addService( ServiceDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setModule( this );
        services.add( aDescriptor );
    }

    final void addImportedService( ImportedServiceDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setModule( this );
        importedServices.add( aDescriptor );
    }

    final void addEntity( EntityDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setModule( this );
        entities.add( aDescriptor );
    }

    final void addValue( ValueDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setModule( this );
        values.add( aDescriptor );
    }

    final void addComposite( CompositeDetailDescriptor aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setModule( this );
        composites.add( aDescriptor );
    }

    final void addObject( ObjectDetailDescriptor aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setModule( this );
        objects.add( aDescriptor );
    }

    @Override
    public final String toString()
    {
        return descriptor.name();
    }
}