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
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.bootstrap.ValueDeclaration;
import org.qi4j.runtime.value.ValueModel;

/**
 * Declaration of a ValueComposite. Created by {@link org.qi4j.bootstrap.ModuleAssembly#addValues(Class[])}.
 */
public final class ValueDeclarationImpl
    implements ValueDeclaration, Serializable
{
    private Class<? extends ValueComposite>[] compositeTypes;
    private List<Class<?>> concerns = new ArrayList<Class<?>>();
    private List<Class<?>> sideEffects = new ArrayList<Class<?>>();
    private List<Class<?>> mixins = new ArrayList<Class<?>>();
    private MetaInfo metaInfo = new MetaInfo();
    private Visibility visibility = Visibility.module;

    public ValueDeclarationImpl( Class<? extends ValueComposite>... compositeTypes )
    {
        this.compositeTypes = compositeTypes;
    }

    public ValueDeclaration setMetaInfo( Object info )
    {
        metaInfo.set( info );
        return this;
    }

    public ValueDeclaration visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public ValueDeclaration withConcerns( Class<?>... concerns )
    {
        this.concerns.addAll( Arrays.asList( concerns ) );
        return this;
    }

    public ValueDeclaration withSideEffects( Class<?>... sideEffects )
    {
        this.sideEffects.addAll( Arrays.asList( sideEffects ) );
        return this;
    }

    public ValueDeclaration withMixins( Class<?>... mixins )
    {
        this.mixins.addAll( Arrays.asList( mixins ) );
        return this;
    }

    void addValues( List<ValueModel> values, PropertyDeclarations propertyDecs )
    {
        for( Class<? extends ValueComposite> compositeType : compositeTypes )
        {
            try
            {
                ValueModel valueModel = ValueModel.newModel( compositeType,
                                                             visibility,
                                                             new MetaInfo( metaInfo ).withAnnotations( compositeType ),
                                                             propertyDecs,
                                                             concerns,
                                                             sideEffects,
                                                             mixins );
                values.add( valueModel );
            }
            catch( Exception e )
            {
                throw new InvalidApplicationException( "Could not register " + compositeType.getName(), e );
            }
        }
    }
}