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

package org.qi4j.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import org.qi4j.composite.Composite;
import org.qi4j.runtime.composite.CompositeModelFactory;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.spi.structure.Visibility;

/**
 * TODO
 */
public class CompositeDeclaration
{
    private Class<? extends Composite>[] compositeTypes;
    private Map<Class, Object> compositeInfos = new HashMap<Class, Object>();
    private Visibility visibility = Visibility.module;

    public CompositeDeclaration( Class<? extends Composite>[] compositeTypes )
    {
        this.compositeTypes = compositeTypes;
    }

    public CompositeDeclaration addCompositeInfo( Object info )
    {
        compositeInfos.put( info.getClass(), info );
        return this;
    }

    public CompositeDeclaration visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    List<CompositeDescriptor> getCompositeDescriptors( CompositeModelFactory compositeModelFactory )
    {
        List<CompositeDescriptor> compositeDescriptors = new ArrayList<CompositeDescriptor>();
        for( Class<? extends Composite> compositeType : compositeTypes )
        {
            CompositeModel compositeModel = compositeModelFactory.newCompositeModel( compositeType );
            CompositeDescriptor compositeDescriptor = new CompositeDescriptor( compositeModel, compositeInfos, visibility );
            compositeDescriptors.add( compositeDescriptor );
        }
        return compositeDescriptors;
    }
}
