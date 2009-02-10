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

import java.util.List;
import java.io.Serializable;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.ValueComposite;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.bootstrap.ValueDeclaration;
import org.qi4j.runtime.composite.ValueModel;
import org.qi4j.runtime.composite.CompositeModel;

/**
 * Declaration of a ValueComposite. Created by {@link org.qi4j.bootstrap.ModuleAssembly#addValues(Class[])}.
 */
public final class ValueDeclarationImpl
    implements ValueDeclaration, Serializable
{
    private Class<? extends ValueComposite>[] compositeTypes;
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

    void addValues( List<CompositeModel> values, PropertyDeclarations propertyDecs )
    {
        for( Class<? extends ValueComposite> compositeType : compositeTypes )
        {
            ValueModel compositeModel = ValueModel.newModel( compositeType,
                                                               visibility,
                                                               new MetaInfo( metaInfo ).withAnnotations( compositeType ),
                                                               propertyDecs );
            values.add( compositeModel );
        }
    }
}