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

import org.qi4j.composite.Composite;
import java.util.List;
import java.util.ArrayList;

/**
 * TODO
 */
public class CompositeDeclaration
{
    private Iterable<Class<? extends Composite>> compositeTypes;

    private boolean modulePublic;
    private boolean layerPublic;

    public CompositeDeclaration( List<Class<? extends Composite>> compositeTypes )
    {
        this.compositeTypes = compositeTypes;
    }

    public CompositeDeclaration( Class<? extends Composite>[] compositeTypes )
    {
        List<Class<? extends Composite>> list = new ArrayList<Class<? extends Composite>>();
        for( Class<? extends Composite> clazz : compositeTypes )
        {
            list.add( clazz );
        }
        this.compositeTypes = list;
    }

    public CompositeDeclaration isModulePublic()
    {
        modulePublic = true;
        return this;
    }

    public CompositeDeclaration isLayerPublic()
        throws IllegalStateException
    {
        if( modulePublic == false )
        {
            throw new IllegalStateException( "Composites must be declared as public in Module first" );
        }

        layerPublic = true;
        return this;
    }

    Iterable<Class<? extends Composite>> getCompositeTypes()
    {
        return compositeTypes;
    }

    boolean getModulePublic()
    {
        return modulePublic;
    }

    boolean getLayerPublic()
    {
        return layerPublic;
    }
}
