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

package org.apache.polygene.runtime.object;

import java.util.stream.Stream;
import org.apache.polygene.api.common.ConstructionException;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.mixin.Initializable;
import org.apache.polygene.api.mixin.InitializationException;
import org.apache.polygene.api.object.ObjectDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;
import org.apache.polygene.runtime.composite.ConstructorsModel;
import org.apache.polygene.runtime.injection.InjectedFieldsModel;
import org.apache.polygene.runtime.injection.InjectedMethodsModel;
import org.apache.polygene.runtime.injection.InjectionContext;

/**
 * JAVADOC
 */
public final class ObjectModel
    implements ObjectDescriptor, VisitableHierarchy<Object, Object>
{
    private final ModuleDescriptor module;
    private final Class<?> objectType;
    private final Visibility visibility;
    private final MetaInfo metaInfo;
    private final ConstructorsModel constructorsModel;
    private final InjectedFieldsModel injectedFieldsModel;
    private final InjectedMethodsModel injectedMethodsModel;

    public ObjectModel( ModuleDescriptor module,
                        Class<?> objectType,
                        Visibility visibility,
                        MetaInfo metaInfo
    )
    {
        this.module = module;
        this.objectType = objectType;
        this.visibility = visibility;
        this.metaInfo = metaInfo;

        constructorsModel = new ConstructorsModel( objectType );
        injectedFieldsModel = new InjectedFieldsModel( objectType );
        injectedMethodsModel = new InjectedMethodsModel( objectType );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Stream<Class<?>> types()
    {
        return Stream.of( objectType );
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
    public boolean isAssignableTo( Class<?> type )
    {
        return type.isAssignableFrom( objectType );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            if( constructorsModel.accept( visitor ) )
            {
                if( injectedFieldsModel.accept( visitor ) )
                {
                    injectedMethodsModel.accept( visitor );
                }
            }
        }
        return visitor.visitLeave( this );
    }

    public Object newInstance( InjectionContext injectionContext )
    {
        Object instance;
        try
        {
            instance = constructorsModel.newInstance( injectionContext );
            injectionContext = new InjectionContext( injectionContext.module(), injectionContext.uses(), instance );
            injectedFieldsModel.inject( injectionContext, instance );
            injectedMethodsModel.inject( injectionContext, instance );
        }
        catch( Exception e )
        {
            throw new ConstructionException( "Could not instantiate " + objectType.getName(), e );
        }

        if( instance instanceof Initializable )
        {
            try
            {
                ( (Initializable) instance ).initialize();
            }
            catch( Exception e )
            {
                String message = "Unable to initialize " + objectType;
                throw new ConstructionException( new InitializationException( message, e ) );
            }
        }

        return instance;
    }

    public void inject( InjectionContext injectionContext, Object instance )
    {
        injectedFieldsModel.inject( injectionContext, instance );
        injectedMethodsModel.inject( injectionContext, instance );
    }

    @Override
    public String toString()
    {
        return objectType.getName();
    }
}
