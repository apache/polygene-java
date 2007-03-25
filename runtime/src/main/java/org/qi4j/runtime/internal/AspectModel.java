/*
 * Copyright 2007 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.runtime.internal;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.lang.annotation.Annotation;
import org.qi4j.runtime.mixin.QiMixin;
import org.qi4j.runtime.pointcut.QiPointcut;
import org.qi4j.runtime.pointcut.QiInterceptor;
import org.qi4j.runtime.pointcut.constraints.QiImplements;
import org.qi4j.runtime.pointcut.constraints.QiMethodExpression;
import org.qi4j.runtime.pointcut.constraints.QiProperty;
import org.qi4j.runtime.pointcut.constraints.QiTargetClass;

public class AspectModel
{
    private final Class m_primaryAspect;
    private final HashSet<Class> m_mixins;

    public AspectModel( Class primaryAspect )
    {
        m_primaryAspect = primaryAspect;
        m_mixins = new HashSet<Class>();
        extract(primaryAspect);
    }

    public Class getPrimaryAspect()
    {
        return m_primaryAspect;
    }

    /**
     * @param candidate The class to be checked.
     *
     * @return true if the candidate class is a mixin of this model.
     */
    public boolean isMixin( Class candidate )
    {
        return m_mixins.contains( candidate );
    }

    private void extract( Class cls )
    {
        Class[] interfaces = cls.getInterfaces();
        for( Class intface : interfaces )
        {
            processMixin( intface );
            processPointcut( intface );
        }
        for( Class intface : interfaces )
        {
            extract( intface );
        }
    }

    private void processMixin( Class intface )
    {
        QiMixin annotation = (QiMixin) intface.getAnnotation( QiMixin.class );
        if( annotation == null )
        {
            return;
        }
        Class[] mixinClasses = annotation.value();
        for( Class mixinClass : mixinClasses )
        {
            m_mixins.add( mixinClass );
        }
    }

    private void processPointcut( Class intface )
    {
        QiPointcut annotation = (QiPointcut) intface.getAnnotation( QiPointcut.class );
        if( annotation == null )
        {
            return;
        }
        boolean condition = processImplements( intface );
        condition = condition && processMethodExpression( intface );
        condition = condition && processProperty( intface );
        condition = condition && processTargetClass( intface );
        if( condition )
        {
            processInterceptor( intface );
        }
    }

    private boolean processImplements( Class intface )
    {
        QiImplements implementsAnnotation = (QiImplements) intface.getAnnotation( QiImplements.class );
        Class[] impls = implementsAnnotation.value();
        for( Class implementsClass : impls )
        {
            if( ! implementsClass.isAssignableFrom( m_primaryAspect ) )
            {
                return false;
            }
        }
        return true;
    }

    private boolean processMethodExpression( Class intface )
    {
        QiMethodExpression methodExpressionAnnotation = (QiMethodExpression) intface.getAnnotation( QiMethodExpression.class );
        String expression = methodExpressionAnnotation.value();
        return true;
    }

    private boolean processProperty( Class intface )
    {
        QiProperty methodExpressionAnnotation = (QiProperty) intface.getAnnotation( QiProperty.class );
        String[] present = methodExpressionAnnotation.value();
        for( String property : present )
        {
            if( System.getProperty( property ) == null )
            {
                return false;
            }
        }
        String[] notPresent = methodExpressionAnnotation.notpresent();
        for( String property : notPresent )
        {
            if( System.getProperty( property ) != null )
            {
                return false;
            }
        }
        return true;
    }

    private boolean processTargetClass( Class intface )
    {
        QiTargetClass interceptorAnnotation = (QiTargetClass) intface.getAnnotation( QiTargetClass.class );
        Class[] interceptorClasses = interceptorAnnotation.value();
        for( Class interceptor : interceptorClasses )
        {

        }
        return true;
    }

    private void processInterceptor( Class intface )
    {
        QiInterceptor interceptorAnnotation = (QiInterceptor) intface.getAnnotation( QiInterceptor.class );
        Class[] interceptorClasses = interceptorAnnotation.value();
        for( Class interceptor : interceptorClasses )
        {

        }
    }
}

