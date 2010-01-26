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

import java.io.Serializable;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.composite.ConstructorsModel;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.object.ObjectDescriptor;

/**
 * JAVADOC
 */
public final class ObjectModel
    implements Binder, ObjectDescriptor, Serializable
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

    public Class<?> type()
    {
        return objectType;
    }

    public Visibility visibility()
    {
        return visibility;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        constructorsModel.visitModel( modelVisitor );
        injectedFieldsModel.visitModel( modelVisitor );
        injectedMethodsModel.visitModel( modelVisitor );
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), this, null, null );

        constructorsModel.bind( resolution );
        injectedFieldsModel.bind( resolution );
        injectedMethodsModel.bind( resolution );
    }

    public Object newInstance( InjectionContext injectionContext )
    {
        Object instance = constructorsModel.newInstance( injectionContext );
        injectedFieldsModel.inject( injectionContext, instance );
        injectedMethodsModel.inject( injectionContext, instance );

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
