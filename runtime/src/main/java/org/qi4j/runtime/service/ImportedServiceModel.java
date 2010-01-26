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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.runtime.structure.ModelVisitor;

/**
 * JAVADOC
 */
public final class ImportedServiceModel
    implements ImportedServiceDescriptor, Serializable
{
    private final Class<? extends ServiceComposite> type;
    private final Visibility visibility;
    private final Class<? extends ServiceImporter> serviceImporter;
    private final String identity;
    private final MetaInfo metaInfo;
    private String moduleName;

    public ImportedServiceModel( Class<? extends ServiceComposite> serviceType,
                                 Visibility visibility,
                                 Class<? extends ServiceImporter> serviceImporter,
                                 String identity,
                                 MetaInfo metaInfo, String moduleName
    )
    {
        type = serviceType;
        this.visibility = visibility;
        this.serviceImporter = serviceImporter;
        this.identity = identity;
        this.metaInfo = metaInfo;
        this.moduleName = moduleName;
    }

    public Class<? extends ServiceComposite> type()
    {
        return type;
    }

    public Visibility visibility()
    {
        return visibility;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public Class<? extends ServiceImporter> serviceImporter()
    {
        return serviceImporter;
    }

    public String identity()
    {
        return identity;
    }

    public String moduleName()
    {
        return moduleName;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );
    }

    public boolean isServiceFor( Type serviceType, Visibility visibility )
    {
        // Check visibility
        if( visibility != this.visibility )
        {
            return false;
        }

        // Check types
        if( serviceType instanceof Class )
        {
            // Plain class check
            Class serviceClass = (Class) serviceType;
            return serviceClass.isAssignableFrom( type );
        }
        else if( serviceType instanceof ParameterizedType )
        {
            // Parameterized type check. This is useful for example Wrapper<Foo> usages
            ParameterizedType paramType = (ParameterizedType) serviceType;
            Class rawClass = (Class) paramType.getRawType();
            Set<Type> types = Classes.genericInterfacesOf( type );
            for( Type type1 : types )
            {
                if( type1 instanceof ParameterizedType && rawClass.isAssignableFrom( Classes.getRawClass( type1 ) ) )
                {
                    // Check params
                    Type[] actualTypes = paramType.getActualTypeArguments();
                    Type[] actualServiceTypes = ( (ParameterizedType) type1 ).getActualTypeArguments();
                    for( int i = 0; i < actualTypes.length; i++ )
                    {
                        Type actualType = actualTypes[ i ];
                        if( actualType instanceof Class )
                        {
                            Class actualClass = (Class) actualType;
                            Class actualServiceType = (Class) actualServiceTypes[ i ];
                            if( !actualClass.isAssignableFrom( actualServiceType ) )
                            {
                                return false;
                            }
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public <T> ImportedServiceInstance<T> importInstance( Module module )
    {
        ServiceImporter importer = module.objectBuilderFactory().newObject( serviceImporter );
        try
        {
            T instance = (T) importer.importService( this );
            return new ImportedServiceInstance<T>( instance, importer );
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