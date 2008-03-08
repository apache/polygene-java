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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import org.qi4j.runtime.composite.ObjectModelFactory;
import org.qi4j.spi.structure.ObjectDescriptor;
import org.qi4j.spi.structure.Visibility;

/**
 * TODO
 */
public final class ObjectDeclaration
{
    private Iterable<Class> objectTypes;
    private Map<Class, Object> objectInfos = new HashMap<Class, Object>();
    private Visibility visibility = Visibility.module;

    public ObjectDeclaration( Iterable<Class> classes )
    {
        this.objectTypes = classes;
    }

    public <T> ObjectDeclaration addObjectInfo( Class<T> infoType, T info )
    {
        objectInfos.put( infoType, info );
        return this;
    }

    public ObjectDeclaration visibleIn( Visibility visibility )
        throws IllegalStateException
    {
        this.visibility = visibility;
        return this;
    }

    List<ObjectDescriptor> getObjectDescriptors( ObjectModelFactory objectModelFactory )
    {
        List<ObjectDescriptor> objectDescriptors = new ArrayList<ObjectDescriptor>();
        for( Class objectType : objectTypes )
        {
            ObjectDescriptor objectDescriptor = new ObjectDescriptor( objectModelFactory.newObjectModel( objectType ), objectInfos, visibility );
            objectDescriptors.add( objectDescriptor );
        }
        return objectDescriptors;
    }
}
