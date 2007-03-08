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
package org.ops4j.orthogon.internal;

import java.lang.annotation.Annotation;
import org.ops4j.orthogon.pointcut.constraints.QiTargetClass;
import org.ops4j.orthogon.pointcut.constraints.QiImplements;
import org.ops4j.orthogon.pointcut.constraints.QiProperty;
import org.ops4j.orthogon.pointcut.constraints.QiMethodExpression;

public class AspectParser
{
    private AspectRegistry m_registry;

    void parse( Class source )
    {
        parse( null, source );
    }

    private void parse( Pointcut parent, Class aspect )
    {
        // Signal the change in hierarchy.
        PointcutBuilder builder = new PointcutBuilder(parent);
        Annotation[] annotations = aspect.getAnnotations();
        for( Annotation annotation: annotations)
        {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if( annotationType.equals( QiImplements.class ) )
            {
                QiImplements implementsConstraint = (QiImplements) annotation;
                builder.addImplementsConstraint( implementsConstraint );
            }
            else if( annotationType.equals( QiProperty.class ) )
            {
                QiProperty propertyConstraint = (QiProperty) annotation;
                builder.addPropertyConstraint( propertyConstraint );
            }
            else if( annotationType.equals( QiMethodExpression.class ) )
            {
                QiMethodExpression methodExpressionConstraint = (QiMethodExpression) annotation;
                builder.addMethodExpressionConstraint( methodExpressionConstraint );
            }
            else if( annotationType.equals( QiTargetClass.class ) )
            {
                QiTargetClass targetClassConstraint = (QiTargetClass) annotation;
                builder.addTargetClassConstraint( targetClassConstraint );
            }
            else
            {
                // TODO: Problem
            }
        }
        Pointcut pointcut = builder.create();
        m_registry.registerPointcut( pointcut );
        Class[] superclasses = aspect.getInterfaces();
        for( Class superclass:superclasses)
        {
            parse( pointcut, superclass );
        }
    }
}
