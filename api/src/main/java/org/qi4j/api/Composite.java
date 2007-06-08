/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.annotation.Uses;

/**
 * Composites are descriptors of what an interface represent. <TODO better docs needed here>
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
public final class Composite
{
    // Attributes ----------------------------------------------------
    Class composite;
    Map<Class, List<Mixin>> mixins = new HashMap<Class, List<Mixin>>();
    Map<Method, List<Modifier>> modifiers = new HashMap<Method, List<Modifier>>();

    // Constructors --------------------------------------------------
    public Composite( Class aCompositeClass )
    {
        this.composite = aCompositeClass;

        // Find mixins
        Method[] methods = aCompositeClass.getMethods();
        for( Method method : methods )
        {
            // Find implementation
            Class<?> methodClass = method.getDeclaringClass();
            if( getMixin( methodClass ) == null )
            {
                List<Mixin> methodMixins = new ArrayList<Mixin>();
                findMixins( methodClass, aCompositeClass, methodMixins );
                if( methodMixins.size() > 0 )
                {
                    mixins.put( methodClass, methodMixins );
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
            List<Modifier> modifierClasses = new ArrayList<Modifier>();

            // 1) Interface modifiers
            findModifiers( method, aCompositeClass, modifierClasses );

            // 2) Implementation modifiers
            Mixin methodMixin = getMixin( method.getDeclaringClass() );
            findModifiers( method, methodMixin.getFragmentClass(), modifierClasses );

            // 3) Modifiers from other mixins
            for( List<Mixin> mixins : this.mixins.values() )
            {
                for( Mixin mixin : mixins )
                {
                    if( !mixin.equals( methodMixin ) )
                    {
                        findModifiers( method, mixin.getFragmentClass(), modifierClasses );
                    }
                }
            }

            modifiers.put( method, modifierClasses );
        }
    }

    // Public --------------------------------------------------------
    public Class getCompositeClass()
    {
        return composite;
    }

    public Mixin getMixin( Class anInterface )
    {
        List<Mixin> classes = mixins.get( anInterface );
        if( classes != null && classes.size() > 0 )
        {
            return classes.get( 0 );
        }
        else
        {
            return null;
        }
    }

    public List<Mixin> getMixins( Class anInterface )
    {
        return mixins.get( anInterface );
    }

    public List<Modifier> getModifiers( Method aMethod )
    {
        return modifiers.get( aMethod );
    }

    public Set<Fragment> getFragments()
    {
        Set<Fragment> fragments = new HashSet<Fragment>();
        for( List<Mixin> mixins : this.mixins.values() )
        {
            fragments.addAll( mixins );
        }
        for( List<Modifier> methodModifiers : modifiers.values() )
        {
            fragments.addAll( methodModifiers );
        }
        return fragments;
    }

    // Object overrides ---------------------------------------------
    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( composite.getName() );
        for( Iterator<Map.Entry<Class, List<Mixin>>> iterator = mixins.entrySet().iterator(); iterator.hasNext(); )
        {
            Map.Entry<Class, List<Mixin>> entry = iterator.next();
            Class interfaceClass = entry.getKey();
            List<Mixin> mixinClasses = entry.getValue();
            out.println( "  " + interfaceClass.getName() );
            out.println( "  implemented by " + mixinClasses.get( 0 ).getFragmentClass().getName() );
            Method[] methods = interfaceClass.getMethods();
            for( Method method : methods )
            {
                out.println( "    " + method.toGenericString() );
                List<Modifier> methodModifiers = getModifiers( method );
                for( Modifier methodModifier : methodModifiers )
                {
                    out.println( "      " + methodModifier.getFragmentClass().getName() );
                }
            }
        }
        out.close();
        return str.toString();
    }

    // Private -------------------------------------------------------
    private void findMixins( Class aMethodClass, Class aType, List<Mixin> anImplementationClasses )
    {
        ImplementedBy impls = (ImplementedBy) aType.getAnnotation( ImplementedBy.class );
        if( impls != null )
        {
            Class[] implementationClasses = impls.value();
            classes:
            for( Class implementationClass : implementationClasses )
            {
                if( aMethodClass.isAssignableFrom( implementationClass ) )
                {
                    if( anImplementationClasses.size() > 0 )
                    {
                        // Find generic implementation - this one overrides it
                        for( int i = 0; i < anImplementationClasses.size(); i++ )
                        {
                            if( InvocationHandler.class.isAssignableFrom( anImplementationClasses.get( i ).getFragmentClass() ) )
                            {
                                anImplementationClasses.add( i, new Mixin( implementationClass ) );
                                continue classes;
                            }
                        }
                    }
                    anImplementationClasses.add( new Mixin( implementationClass ) );
                }
                else
                {
                    AppliesTo appliesTo = (AppliesTo) implementationClass.getAnnotation( AppliesTo.class );
                    if( appliesTo != null )
                    {
                        if( appliesTo.value().isAssignableFrom( aMethodClass ) )
                        {
                            // Generic mixin that can apply to this interface
                            anImplementationClasses.add( new Mixin( implementationClass ) );
                        }
                    }
                }
            }
        }

        // Check subinterfaces
        Class[] subTypes = aType.getInterfaces();
        for( Class subType : subTypes )
        {
            findMixins( aMethodClass, subType, anImplementationClasses );
        }
    }

    private void findModifiers( Method method, Class modifierType, List<Modifier> aModifierList )
    {
        ModifiedBy modifiedBy = (ModifiedBy) modifierType.getAnnotation( ModifiedBy.class );
        if( modifiedBy != null )
        {
            Class[] modificationClasses = modifiedBy.value();
            modifications:
            for( Class modificationClass : modificationClasses )
            {
                Modifier modifier = new Modifier( modificationClass );

                if( !aModifierList.contains( modifier ) )
                {
                    if( InvocationHandler.class.isAssignableFrom( modificationClass ) )
                    {
                        // Check AppliesTo
                        Class appliesTo = modifier.getAppliesTo();
                        if( appliesTo != null )
                        {
                            // Check AppliesTo
                            if( appliesTo.isAnnotation() )
                            {
                                Mixin implClass = getMixin( method.getDeclaringClass() );
                                try
                                {
                                    Method implMethod = implClass.getFragmentClass().getMethod( method.getName(), method.getParameterTypes() );
                                    if( implClass.getFragmentClass().getAnnotation( appliesTo ) == null && implMethod.getAnnotation( appliesTo ) == null )
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
                                if( !appliesTo.isAssignableFrom( getMixin( method.getDeclaringClass() ).getFragmentClass() ) && !appliesTo.isAssignableFrom( method.getDeclaringClass() ) )
                                {
                                    continue; // Skip this modifier
                                }
                            }
                        }
                    }
                    else if( !method.getDeclaringClass().isAssignableFrom( modificationClass ) )
                    {
                        continue; // Skip this modifier
                    }

                    // Check @Uses
                    List<Field> usesFields = new ArrayList<Field>();
                    findUses( modificationClass, usesFields );
                    for( Field usesField : usesFields )
                    {
                        boolean isAssignable = usesField.getType().isAssignableFrom( composite );
                        if( !isAssignable )
                        {
                            continue modifications; // Skip this modifier
                        }
                    }

                    aModifierList.add( modifier );
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
