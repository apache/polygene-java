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
package org.apache.zest.runtime.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.service.ImportedServiceDescriptor;
import org.apache.zest.api.service.ServiceImporter;
import org.apache.zest.api.service.ServiceImporterException;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.util.HierarchicalVisitor;
import org.apache.zest.api.util.VisitableHierarchy;
import org.apache.zest.runtime.activation.ActivatorsInstance;
import org.apache.zest.runtime.activation.ActivatorsModel;

/**
 * JAVADOC
 */
public final class ImportedServiceModel
    implements ImportedServiceDescriptor, VisitableHierarchy<Object, Object>
{
    private final ModuleDescriptor module;
    private final Class<?> type;
    private final Visibility visibility;
    @SuppressWarnings( "raw" )
    private final Class<? extends ServiceImporter> serviceImporter;
    private final Identity identity;
    private final boolean importOnStartup;
    private final MetaInfo metaInfo;
    private final ActivatorsModel<?> activatorsModel;
    private final String moduleName;

    @SuppressWarnings( "raw" )
    public ImportedServiceModel( ModuleDescriptor module,
                                 Class serviceType,
                                 Visibility visibility,
                                 Class<? extends ServiceImporter> serviceImporter,
                                 Identity identity,
                                 boolean importOnStartup,
                                 MetaInfo metaInfo,
                                 ActivatorsModel<?> activatorsModel,
                                 String moduleName
    )
    {
        this.module = module;
        type = serviceType;
        this.visibility = visibility;
        this.serviceImporter = serviceImporter;
        this.identity = identity;
        this.importOnStartup = importOnStartup;
        this.metaInfo = metaInfo;
        this.activatorsModel = activatorsModel;
        this.moduleName = moduleName;
    }

    public boolean isImportOnStartup()
    {
        return importOnStartup;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Stream<Class<?>> types()
    {
        return Stream.of( type );
    }

    @Override
    public Visibility visibility()
    {
        return visibility;
    }

    @Override
    public ModuleDescriptor module()
    {
        return module;
    }

    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    @Override
    @SuppressWarnings( "raw" )
    public Class<? extends ServiceImporter> serviceImporter()
    {
        return serviceImporter;
    }

    @Override
    public Class<?> type()
    {
        return type;
    }

    @Override
    public Identity identity()
    {
        return identity;
    }

    public String moduleName()
    {
        return moduleName;
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    public ActivatorsInstance<?> newActivatorsInstance( ModuleDescriptor module )
        throws Exception
    {
        return new ActivatorsInstance( activatorsModel.newInstances( module ) );
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        return this.type.isAssignableFrom( type );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            activatorsModel.accept( visitor );
        }
        return visitor.visitLeave( this );
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    public <T> ImportedServiceInstance<T> importInstance( ModuleDescriptor module )
    {
        try
        {
            ServiceImporter importer = module.instance().newObject( serviceImporter );
            T instance = (T) importer.importService( this );
            return new ImportedServiceInstance<>( instance, importer );
        }
        catch( ServiceImporterException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new ServiceImporterException( "Could not import service " + identity, e );
        }
    }

    @SuppressWarnings( "raw" )
    public Object newProxy( InvocationHandler serviceInvocationHandler )
    {
        if( type.isInterface() )
        {
            return Proxy.newProxyInstance( type.getClassLoader(),
                                           new Class[]{ type },
                                           serviceInvocationHandler );
        }
        else
        {
            Class[] interfaces = type.getInterfaces();
            return Proxy.newProxyInstance( type.getClassLoader(),
                                           interfaces,
                                           serviceInvocationHandler );
        }
    }

    @Override
    public String toString()
    {
        return type.getName() + ":" + identity;
    }
}
