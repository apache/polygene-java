/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.bootstrap;

import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.bootstrap.TransientAssembly;
import org.qi4j.runtime.composite.TransientModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Declaration of a TransientComposite.
 */
public final class TransientAssemblyImpl
    implements TransientAssembly
{
    private Class<? extends TransientComposite> compositeType;
    List<Class<?>> concerns = new ArrayList<Class<?>>();
    List<Class<?>> sideEffects = new ArrayList<Class<?>>();
    List<Class<?>> mixins = new ArrayList<Class<?>>();
    List<Class<?>> roles = new ArrayList<Class<?>>();
    MetaInfo metaInfo = new MetaInfo();
    Visibility visibility = Visibility.module;

    public TransientAssemblyImpl( Class<? extends TransientComposite> compositeType )
    {
        this.compositeType = compositeType;
    }

    @Override
    public Class<? extends TransientComposite> type()
    {
        return compositeType;
    }

    void addTransients( List<TransientModel> aTransients,
                        PropertyDeclarations propertyDeclarations,
                        AssemblyHelper helper
    )
    {
        try
        {
            MetaInfo compositeMetaInfo = new MetaInfo( metaInfo ).withAnnotations( compositeType );
            addAnnotationsMetaInfo( compositeType, compositeMetaInfo );
            TransientModel transientModel = TransientModel.newModel( compositeType,
                                                                     visibility,
                                                                     compositeMetaInfo,
                                                                     propertyDeclarations, concerns, sideEffects, mixins, roles, helper );
            aTransients.add( transientModel );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + compositeType.getName(), e );
        }
    }

    private void addAnnotationsMetaInfo( Class<? extends Composite> type, MetaInfo compositeMetaInfo )
    {
        Class[] declaredInterfaces = type.getInterfaces();
        for( int i = declaredInterfaces.length - 1; i >= 0; i-- )
        {
            addAnnotationsMetaInfo( declaredInterfaces[ i ], compositeMetaInfo );
        }
        compositeMetaInfo.withAnnotations( type );
    }
}
