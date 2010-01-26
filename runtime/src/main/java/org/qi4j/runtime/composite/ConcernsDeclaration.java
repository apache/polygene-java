/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.composite;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.concern.Concerns;
import org.qi4j.spi.util.MethodKeyMap;

import static org.qi4j.api.util.Classes.*;

/**
 * JAVADOC
 */
public final class ConcernsDeclaration
    implements Serializable
{
    public static void concernDeclarations( Class type, List<ConcernDeclaration> concerns )
    {
        // Find concern declarations
        Set<Type> types = ( type.isInterface() ? genericInterfacesOf( type ) : Collections.singleton( (Type) type ) );

        for( Type aType : types )
        {
            addConcernDeclarations( aType, concerns );
        }
    }

    public static void concernDeclarations( Iterable<Class<?>> concernclasses, List<ConcernDeclaration> concerns )
    {
        // Add concerns from assembly
        for( Class<?> concern : concernclasses )
        {
            concerns.add( new ConcernDeclaration( concern, null ) );
        }
    }

    private static void addConcernDeclarations( Type type, List<ConcernDeclaration> concerns )
    {
        if( type instanceof Class )
        {
            final Class clazz = (Class) type;
            Concerns annotation = Concerns.class.cast( clazz.getAnnotation( Concerns.class ) );
            if( annotation != null )
            {
                Class[] concernClasses = annotation.value();
                for( Class concernClass : concernClasses )
                {
                    concerns.add( new ConcernDeclaration( concernClass, clazz ) );
                }
            }
        }
    }

    private final List<ConcernDeclaration> concerns;
    private final Map<Method, MethodConcernsModel> methodConcernsModels = new MethodKeyMap<MethodConcernsModel>();

    public ConcernsDeclaration( List<ConcernDeclaration> concerns )
    {
        this.concerns = concerns;
    }

    // Model

    public MethodConcernsModel concernsFor( Method method, Class<? extends Composite> type )
    {
        if( !methodConcernsModels.containsKey( method ) )
        {
            List<MethodConcernModel> concernsForMethod = new ArrayList<MethodConcernModel>();
            for( ConcernDeclaration concern : concerns )
            {
                if( concern.appliesTo( method, type ) )
                {
                    Class concernClass = concern.type();
                    concernsForMethod.add( new MethodConcernModel( concernClass ) );
                }
            }

            MethodConcernsModel methodConcerns = new MethodConcernsModel( method, concernsForMethod );
            methodConcernsModels.put( method, methodConcerns );
            return methodConcerns;
        }
        else
        {
            return methodConcernsModels.get( method );
        }
    }
}
