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

package org.qi4j.runtime.object;

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.composite.ConstructorsModel;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;

import static org.qi4j.functional.Iterables.iterable;

/**
 * JAVADOC
 */
public final class ObjectModel
    implements ObjectDescriptor, VisitableHierarchy<Object, Object>
{
    private final Class<?> objectType;
    private final Visibility visibility;
    private final MetaInfo metaInfo;
    private final ConstructorsModel constructorsModel;
    private final InjectedFieldsModel injectedFieldsModel;
    private final InjectedMethodsModel injectedMethodsModel;

    public ObjectModel( Class<?> objectType,
                        Visibility visibility,
                        MetaInfo metaInfo
    )
    {
        this.objectType = objectType;
        this.visibility = visibility;
        this.metaInfo = metaInfo;

        constructorsModel = new ConstructorsModel( objectType );
        injectedFieldsModel = new InjectedFieldsModel( objectType );
        injectedMethodsModel = new InjectedMethodsModel( objectType );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Iterable<Class<?>> types()
    {
        Iterable<? extends Class<?>> iterable = iterable( objectType );
        return (Iterable<Class<?>>) iterable;
    }

    @Override
    public Visibility visibility()
    {
        return visibility;
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
            catch( InitializationException e )
            {
                throw new ConstructionException( "Unable to initialize " + objectType, e );
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
