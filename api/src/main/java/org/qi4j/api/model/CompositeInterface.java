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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The CI is the runtime resolution of how a compile-time Composite
 * will be instantiated. It decides what mixins to be used for each
 * interface and what modifiers to use for each method.
 * <p/>
 * It also
 * considers whether a method or interface is backed by the composite
 * itself or a wrapped object.
 *
 * @author rickard
 * @version $Revision: 1.7 $
 * @see Composite
 */
public final class CompositeInterface
{
    // Attributes ----------------------------------------------------
    Composite composite;
    Class compositeInterface;
    CompositeInterface wrappedComposite;
    Map<Class, Mixin> mixins = new HashMap<Class, Mixin>();
    Map<Method, List<Modifier>> modifiers = new HashMap<Method, List<Modifier>>();

    // Constructors --------------------------------------------------
    public CompositeInterface( Composite aComposite, Class anInterface )
    {
        this( aComposite, anInterface, null );
    }

    public CompositeInterface( Composite aComposite, Class anInterface, CompositeInterface aWrappedComposite )
    {
        this.composite = aComposite;
        this.compositeInterface = anInterface;
        this.wrappedComposite = aWrappedComposite;

        Method[] methods = anInterface.getMethods();
        for( Method method : methods )
        {
            // Find mixin
            {
                Class<?> methodClass = method.getDeclaringClass();
                Mixin mixin = getMixin( methodClass );
                if( mixin == null && ( aWrappedComposite == null || !aWrappedComposite.isAssignableFrom( methodClass ) ) )
                {
                    throw new IllegalStateException( "No implementation for interface " + methodClass.getName() + " found in composite " + composite.getCompositeClass().getName() );
                }
            }
        }

        for( Method method : methods )
        {
            // Find modifiers for method
            {
                List<Modifier> modifierClasses = new ArrayList<Modifier>();

                // 1) Interface modifiers
                addModifiers( method, composite.getModifiers(), modifierClasses );

                // 2) Mixin modifiers
                Class<?> methodClass = method.getDeclaringClass();
                Mixin mixin = getMixin( methodClass );
                if( mixin != null )
                {
                    addModifiers( method, mixin.getModifiers(), modifierClasses );
                }

                // 3) Modifiers from other mixins
                for( Map.Entry<Class, Mixin> mapping : mixins.entrySet() )
                {
                    if( !methodClass.equals( mapping.getKey() ) )
                    {
                        addModifiers( method, mapping.getValue().getModifiers(), modifierClasses );
                    }
                }

                modifiers.put( method, modifierClasses );
            }
        }
    }

    // Public --------------------------------------------------------
    public Composite getComposite()
    {
        return composite;
    }

    public Class getCompositeInterface()
    {
        return compositeInterface;
    }

    public CompositeInterface getWrappedComposite()
    {
        return wrappedComposite;
    }

    public Mixin getMixin( Class anInterface )
    {
        Mixin mixin = mixins.get( anInterface );

        if( mixin == null )
        {
            List<Mixin> possibleMixins = composite.getImplementations( anInterface );

            // Add from composite, if necessary
            if( wrappedComposite != null && !anInterface.isAssignableFrom( wrappedComposite.getCompositeInterface() ) )
            {
                List<Mixin> possibleWrapperMixins = wrappedComposite.getComposite().getImplementations( anInterface );
                if( possibleWrapperMixins.size() > 0 )
                {
                    possibleMixins = new ArrayList<Mixin>( possibleMixins );
                    possibleMixins.addAll( possibleWrapperMixins );
                }
            }

            nextmixin:
            for( Mixin possibleMixin : possibleMixins )
            {
                // Check if this mixin is valid

                // Verify that all @Uses fields can be resolved
                List<Field> uses = possibleMixin.getUsesFields();
                for( Field use : uses )
                {
                    Class<?> useInterface = use.getType();
                    if( !useInterface.equals( anInterface ) && getMixin( useInterface ) != null && wrappedComposite != null && !wrappedComposite.isAssignableFrom( use.getType() ) )
                    {
                        continue nextmixin;
                    }
                }

                mixin = possibleMixin;
                break;
            }

            if( mixin != null )
            {
                mixins.put( anInterface, mixin );
            }
        }

        return mixin;
    }

    public boolean isAssignableFrom( Class anInterface )
    {
        return getMixin( anInterface ) != null || ( wrappedComposite != null && wrappedComposite.isAssignableFrom( anInterface ) );
    }

    public List<Modifier> getModifiers( Method aMethod )
    {
        return modifiers.get( aMethod );
    }

    // Object overrides ---------------------------------------------
    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( composite.getCompositeClass().getName() );
        for( Iterator<Map.Entry<Class, Mixin>> iterator = mixins.entrySet().iterator(); iterator.hasNext(); )
        {
            Map.Entry<Class, Mixin> entry = iterator.next();
            Class interfaceClass = entry.getKey();
            Mixin mixin = entry.getValue();
            out.println( "  " + interfaceClass.getName() );
            out.println( "  implemented by " + mixin.getFragmentClass().getName() );
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


    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        CompositeInterface that = (CompositeInterface) o;

        if( !composite.equals( that.composite ) )
        {
            return false;
        }
        if( !compositeInterface.equals( that.compositeInterface ) )
        {
            return false;
        }
        if( wrappedComposite != null ? !wrappedComposite.equals( that.wrappedComposite ) : that.wrappedComposite != null )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return compositeInterface.hashCode();
    }

    // Private -------------------------------------------------------
    private void addModifiers( Method method, List<Modifier> aModifierList, List<Modifier> aMethodModifierList )
    {
        nextmodifier:
        for( Modifier modifier : aModifierList )
        {
            if( !aMethodModifierList.contains( modifier ) )
            {
                // Check AppliesTo
                Class appliesTo = modifier.getAppliesTo();
                if( appliesTo != null )
                {
                    // Check AppliesTo
                    if( appliesTo.isAnnotation() )
                    {
                        Mixin mixin = getMixin( method.getDeclaringClass() );

                        boolean ok = false;

                        if( mixin.getFragmentClass().getAnnotation( appliesTo ) == null )
                        {
                            // Check method
                            if( !mixin.isGeneric() )
                            {
                                try
                                {
                                    Method implMethod = mixin.getFragmentClass().getMethod( method.getName(), method.getParameterTypes() );
                                    if( implMethod.getAnnotation( appliesTo ) == null )
                                    {
                                        continue; // Skip this modifier
                                    }
                                }
                                catch( NoSuchMethodException e )
                                {
                                    continue; // Skip this modifier
                                }
                            }
                            else
                            {
                                continue; // Skip this modifier
                            }
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

                // Check interface
                if( !modifier.isGeneric() )
                {
                    if( !method.getDeclaringClass().isAssignableFrom( modifier.getFragmentClass() ) )
                    {
                        continue; // Modifier does not implement interface of this method
                    }
                }

                // Check @Uses
                List<Field> uses = modifier.getUsesFields();
                for( Field use : uses )
                {
                    if( getMixin( use.getType() ) != null && wrappedComposite != null && !wrappedComposite.isAssignableFrom( use.getType() ) )
                    {
                        continue nextmodifier;
                    }
                }

                // Modifier is ok!
                aMethodModifierList.add( modifier );
            }
        }
    }
}
