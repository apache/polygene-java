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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Collections.singleton;
import org.qi4j.composite.Composite;
import org.qi4j.composite.SideEffects;
import org.qi4j.util.AnnotationUtil;
import static org.qi4j.util.ClassUtil.*;

/**
 * TODO
 */
public final class SideEffectsDeclaration
{
    private final List<SideEffectDeclaration> sideEffectDeclarations = new ArrayList<SideEffectDeclaration>();
    private final Map<Method, MethodSideEffectsModel> methodSideEffects = new HashMap<Method, MethodSideEffectsModel>();

    public SideEffectsDeclaration( Class type )
    {
        Collection<Type> types = asSideEffectsTargetTypes( type );


        for( Type aType : types )
        {
            addSideEffectDeclaration( aType );
        }
    }

    private Collection<Type> asSideEffectsTargetTypes( Class type )
    {
        // Find side-effect declarations
        if( type.isInterface() )
        {
            return genericInterfacesOf( type );
        }
        else
        {
            return singleton( (Type) type );
        }
    }

    // Model
    public MethodSideEffectsModel sideEffectsFor( Method method, Class<? extends Composite> compositeType )
    {
        if( methodSideEffects.containsKey( method ) )
        {
            return methodSideEffects.get( method );
        }


        final Collection<Class> matchingSideEffects = matchingSideEffectClasses( method, compositeType );
        MethodSideEffectsModel methodConcerns = MethodSideEffectsModel.createForMethod( method, matchingSideEffects );
        methodSideEffects.put( method, methodConcerns );
        return methodConcerns;
    }

    private Collection<Class> matchingSideEffectClasses( Method method, Class<? extends Composite> compositeType )
    {
        Collection<Class> result=new ArrayList<Class>(sideEffectDeclarations.size());

        for( SideEffectDeclaration sideEffectDeclaration : sideEffectDeclarations )
        {
            if (sideEffectDeclaration.appliesTo( method, compositeType))
                result.add(sideEffectDeclaration.type());
        }
        return result;
    }

    private void addSideEffectDeclaration( Type type )
    {
        SideEffects annotation = AnnotationUtil.getAnnotation( type, SideEffects.class );
        if( annotation != null )
        {
            Class[] sideEffectClasses = annotation.value();
            for( Class sideEffectClass : sideEffectClasses )
            {
                sideEffectDeclarations.add( new SideEffectDeclaration( sideEffectClass, type ) );
            }
        }
    }

}