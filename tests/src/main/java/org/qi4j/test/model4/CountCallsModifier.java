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
package org.qi4j.test.model4;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.annotation.DependencyOld;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.ThisAs;

public class CountCallsModifier
    implements InvocationHandler
{
    private @Modifies InvocationHandler next;
    private @ThisAs Counter meAsCounter;

    private CountCalls annotation;

    public CountCallsModifier( @DependencyOld AnnotatedElement method )
    {
        annotation = method.getAnnotation( CountCalls.class );
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        if( annotation != null )
        {
            meAsCounter.increment();
        }
        return next.invoke( proxy, method, args );
    }
}
