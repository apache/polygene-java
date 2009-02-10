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
import java.io.Serializable;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.runtime.service.ServiceReferenceInstance;
import org.qi4j.runtime.service.ImportedServiceModel;
import org.qi4j.runtime.service.ImportedServiceReferenceInstance;
import org.qi4j.api.structure.Module;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceReference;

/**
 * TODO
 */
public class ServicesModel
    implements Serializable
{
    private final Iterable<ServiceModel> serviceModels;
    private List<ImportedServiceModel> importedServiceModels;

    public ServicesModel( Iterable<ServiceModel> serviceModels, List<ImportedServiceModel> importedServiceModels )
    {
        this.serviceModels = serviceModels;
        this.importedServiceModels = importedServiceModels;
    }

    public Iterable<String> getServiceIdentitiesFor( Type serviceType, Visibility visibility )
    {
        List<String> foundServices = new ArrayList<String>();
        for( ServiceModel serviceModel : serviceModels )
        {
            if( serviceModel.isServiceFor( serviceType, visibility ) )
            {
                foundServices.add( serviceModel.identity() );
            }
        }
        for( ImportedServiceModel serviceModel : importedServiceModels )
        {
            if( serviceModel.isServiceFor( serviceType, visibility ) )
            {
                foundServices.add( serviceModel.identity() );
            }
        }
        return foundServices;
    }

    public ServicesInstance newInstance( Module module )
    {
        List<ServiceReference> serviceReferences = new ArrayList<ServiceReference>();
        for( ServiceModel serviceModel : serviceModels )
        {
            ServiceReferenceInstance serviceReferenceInstance = new ServiceReferenceInstance( serviceModel, module );
            serviceReferences.add( serviceReferenceInstance );
        }
        for( ImportedServiceModel serviceModel : importedServiceModels )
        {
            ImportedServiceReferenceInstance serviceReferenceInstance = new ImportedServiceReferenceInstance( serviceModel, module );
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
