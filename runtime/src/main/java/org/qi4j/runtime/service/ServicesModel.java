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

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * JAVADOC
 */
public class ServicesModel
    implements Serializable, Binder
{
    private final Iterable<ServiceModel> serviceModels;

    public ServicesModel( Iterable<ServiceModel> serviceModels )
    {
        this.serviceModels = serviceModels;
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        for( ServiceModel serviceModel : serviceModels )
        {
            serviceModel.bind( resolution );
        }
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

    public void visitModel( ModelVisitor modelVisitor )
    {
        for( ServiceModel serviceModel : serviceModels )
        {
            serviceModel.visitModel( modelVisitor );
        }
    }

    public ServiceModel getServiceFor( Type type, Visibility visibility )
    {
        for( ServiceModel serviceModel : serviceModels )
        {
            if( serviceModel.isServiceFor( type, visibility ) )
            {
                return serviceModel;
            }
        }

        return null;
    }

    public void getServicesFor( Type type, Visibility visibility, List<ServiceModel> models )
    {
        for( ServiceModel serviceModel : serviceModels )
        {
            if( serviceModel.isServiceFor( type, visibility ) )
            {
                models.add( serviceModel );
            }
        }
    }
}
