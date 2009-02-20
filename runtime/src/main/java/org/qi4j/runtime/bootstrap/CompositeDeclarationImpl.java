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
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.CompositeDeclaration;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.composite.CompositeModel;

/**
 * Declaration of a Composite. Created by {@link org.qi4j.bootstrap.ModuleAssembly#addComposites(Class[])}.
 */
public final class CompositeDeclarationImpl
    implements CompositeDeclaration, Serializable
{
    private Class<? extends Composite>[] compositeTypes;
    private List<Class<?>> concerns = new ArrayList<Class<?>>( );
    private List<Class<?>> sideEffects = new ArrayList<Class<?>>( );
    private List<Class<?>> mixins = new ArrayList<Class<?>>( );
    private MetaInfo metaInfo = new MetaInfo();
    private Visibility visibility = Visibility.module;

    public CompositeDeclarationImpl( Class<? extends Composite>... compositeTypes )
        throws AssemblyException
    {
        this.compositeTypes = compositeTypes;
    }

    public CompositeDeclaration setMetaInfo( Object info )
    {
        metaInfo.set( info );
        return this;
    }

    public CompositeDeclaration visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public CompositeDeclaration withConcerns( Class<?>... concerns )
    {
        this.concerns.addAll( Arrays.asList(concerns ));
        return this;
    }

    public CompositeDeclaration withSideEffects( Class<?>... sideEffects )
    {
        this.sideEffects.addAll( Arrays.asList(sideEffects ));
        return this;
    }

    public CompositeDeclaration withMixins( Class<?>... mixins )
    {
        this.mixins.addAll(Arrays.asList(mixins));
        return this;
    }

    void addComposites( List<CompositeModel> composites, PropertyDeclarations propertyDeclarations )
    {
        for( Class<? extends Composite> compositeType : compositeTypes )
        {
            MetaInfo compositeMetaInfo = new MetaInfo( metaInfo ).withAnnotations( compositeType );
            addAnnotationsMetaInfo( compositeType, compositeMetaInfo );
            CompositeModel compositeModel = CompositeModel.newModel( compositeType,
                                                                     visibility,
                                                                     compositeMetaInfo,
                                                                     propertyDeclarations, concerns, sideEffects, mixins );
            composites.add( compositeModel );
        }
    }

    private void addAnnotationsMetaInfo( Class<? extends Composite> type, MetaInfo compositeMetaInfo )
    {
        Class[] declaredInterfaces = type.getInterfaces();
        for( int i=declaredInterfaces.length-1 ; i >= 0 ; i-- )
        {
            addAnnotationsMetaInfo( declaredInterfaces[i], compositeMetaInfo );
        }
        compositeMetaInfo.withAnnotations( type );
    }
}
