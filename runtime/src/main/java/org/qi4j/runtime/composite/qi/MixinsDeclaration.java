/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.composite.qi;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.qi4j.composite.Mixins;
import org.qi4j.util.ClassUtil;

/**
 * TODO
 */
public final class MixinsDeclaration
{
    private List<MixinDeclaration> mixins = new ArrayList<MixinDeclaration>();

    public MixinsDeclaration( Class type )
    {
        Set<Type> interfaces = ClassUtil.interfacesOf( type );
        for( Type anInterface : interfaces )
        {
            addMixinDeclarations( anInterface );
        }
    }

    public MixinDeclaration findImplementationOf( Method method, Class compositeType )
    {
        for( MixinDeclaration mixin : mixins )
        {
            if( mixin.appliesTo( method, compositeType ) )
            {
                return mixin;
            }
        }

        return null;
    }

    private void addMixinDeclarations( Type type )
    {
        if( type instanceof Class )
        {
            Mixins annotation = Mixins.class.cast( ( (Class) type ).getAnnotation( Mixins.class ) );

            Class[] mixinClasses = annotation.value();
            for( Class mixinClass : mixinClasses )
            {
                mixins.add( new MixinDeclaration( mixinClass, type ) );
            }
        }
    }
}
