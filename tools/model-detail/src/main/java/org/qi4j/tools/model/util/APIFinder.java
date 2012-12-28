/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.tools.model.util;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.tools.model.descriptor.LayerDetailDescriptor;
import org.qi4j.tools.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.tools.model.descriptor.ServiceDetailDescriptor;

/**
 * API would be defined as "All service interfaces which are visible for layer
 * or application"
 */
class APIFinder
{

    public List<ServiceDetailDescriptor> findModuleAPI( ModuleDetailDescriptor descriptor )
    {
        ArrayList<ServiceDetailDescriptor> list = new ArrayList<ServiceDetailDescriptor>();

        for( ServiceDetailDescriptor serviceDetailDescriptor : descriptor.services() )
        {
            list.add( serviceDetailDescriptor );
        }

        return list;
    }

    public List<ServiceDetailDescriptor> findLayerAPI( LayerDetailDescriptor descriptor )
    {
        ArrayList<ServiceDetailDescriptor> list = new ArrayList<ServiceDetailDescriptor>();

        for( ModuleDetailDescriptor moduleDetailDescriptor : descriptor.modules() )
        {
            list.addAll( findModuleAPI( moduleDetailDescriptor ) );
        }

        return list;
    }
}
