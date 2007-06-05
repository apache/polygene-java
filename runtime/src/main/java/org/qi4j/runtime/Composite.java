/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.runtime;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Field;
import java.io.StringWriter;
import java.io.PrintWriter;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.Uses;

/**
 * TODO
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
public class Composite
{
    // Static --------------------------------------------------------

    // Attributes ----------------------------------------------------
    Class composite;
    Map<Class, Class> implementations = new HashMap<Class, Class>();
    Map<Method, List<Class>> modifiers = new HashMap<Method, List<Class>>();

    // Constructors --------------------------------------------------
    public Composite( Class aCompositeClass)
    {
        this.composite = aCompositeClass;

        // Find implementations
        Method[] methods = aCompositeClass.getMethods();
        for( Method method : methods )
        {
            // Find implementation
            Class<?> methodClass = method.getDeclaringClass();
            if( getImplementation( methodClass ) == null )
            {
                List<Class> implementationClasses = new ArrayList<Class>();
                findImplementations( methodClass, aCompositeClass, implementationClasses );
                if( implementationClasses.size() > 0 )
                {
                    implementations.put( methodClass, implementationClasses.get( 0 ) );
                }
                else
                {
                    throw new IllegalStateException( "No implementation for interface " + methodClass.getName() + " found" );
                }
            }
        }

        // Find modifiers
        for( Method method : methods )
        {
            // Find modifiers for method
            List<Class> modifierClasses = new ArrayList<Class>();

            // 1) Interface modifiers
            findModifiers( method, aCompositeClass, modifierClasses );

            // 2) Implementation modifiers
            Class implementationClass = getImplementation( method.getDeclaringClass() );
            findModifiers( method, implementationClass, modifierClasses );

            // 3) Modifiers from other mixins
            for( Class mixinClass : implementations.values() )
            {
                if( !mixinClass.equals( implementationClass ) )
                {
                    findModifiers( method, mixinClass, modifierClasses );
                }
            }

            modifiers.put( method, modifierClasses );
        }
    }

    // Public --------------------------------------------------------
    public Class getComposite()
    {
        return composite;
    }

    public Class getImplementation( Class anInterface )
    {
        return implementations.get( anInterface );
    }

    public List<Class> getModifiers( Method aMethod )
    {
        return modifiers.get( aMethod );
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( composite.getName() );
        for( Iterator iterator = implementations.entrySet().iterator(); iterator.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            Class interfaceClass = (Class) entry.getKey();
            Class implClass = (Class) entry.getValue();
            out.println( "  " + interfaceClass.getName() );
            out.println( "  implemented by " + implClass.getName() );
            Method[] methods = interfaceClass.getMethods();
            for( Method method : methods )
            {
                out.println( "    " + method.toGenericString() );
                List<Class> methodModifiers = getModifiers( method );
                for( Class methodModifier : methodModifiers )
                {
                    out.println( "      " + methodModifier.getName() );
                }
            }
        }
        out.close();
        return str.toString();
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private void findImplementations( Class aMethodClass, Class aType, List<Class> anImplementationClasses )
    {
        ImplementedBy impls = (ImplementedBy) aType.getAnnotation( ImplementedBy.class );
        if( impls != null )
        {
            Class[] implementationClasses = impls.value();
            for( Class implementationClass : implementationClasses )
            {
                if( aMethodClass.isAssignableFrom( implementationClass ) )
                {
                    anImplementationClasses.add( implementationClass );
                }
            }
        }

        // Check subinterfaces
        Class[] subTypes = aType.getInterfaces();
        for( Class subType : subTypes )
        {
            findImplementations( aMethodClass, subType, anImplementationClasses );
        }
    }

    private void findModifiers( Method method, Class modifierType, List<Class> aModifierList )
    {
        ModifiedBy modifiedBy = (ModifiedBy) modifierType.getAnnotation( ModifiedBy.class );
        if( modifiedBy != null )
        {
            Class[] modificationClasses = modifiedBy.value();
            modifications : for( Class modificationClass : modificationClasses )
            {
                if( !aModifierList.contains( modificationClass ) )
                {
                    if( InvocationHandler.class.isAssignableFrom( modificationClass ) )
                    {
                        // Check AppliesTo
                        AppliesTo appliesTo = (AppliesTo) modificationClass.getAnnotation( AppliesTo.class );
                        if( appliesTo != null )
                        {
                            // Check AppliesTo
                            if( appliesTo.value().isAnnotation() )
                            {
                                Class implClass = getImplementation( method.getDeclaringClass() );
                                try
                                {
                                    Method implMethod = implClass.getMethod( method.getName(), method.getParameterTypes() );
                                    if( implClass.getAnnotation( appliesTo.value() ) == null && implMethod.getAnnotation( appliesTo.value()) == null)
                                    {
                                        continue; // Skip this modifier
                                    }
                                }
                                catch( NoSuchMethodException e )
                                {
                                    // Ignore
                                }
                            }
                            else
                            {
                                if( !appliesTo.value().isAssignableFrom( getImplementation( method.getDeclaringClass()) ) )
                                {
                                    continue; // Skip this modifier
                                }
                            }
                        }
                    } else if( !method.getDeclaringClass().isAssignableFrom( modificationClass ) )
                    {
                        continue; // Skip this modifier
                    }



                    // Check @Uses
                    List<Field> usesFields = new ArrayList<Field>();
                    findUses( modificationClass, usesFields);
                    for( Field usesField : usesFields )
                    {
                        boolean isAssignable = usesField.getType().isAssignableFrom( composite );
                        if (!isAssignable )
                            continue modifications; // Skip this modifier
                    }

                    aModifierList.add( modificationClass );
                }
            }
        }

        // Check subinterfaces
        Class[] subTypes = modifierType.getInterfaces();
        for( Class subType : subTypes )
        {
            findModifiers( method, subType, aModifierList );
        }

        // Check superclass
        if( !modifierType.isInterface() && modifierType != Object.class )
        {
            findModifiers( method, modifierType.getSuperclass(), aModifierList );
        }
    }

    private void findUses( Class<? extends Object> aClass, List<Field> aUsesFields )
    {
        Field[] fields = aClass.getDeclaredFields();
        for( Field field : fields )
        {
            if( field.getAnnotation( Uses.class ) != null )
            {
                field.setAccessible( true );
                aUsesFields.add( field );
            }
        }
        Class<?> parent = aClass.getSuperclass();
        if( parent != Object.class )
        {
            findUses( parent, aUsesFields );
        }
    }
}
