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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.orthogon.advice.Advice;
import org.ops4j.orthogon.pointcut.QiInterceptor;
import org.ops4j.orthogon.pointcut.constraints.QiImplements;
import org.ops4j.orthogon.pointcut.constraints.QiMethodExpression;
import org.ops4j.orthogon.pointcut.constraints.QiProperty;
import org.ops4j.orthogon.pointcut.constraints.QiTargetClass;

/**
 * TODO: Finish up this class
 * TODO: Figure out how to evaluate this in the most efficient manner.
 * TODO: Should we flatten these pointcuts?
 *
 * @since 1.0.0
 */
final class Pointcut
{
    private Pointcut m_parent;
    private List<QiImplements> m_implementsConstraints;
    private List<QiTargetClass> m_targetClassConstraints;
    private List<QiProperty> m_propertyConstraints;
    private List<QiMethodExpression> m_methodExpressionConstraints;
    private List<QiInterceptor> m_interceptors;

    public Pointcut( Pointcut parent, List<QiImplements> anImplements, List<QiProperty> property,
                     List<QiMethodExpression> methodExpressions, List<QiTargetClass> targetClasses,
                     List<QiInterceptor> interceptors
    )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( anImplements, "anImplements" );
        NullArgumentException.validateNotNull( property, "property" );
        NullArgumentException.validateNotNull( methodExpressions, "methodExpressions" );
        NullArgumentException.validateNotNull( targetClasses, "targetClasses" );
        NullArgumentException.validateNotNull( interceptors, "interceptors" );

