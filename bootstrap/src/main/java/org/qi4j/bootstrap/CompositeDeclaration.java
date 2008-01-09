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
import java.util.List;
import org.qi4j.composite.Composite;
import org.qi4j.spi.structure.Visibility;

/**
 * TODO
 */
public class CompositeDeclaration
{
    private Iterable<Class<? extends Composite>> compositeTypes;
    private Visibility visibility = Visibility.none;

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

    public CompositeDeclaration publicIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    Iterable<Class<? extends Composite>> getCompositeTypes()
    {
        return compositeTypes;
    }

    public Visibility getVisibility()
    {
        return visibility;
    }
}
