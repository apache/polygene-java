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
package org.qi4j.library.swing.visualizer.overview.descriptor;

import java.util.LinkedList;
import java.util.List;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class ModuleDetailDescriptor
{
    private final ModuleDescriptor descriptor;

    private final List<ServiceDescriptor> services;
    private final List<EntityDetailDescriptor> entities;
    private final List<CompositeDetailDescriptor> composites;
    private final List<ObjectDescriptor> objects;

    ModuleDetailDescriptor( ModuleDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;

        services = new LinkedList<ServiceDescriptor>();
        entities = new LinkedList<EntityDetailDescriptor>();
        composites = new LinkedList<CompositeDetailDescriptor>();
        objects = new LinkedList<ObjectDescriptor>();
    }

    /**
     * @return Descriptor of this module. Must not return {@code null}.
     * @since 0.5
     */
    public final ModuleDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return services of this module. Must not return {@code null}.
     * @since 0.5
     */
    public final Iterable<ServiceDescriptor> services()
    {
        return services;
    }

    /**
     * @return entities of this module. Must not return {@code null}.
     * @since 0.5
     */
    public final Iterable<EntityDetailDescriptor> entities()
    {
        return entities;
    }

    /**
     * @return composites of this module. Must not return {@code null}.
     * @since 0.5
     */
    public final Iterable<CompositeDetailDescriptor> composites()
    {
        return composites;
    }

    /**
     * @return objects of this module. Must not return {@code null}.
     * @since 0.5
     */
    public final Iterable<ObjectDescriptor> objects()
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

    public void addObject( ObjectDescriptor aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );
        objects.add( aDescriptor );
    }
}