        m_parent = parent;
        m_implementsConstraints = anImplements;
        m_propertyConstraints = property;
        m_methodExpressionConstraints = methodExpressions;
        m_targetClassConstraints = targetClasses;
        m_interceptors = interceptors;
    }

    /**
     * Creates advices of this pointcut.
     *
     * @return a list of advices of this pointcut.
     *
     * @since 1.0.0
     */
    final List<Advice> createAdvices()
    {
        List<Advice> advices = new ArrayList<Advice>();
        // TODO: Need to figure out how to hand in sort order.
        if( m_parent != null )
        {
            List<Advice> parentAdvices = m_parent.createAdvices();
            advices.addAll( parentAdvices );
        }

        // TODO: This is a wrong implementation
        if( !m_interceptors.isEmpty() )
        {
            for( QiInterceptor interceptor : m_interceptors )
            {
                Class[] classes = interceptor.value();
                for( Class aClass : classes )
                {
                    if( aClass == null )
                    {
                        // TODO warning
                        continue;
                    }

                    Advice instance = newAdvice( aClass );
                    if( instance != null )
                    {
                        advices.add( instance );
                    }
                }
            }
        }

        return advices;
    }

    /**
     * Creates a new advice given the interceptor class. The generated advice class must have full name as
     * <b>[packageName]._[className]_</b>.
     *
     * @param aClass The abstract interceptor class. This argument must not be {@code null}.
     *
     * @return A new advice instance.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code aClass} is {@code null}.
     * @since 1.0.0
     */
    @SuppressWarnings( "unchecked" )
    private static Advice newAdvice( Class aClass )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( aClass, "aClass" );

        String classShortName = "_" + aClass.getSimpleName() + "_";
        Package classPackage = aClass.getPackage();
        String packageName = classPackage.getName();
        String classFullName = packageName + "." + classShortName;

        ClassLoader loader = aClass.getClassLoader();
        Advice instance = null;
        try
        {
            Class<Advice> adviceClass = (Class<Advice>) loader.loadClass( classFullName );
            instance = adviceClass.newInstance();
        }
        catch( ClassNotFoundException e )
        {
            // TODO means that the class is not generated yet.
            e.printStackTrace();
        }
        catch( IllegalAccessException e )
        {
            e.printStackTrace();  //TODO: Auto-generated, need attention.
        }
        catch( InstantiationException e )
        {
            e.printStackTrace();  //TODO: Auto-generated, need attention.
        }

        return instance;
    }

    final Set<Class> getJoinpointClasses()
    {
        Set<Class> joinpointClasses = new HashSet<Class>();
        if( m_parent != null )
        {
            joinpointClasses.addAll( m_parent.getJoinpointClasses() );
        }
        for( QiImplements qiImplements : m_implementsConstraints )
        {
            Class[] classes = qiImplements.value();

            addAllClassToSet( classes, joinpointClasses );
        }

        for( QiTargetClass qiTargetClass : m_targetClassConstraints )
        {
            Class[] classes = qiTargetClass.value();

            addAllClassToSet( classes, joinpointClasses );
        }

        return joinpointClasses;
    }

    private static void addAllClassToSet( Class[] arrays, Set<Class> set )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( set, "set" );
        NullArgumentException.validateNotNull( arrays, "arrays" );

        for( Class aClass : arrays )
        {
            set.add( aClass );
        }
    }

    final boolean isIntersect( JoinpointDescriptor descriptor )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( descriptor, "descriptor" );

        if( m_parent != null )
        {
            boolean matchParent = m_parent.isIntersect( descriptor );

            if( !matchParent )
            {
                return false;
            }
        }

        Class[] descriptorTargetClasses = descriptor.getTargetClasses();
        if( !isMatchTargetClassConstraint( descriptorTargetClasses ) )
        {
            return false;
        }

        if( !isMatchImplementConstraints( descriptorTargetClasses ) )
        {
            return false;
        }

        Method method = descriptor.getMethod();
        if( !isMatchAnyInterceptor( method ) )
        {
            return false;
        }

        // TODO complete this method

        return true;
    }

    private boolean isMatchAnyInterceptor( Method method )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( method, "method" );

        if( !m_interceptors.isEmpty() )
        {
            String name = method.getName();
            Class<?>[] parameterType = method.getParameterTypes();
            for( QiInterceptor qiInterceptor : m_interceptors )
            {
                Class[] interceptorClasses = qiInterceptor.value();

                for( Class interceptorClass : interceptorClasses )
                {
                    try
                    {
                        interceptorClass.getMethod( name, parameterType );

                        return true;
                    }
                    catch( NoSuchMethodException e )
                    {
                        // This means, that there is no match method given the name and parameter.
                    }
                }
            }
        }
        return false;
    }

    private boolean isMatchTargetClassConstraint( Class[] classes )
    {
        if( !m_targetClassConstraints.isEmpty() )
        {
            for( QiTargetClass qiTargetClass : m_targetClassConstraints )
            {
                Class[] excludedClasses = qiTargetClass.exclude();

                if( isAnyPartOf( excludedClasses, classes ) )
                {
                    return false;
                }

                Class[] targetClasses = qiTargetClass.value();
                if( !isAllPartOf( targetClasses, targetClasses ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isAnyPartOf( Class[] parts, Class[] list )
    {
        NullArgumentException.validateNotNull( list, "list" );

        if( parts == null )
        {
            return false;
        }

        for( Class part : parts )
        {
            for( Class aClass : list )
            {
                if( aClass.equals( part ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isMatchImplementConstraints( Class[] descriptorTargetClasses )
    {
        if( !m_implementsConstraints.isEmpty() )
        {
            for( QiImplements qiImplements : m_implementsConstraints )
            {
                Class[] classes = qiImplements.value();
                if( !isAllPartOf( classes, descriptorTargetClasses ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isAllPartOf( Class[] parts, Class[] list )
    {
        NullArgumentException.validateNotNull( list, "list" );

        if( parts == null )
        {
            return true;
        }

        for( Class part : parts )
        {
            boolean found = false;
            for( Class aClass : list )
            {
                if( aClass.equals( part ) )
                {
                    found = true;
                    break;
                }
            }

            if( !found )
            {
                return false;
            }
        }

        return true;
    }
}
