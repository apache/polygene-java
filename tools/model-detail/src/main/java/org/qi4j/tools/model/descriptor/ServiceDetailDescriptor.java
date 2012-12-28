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
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceDescriptor;

public final class ServiceDetailDescriptor
    extends CompositeDetailDescriptor<ServiceDescriptor>
{
    ServiceDetailDescriptor( ServiceDescriptor aDescriptor )
    {
        super( aDescriptor );
    }

    /**
     * @return layers that can access this service. Never return {@code null}.
     *
     * @since 0.5
     */
    public final List<LayerDetailDescriptor> accessibleToLayers()
    {
        Visibility visibility = descriptor.visibility();
        if( visibility == Visibility.module )
        {
            return new LinkedList<LayerDetailDescriptor>();
        }

        LayerDetailDescriptor layer = module.layer();
        if( visibility == Visibility.layer )
        {
            List<LayerDetailDescriptor> layers = new LinkedList<LayerDetailDescriptor>();
            layers.add( layer );
            return layers;
        }
        else
        {
            return layer.usedBy();
        }
    }
}