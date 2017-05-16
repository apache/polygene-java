/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.library.spring.bootstrap.internal.service;

import org.apache.polygene.api.object.ObjectDescriptor;
import org.apache.polygene.api.service.ImportedServiceDescriptor;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.api.structure.LayerDescriptor;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;

final class ServiceLocator
    implements HierarchicalVisitor<Object, Object, RuntimeException>
{
    private final String serviceId;
    private Class<?> serviceType;
    private String moduleName;
    private String layerName;

    private String tempLayerName;
    private String tempModuleName;

    ServiceLocator( String serviceId )
    {
        this.serviceId = serviceId;
    }

    @Override
    public boolean visitEnter( Object visited )
        throws RuntimeException
    {
        if( visited instanceof ApplicationDescriptor )
        {
            return true;
        }
        else if( visited instanceof LayerDescriptor )
        {
            tempLayerName = ( (LayerDescriptor) visited ).name();
            return true;
        }
        else if( visited instanceof ModuleDescriptor )
        {
            tempModuleName = ( (ModuleDescriptor) visited ).name();
            return true;
        }
        else if( visited instanceof ServiceDescriptor )
        {
            ServiceDescriptor aDescriptor = (ServiceDescriptor) visited;
            String identity = aDescriptor.identity().toString();
            if( serviceId.equals( identity ) )
            {
                layerName = tempLayerName;
                moduleName = tempModuleName;
                serviceType = aDescriptor.types().findFirst().orElse( null );
            }
        }
        else if( visited instanceof ObjectDescriptor )
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean visitLeave( Object visited )
        throws RuntimeException
    {
        return true;
    }

    @Override
    public boolean visit( Object visited )
        throws RuntimeException
    {
        if( visited instanceof ImportedServiceDescriptor )
        {
            ImportedServiceDescriptor aDescriptor = (ImportedServiceDescriptor) visited;
            String identity = aDescriptor.identity().toString();
            if( serviceId.equals( identity ) )
            {
                layerName = tempLayerName;
                moduleName = tempModuleName;
                serviceType = aDescriptor.type();
            }
        }

        return true;
    }

    @SuppressWarnings( "unchecked" )
    ServiceReference locateService( Application anApplication )
    {
        if( layerName != null )
        {
            Module module = anApplication.findModule( layerName, moduleName );
            return module.findServices( serviceType )
                         .filter( ref -> ref.identity().toString().equals( serviceId ) )
                         .findFirst().orElse( null );
        }
        return null;
    }
}
