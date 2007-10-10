/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.api.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A mixin is an implementation of a particular interface,
 * and is used as a fragment in a composite.
 */
public final class MixinModel<T>
    extends FragmentModel<T>
{
    private Iterable<AssertionModel> assertionModels;
    private Iterable<SideEffectModel> sideEffectModels;
    private Iterable<PropertyModel> propertyModels;

    public MixinModel( Class<T> fragmentClass, Iterable<ConstructorDependency> constructorDependencies, Iterable<FieldDependency> fieldDependencies, Iterable<MethodDependency> methodDependencies, List<PropertyModel> properties, Class appliesTo, Iterable<AssertionModel> assertionModels, Iterable<SideEffectModel> sideEffectModels )
    {
        super( fragmentClass, constructorDependencies, fieldDependencies, methodDependencies, appliesTo );
        this.sideEffectModels = sideEffectModels;
        this.propertyModels = properties;
        this.assertionModels = assertionModels;
    }

    public Iterable<AssertionModel> getAssertions()
    {
        return assertionModels;
    }

    public Iterable<SideEffectModel> getSideEffects()
    {
        return sideEffectModels;
    }

    public Iterable<PropertyModel> getProperties()
    {
        return propertyModels;
    }

    public Annotation getAnnotation( Class<? extends Annotation> annotationType, Method method )
    {
        Annotation annotation = null;
        // Check method
        annotation = method.getAnnotation( annotationType );
        if( annotation != null )
        {
            return annotation;
        }

        // Check method interface
        annotation = method.getDeclaringClass().getAnnotation( annotationType );
        if( annotation != null )
        {
            return annotation;
        }

        // Check mixin class
        annotation = getModelClass().getAnnotation( annotationType );
        if( annotation != null )
        {
            return annotation;
        }

        // Check mixin method
        try
        {
            Method mixinMethod = getModelClass().getMethod( method.getName(), method.getParameterTypes() );
            annotation = mixinMethod.getAnnotation( annotationType );
            if( annotation != null )
            {
                return annotation;
            }
        }
        catch( NoSuchMethodException e )
        {
            if( !isGeneric() )
            {
                throw new InvalidFragmentException( "Mixin " + getModelClass().getName() + " does not contain the method " + method.toGenericString(), getModelClass() );
            }
        }

        // No annotation of given type found
        return null;
    }
}
