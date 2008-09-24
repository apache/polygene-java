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
package org.qi4j.library.swing.visualizer.model;

import java.util.LinkedList;
import java.util.List;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @see ModuleDescriptor
 * @since 0.5
 */
public final class ModuleDetailDescriptor
{
    private final ModuleDescriptor descriptor;

    private final List<ServiceDescriptor> services;
    private final List<EntityDetailDescriptor> entities;
    private final List<CompositeDetailDescriptor> composites;
    private final List<ObjectDetailDescriptor> objects;

    ModuleDetailDescriptor( ModuleDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;

        services = new LinkedList<ServiceDescriptor>();
        entities = new LinkedList<EntityDetailDescriptor>();
        composites = new LinkedList<CompositeDetailDescriptor>();
        objects = new LinkedList<ObjectDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final ModuleDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Services of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final Iterable<ServiceDescriptor> services()
    {
        return services;
    }

    /**
     * @return Entities of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final Iterable<EntityDetailDescriptor> entities()
    {
        return entities;
    }

    /**
     * @return Composites of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final Iterable<CompositeDetailDescriptor> composites()
    {
        return composites;
    }

    /**
     * @return Objects of this {@code ModuleDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final Iterable<ObjectDetailDescriptor> objects()
    {
        return objects;
    }

    final void addService( ServiceDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        services.add( aDescriptor );
    }

    final void addEntity( EntityDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        entities.add( aDescriptor );
    }

    final void addComposite( CompositeDetailDescriptor aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );
        composites.add( aDescriptor );
    }

    final void addObject( ObjectDetailDescriptor aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );
        objects.add( aDescriptor );
    }
}