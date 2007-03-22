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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.orthogon.advice.Advice;
import static org.ops4j.orthogon.internal.util.CollectionUtil.addAllToCollection;
import static org.ops4j.orthogon.internal.util.CollectionUtil.isAllPartOf;
import static org.ops4j.orthogon.internal.util.CollectionUtil.isAnyPartOf;
import org.ops4j.orthogon.pointcut.QiInterceptor;
import org.ops4j.orthogon.pointcut.constraints.QiImplements;
import org.ops4j.orthogon.pointcut.constraints.QiMethodExpression;
import org.ops4j.orthogon.pointcut.constraints.QiProperty;
import org.ops4j.orthogon.pointcut.constraints.QiTargetClass;

/**
 * TODO: Finish up this class
 * TODO: Figure out how to evaluate this in the most efficient manner.
 * TODO: Should we flatten these pointcuts? i.e. no more m_parent?
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
     * <p>Note: The ONLY check that is being done is whether the interceptor listed matches the requested method
     * invocation.</p>
     *
     * @param descriptor The descriptor. This argument must not be {@code null}.
     *
     * @return a list of advices of this pointcut.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code descriptor} is {@code null}.
     * @since 1.0.0
     */
    final List<Advice> createAdvices( JoinpointDescriptor descriptor )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( descriptor, "descriptor" );
        List<Advice> advices = new ArrayList<Advice>();

        // TODO: Need to figure out how to hand in sort order.
        if( m_parent != null )
        {
            List<Advice> parentAdvices = m_parent.createAdvices( descriptor );
            advices.addAll( parentAdvices );
        }

        if( !m_interceptors.isEmpty() )
        {
            Method method = descriptor.getMethod();
            Class declaringClass = method.getDeclaringClass();
            String methodName = method.getName();
            Class[] parameterTypes = method.getParameterTypes();
            for( QiInterceptor interceptor : m_interceptors )
            {
                Class[] classes = interceptor.value();
                for( Class aClass : classes )
                {
                    if( !isInterceptorInvocationRequired( aClass, declaringClass, methodName, parameterTypes ) )
                    {
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

    private static boolean isInterceptorInvocationRequired(
        Class interceptorClass, Class declaringClass, String methodName, Class[] parameterTypes
    )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( declaringClass, "declaringClass" );
        NullArgumentException.validateNotNull( methodName, "methodName" );
        NullArgumentException.validateNotNull( parameterTypes, "parameterTypes" );

        if( interceptorClass == null )
        {
            return false;
        }

        if( !InvocationHandler.class.isAssignableFrom( interceptorClass ) )
        {
            if( !declaringClass.isAssignableFrom( interceptorClass ) )
            {
                return false;
            }

            try
            {
                interceptorClass.getMethod( methodName, parameterTypes );
            }
            catch( NoSuchMethodException e )
            {
                // Ignore this as this means that the interceptor is not interested with the requested
                // method invocation
                return false;
            }
        }
        return true;
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
            // TODO: means that the class is not generated.
            e.printStackTrace();
        }
        catch( IllegalAccessException e )
        {
            // TODO: means that the class is generated but invalid.
            e.printStackTrace();
        }
        catch( InstantiationException e )
        {
            // TODO: means that the class is generated but invalid.
            e.printStackTrace();
        }

        return instance;
    }

    /**
     * Returns all the joinpoint classes that probably matches with this pointcut. This method does not return a
     * {@code null} object.
     *
     * @return all the joinpoint classes that probably matches with this pointcut.
     *
     * @since 1.0.0
     */
    final Set<Class> getJoinpointClasses()
    {
        Set<Class> joinpointClasses = new HashSet<Class>();
        if( m_parent != null )
        {
            Set<Class> parentJoinpointClasses = m_parent.getJoinpointClasses();
            joinpointClasses.addAll( parentJoinpointClasses );
        }

        for( QiImplements qiImplements : m_implementsConstraints )
        {
            Class[] classes = qiImplements.value();

            addAllToCollection( classes, joinpointClasses );
        }

        for( QiTargetClass qiTargetClass : m_targetClassConstraints )
        {
            Class[] classes = qiTargetClass.value();

            addAllToCollection( classes, joinpointClasses );
        }

        // The user probably forgot to set @QiImplements or @QiTargetClasses, let's search for dynamic interceptor
        if( joinpointClasses.isEmpty() )
        {
            for( QiInterceptor qiInterceptor : m_interceptors )
            {
                Class[] classes = qiInterceptor.value();
                for( Class aClass : classes )
                {
                    if( InvocationHandler.class.isAssignableFrom( aClass ) )
                    {
                        joinpointClasses.add( Object.class );
                        return joinpointClasses;
                    }
                }
            }
        }

        return joinpointClasses;
    }

    /**
     * Returns {@code true} if the specified {@code descriptor} argument intersects with this {@code pointcut},
     * {@code false} otherwise.
     *
     * @param descriptor The descriptor. This argument must not be {@code null}.
     *
     * @return A {@code boolean} indicator whether this {@code pointcut} instance intersect with the specified
     *         {@code descriptor} argument.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code descriptor} is {@code null}.
     * @since 1.0.0
     */
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

    private boolean isMatchImplementConstraints( Class[] descriptorTargetClasses )
    {
        if( !m_implementsConstraints.isEmpty() )
        {
            for( QiImplements qiImplements : m_implementsConstraints )
            {
                Class[] classes = qiImplements.value();
                if( !isAnyPartOf( classes, descriptorTargetClasses ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isMatchAnyInterceptor( Method method )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( method, "method" );

        if( !m_interceptors.isEmpty() )
        {
            Class declaringClass = method.getDeclaringClass();
            String methodName = method.getName();
            Class[] parameterType = method.getParameterTypes();
            for( QiInterceptor qiInterceptor : m_interceptors )
            {
                Class[] interceptorClasses = qiInterceptor.value();

                for( Class interceptorClass : interceptorClasses )
                {
                    if( isInterceptorInvocationRequired( interceptorClass, declaringClass, methodName, parameterType ) )
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
