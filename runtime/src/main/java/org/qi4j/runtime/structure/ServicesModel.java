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

package org.qi4j.runtime.structure;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.runtime.service.ServiceReferenceInstance;
import org.qi4j.structure.Module;
import org.qi4j.structure.Visibility;

/**
 * TODO
 */
public class ServicesModel
{
    private final Iterable<ServiceModel> serviceModels;

    public ServicesModel( Iterable<ServiceModel> serviceModels )
    {
        this.serviceModels = serviceModels;
    }

    public Iterable<ServiceModel> getServiceModelsFor( Type serviceType, Visibility visibility )
    {
        List<ServiceModel> foundServices = new ArrayList<ServiceModel>();
        for( ServiceModel serviceModel : serviceModels )
        {
            if( serviceModel.isServiceFor( serviceType, visibility ) )
            {
                foundServices.add( serviceModel );
            }
        }
        return foundServices;
    }

    public ServicesInstance newInstance( Module module )
    {
        List<ServiceReferenceInstance> serviceReferences = new ArrayList<ServiceReferenceInstance>();
        for( ServiceModel serviceModel : serviceModels )
        {
            ServiceReferenceInstance serviceReferenceInstance = new ServiceReferenceInstance( serviceModel, module );
            serviceReferences.add( serviceReferenceInstance );
        }

        return new ServicesInstance( this, serviceReferences );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        for( ServiceModel serviceModel : serviceModels )
        {
            serviceModel.visitModel( modelVisitor );
        }
    }
}
