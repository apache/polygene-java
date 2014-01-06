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
import org.qi4j.api.structure.ApplicationDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class ApplicationDetailDescriptor
    implements ActivateeDetailDescriptor
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

}
