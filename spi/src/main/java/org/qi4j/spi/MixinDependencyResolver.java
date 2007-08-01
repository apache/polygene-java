/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.CompositeModelFactory;
import org.qi4j.api.model.CompositeContext;

public class MixinDependencyResolver
    implements DependencyResolver
{
    public Object resolveDependency( AnnotatedElement annotatedElement, CompositeContext context )
    {
        Class<?> type = getElementType( annotatedElement);
        if( type.equals( CompositeBuilderFactory.class ) )
        {
            return context.getCompositeBuilderFactory();
        }
        else if( type.equals( CompositeModelFactory.class ) )
        {
            return context.getCompositeModelFactory();
        }
        else
        {
            return null;
        }
    }

    private Class getElementType( AnnotatedElement annotatedElement )
    {
        Class clazz;
        if( annotatedElement instanceof Class )
        {
            clazz = (Class) annotatedElement;
        }
        else
        {
            clazz = ( (Field) annotatedElement ).getType();
        }
        return clazz;
    }

}
