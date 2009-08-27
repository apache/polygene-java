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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.bootstrap.TransientDeclaration;
import org.qi4j.runtime.composite.TransientModel;

/**
 * Declaration of a Composite. Created by {@link org.qi4j.bootstrap.ModuleAssembly#addTransients(Class[])}.
 */
public final class TransientDeclarationImpl
    implements TransientDeclaration, Serializable
{
    private Class<? extends TransientComposite>[] compositeTypes;
    private List<Class<?>> concerns = new ArrayList<Class<?>>();
    private List<Class<?>> sideEffects = new ArrayList<Class<?>>();
    private List<Class<?>> mixins = new ArrayList<Class<?>>();
    private MetaInfo metaInfo = new MetaInfo();
    private Visibility visibility = Visibility.module;

    public TransientDeclarationImpl( Class<? extends TransientComposite>... compositeTypes )
        throws AssemblyException
    {
        this.compositeTypes = compositeTypes;
    }

    public TransientDeclaration setMetaInfo( Object info )
    {
        metaInfo.set( info );
        return this;
    }

    public TransientDeclaration visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public TransientDeclaration withConcerns( Class<?>... concerns )
    {
        this.concerns.addAll( Arrays.asList( concerns ) );
        return this;
    }

    public TransientDeclaration withSideEffects( Class<?>... sideEffects )
    {
        this.sideEffects.addAll( Arrays.asList( sideEffects ) );
        return this;
    }

    public TransientDeclaration withMixins( Class<?>... mixins )
    {
        this.mixins.addAll( Arrays.asList( mixins ) );
        return this;
    }

    void addComposites( List<TransientModel> aTransients, PropertyDeclarations propertyDeclarations )
    {
        for( Class<? extends TransientComposite> compositeType : compositeTypes )
        {
            try
            {
                MetaInfo compositeMetaInfo = new MetaInfo( metaInfo ).withAnnotations( compositeType );
                addAnnotationsMetaInfo( compositeType, compositeMetaInfo );
                TransientModel transientModel = TransientModel.newModel( compositeType,
                                                                         visibility,
                                                                         compositeMetaInfo,
                                                                         propertyDeclarations, concerns, sideEffects, mixins );
                aTransients.add( transientModel );
            }
            catch( Exception e )
            {
                throw new InvalidApplicationException( "Could not register " + compositeType.getName(), e );
            }
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
