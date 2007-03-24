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
package org.qi4j.runtime.internal;

import java.lang.annotation.Annotation;
import org.ops4j.lang.NullArgumentException;
import org.qi4j.runtime.pointcut.QiInterceptor;
import org.qi4j.runtime.pointcut.constraints.QiImplements;
import org.qi4j.runtime.pointcut.constraints.QiMethodExpression;
import org.qi4j.runtime.pointcut.constraints.QiProperty;
import org.qi4j.runtime.pointcut.constraints.QiTargetClass;

public final class AspectParser
{
    private AspectRegistry m_registry;

    public AspectParser( AspectRegistry registry )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( registry, "registry" );

        m_registry = registry;
    }

    public final void parse( Class... sources )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( sources, "sources" );

        for( int i = 0; i < sources.length; i++ )
        {
            Class source = sources[ i ];
            if( source == null )
            {
                throw new IllegalArgumentException( "Sources element [" + i + "] is null." );
            }

            parse( null, source );
        }
    }

    /**
     * TODO: I think it is best to flatten the pointcut, unless if we want to support updates of the parent will update
     * the child. Sounds dangerous to me.
     */
    private void parse( Pointcut parent, Class aspect )
    {
        // Signal the change in hierarchy.
        PointcutBuilder builder = new PointcutBuilder( parent );
        Annotation[] annotations = aspect.getAnnotations();
        for( Annotation annotation : annotations )
        {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if( QiImplements.class.equals( annotationType ) )
            {
                QiImplements implementsConstraint = (QiImplements) annotation;
                builder.addImplementsConstraint( implementsConstraint );
            }
            else if( QiProperty.class.equals( annotationType ) )
            {
                QiProperty propertyConstraint = (QiProperty) annotation;
                builder.addPropertyConstraint( propertyConstraint );
            }
            else if( QiMethodExpression.class.equals( annotationType ) )
            {
                QiMethodExpression methodExpressionConstraint = (QiMethodExpression) annotation;
                builder.addMethodExpressionConstraint( methodExpressionConstraint );
            }
            else if( QiTargetClass.class.equals( annotationType ) )
            {
                QiTargetClass targetClassConstraint = (QiTargetClass) annotation;
                builder.addTargetClassConstraint( targetClassConstraint );
            }
            else if( QiInterceptor.class.equals( annotationType ) )
            {
                QiInterceptor interceptor = (QiInterceptor) annotation;
                builder.addInterceptor( interceptor );
            }
            else
            {
                //  TODO: problem
            }
        }
        Pointcut pointcut = builder.create();
        m_registry.registerPointcut( pointcut );
        Class[] superclasses = aspect.getInterfaces();
        for( Class superclass : superclasses )
        {
            parse( pointcut, superclass );
        }
    }
}
