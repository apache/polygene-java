/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.service;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * JAVADOC
 */
public class ServicesModel
    implements VisitableHierarchy<Object, Object>
{
    private final Iterable<ServiceModel> serviceModels;

    public ServicesModel( Iterable<ServiceModel> serviceModels )
    {
        this.serviceModels = serviceModels;
    }

    public ServicesInstance newInstance( ModuleInstance module )
    {
        List<ServiceReference> serviceReferences = new ArrayList<ServiceReference>();
        for( ServiceModel serviceModel : serviceModels )
        {
            ServiceReferenceInstance serviceReferenceInstance = new ServiceReferenceInstance( serviceModel, module );
            serviceReferences.add( serviceReferenceInstance );
        }

        return new ServicesInstance( this, serviceReferences );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( ServiceModel serviceModel : serviceModels )
            {
                if( !serviceModel.accept( visitor ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }
}
