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
import java.lang.reflect.Modifier;
import java.util.List;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.bootstrap.ObjectDeclaration;
import org.qi4j.runtime.object.ObjectModel;

/**
 * Declaration of an Object. Created by {@link org.qi4j.runtime.bootstrap.ModuleAssemblyImpl#addObjects(Class[])}.
 */
public final class ObjectDeclarationImpl
    implements ObjectDeclaration, Serializable
{
    private Iterable<Class> objectTypes;
    private MetaInfo metaInfo = new MetaInfo();
    private Visibility visibility = Visibility.module;

    public ObjectDeclarationImpl( Iterable<Class> classes )
    {
        for( Class clazz : classes )
        {
            // best try to find out if the class is a concrete class
            if( clazz.isEnum() ||
                ( !Composite.class.isAssignableFrom( clazz ) && Modifier.isAbstract( clazz.getModifiers() ) ) )
            {
                throw new IllegalArgumentException( "Declared objects must be concrete classes: " + clazz );
            }
        }
        this.objectTypes = classes;
    }

    public ObjectDeclaration setMetaInfo( Object info )
    {
        metaInfo.set( info );
        return this;
    }

    public ObjectDeclaration visibleIn( Visibility visibility )
        throws IllegalStateException
    {
        this.visibility = visibility;
        return this;
    }

    public void addObjects( List<ObjectModel> objectModels )
    {
        for( Class objectType : objectTypes )
        {
            try
            {
                ObjectModel objectModel = new ObjectModel( objectType, visibility, metaInfo );
                objectModels.add( objectModel );
            }
            catch( Throwable e )
            {
                throw new InvalidApplicationException( "Could not register " + objectType.getName(), e );
            }
        }
    }
}
