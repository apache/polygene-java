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
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.bootstrap.ValueAssembly;
import org.qi4j.runtime.value.ValueModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Declaration of a ValueComposite.
 */
public final class ValueAssemblyImpl
    implements ValueAssembly
{
    private Class<? extends ValueComposite> compositeType;
    List<Class<?>> concerns = new ArrayList<Class<?>>();
    List<Class<?>> sideEffects = new ArrayList<Class<?>>();
    List<Class<?>> mixins = new ArrayList<Class<?>>();
    List<Class<?>> roles = new ArrayList<Class<?>>();
    MetaInfo metaInfo = new MetaInfo();
    Visibility visibility = Visibility.module;

    public ValueAssemblyImpl( Class<? extends ValueComposite> compositeType )
    {
        this.compositeType = compositeType;
    }

    @Override
    public Class<? extends ValueComposite> type()
    {
        return compositeType;
    }

    void addValueModel( List<ValueModel> values, PropertyDeclarations propertyDecs, AssemblyHelper helper)
    {
        try
        {
            ValueModel valueModel = ValueModel.newModel( compositeType,
                                                         visibility,
                                                         new MetaInfo( metaInfo ).withAnnotations( compositeType ),
                                                         propertyDecs,
                                                         concerns,
                                                         sideEffects,
                                                         mixins,
                                                         roles,
                                                         helper);
            values.add( valueModel );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + compositeType.getName(), e );
        }
    }
}