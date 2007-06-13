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
import java.util.List;
import java.util.Map;
import org.qi4j.api.annotation.Uses;

/**
 * The CompositeObject is the runtime resolution of how a compile-time CompositeModel
 * will be instantiated. It decides what mixins to be used for each
 * interface and what modifiers to use for each method.
 * <p/>
 * It also considers whether a method or interface is backed by the compositeModel
 * itself or a wrapped object.
 *
 * @see CompositeModel
 */
public final class CompositeObject
{
    // Attributes ----------------------------------------------------
    private CompositeModel compositeModel;
    private Class compositeInterface;
    private CompositeObject wrappedComposite;
    private Class wrappedInterface;
    private Map<Class, MixinModel> mixins = new HashMap<Class, MixinModel>();
    private Map<Method, List<ModifierModel>> modifiers = new HashMap<Method, List<ModifierModel>>();

    // Constructors --------------------------------------------------
    public CompositeObject( CompositeModel aCompositeModel, Class anInterface )
    {
        this( aCompositeModel, anInterface, null, null );
    }

    public CompositeObject( CompositeModel aCompositeModel, Class anInterface, CompositeObject aWrappedComposite, Class aWrappedInterface )
    {
        this.compositeModel = aCompositeModel;
        this.compositeInterface = anInterface;
        this.wrappedComposite = aWrappedComposite;
        this.wrappedInterface = aWrappedInterface;

        Method[] methods = anInterface.getMethods();
        for( Method method : methods )
        {
            // Find mixin
            Class<?> methodClass = method.getDeclaringClass();
            MixinModel mixinModel = getMixin( methodClass );
            if( mixinModel == null && ( aWrappedComposite == null || !aWrappedComposite.isAssignableFrom( methodClass ) ) && ( aWrappedInterface == null || !methodClass.isAssignableFrom( aWrappedInterface ) ) )
            {
                throw new IllegalStateException( "No implementation for interface " + methodClass.getName() + " found in composite " + compositeModel.getCompositeClass().getName() );
            }
        }

        for( Method method : methods )
        {
            // Find modifiers for method
            findModifiers( method );
        }

        // Add additional latent mixins
        for( MixinModel mixinModel : compositeModel.getImplementations() )
        {
            if (!mixinModel.isGeneric())
            {
                Class fragmentClass = mixinModel.getFragmentClass();
                Class[] interfaces = fragmentClass.getInterfaces();
                for( Class mixinInterface : interfaces )
                {
                    for( Method method : mixinInterface.getMethods() )
                    {
                        if( getModifiers( method ) == null )
                        {
                            findModifiers( method );
                        }
                    }
                }
            }
        }
    }

    // Public --------------------------------------------------------
    public CompositeModel getCompositeModel()
    {
        return compositeModel;
    }

    public Class getCompositeInterface()
    {
        return compositeInterface;
    }

    public CompositeObject getWrappedCompositeModel()
    {
        return wrappedComposite;
    }

    public Class getWrappedInterface()
    {
        return wrappedInterface;
    }

    public MixinModel getMixin( Class anInterface )
    {
        MixinModel mixinModel = mixins.get( anInterface );

        if( mixinModel == null )
        {
            List<MixinModel> possibleMixinModels = compositeModel.getImplementations( anInterface );

            // Add from compositeModel, if necessary
            if( wrappedComposite != null && !anInterface.isAssignableFrom( wrappedComposite.getCompositeInterface() ) )
            {
                List<MixinModel> possibleWrapperMixinModels = wrappedComposite.getCompositeModel().getImplementations( anInterface );
                if( possibleWrapperMixinModels.size() > 0 )
                {
                    possibleMixinModels = new ArrayList<MixinModel>( possibleMixinModels );
                    possibleMixinModels.addAll( possibleWrapperMixinModels );
                }
            }

            for( MixinModel possibleMixinModel : possibleMixinModels )
            {
                // Check if this mixinModel is valid
                if( isMixinValid( possibleMixinModel, anInterface ) )
                {
                    mixinModel = possibleMixinModel;
                    break;
                }
            }

            if( mixinModel != null )
            {
                mixins.put( anInterface, mixinModel );
            }
        }

        return mixinModel;
    }

    private boolean isMixinValid( MixinModel possibleMixinModel, Class anInterface )
    {
        // Verify that all @Uses fields can be resolved
        List<Field> uses = possibleMixinModel.getUsesFields();
        for( Field use : uses )
        {
            if (use.getAnnotation( Uses.class).optional())
                continue; // Don't bothing checking this - it's either there or not and in both cases the mixin is valid
            Class<?> useInterface = use.getType();
            if( !useInterface.equals( anInterface ) &&
                getMixin( useInterface ) != null &&
                wrappedComposite != null &&
                !wrappedComposite.isAssignableFrom( useInterface ) )
            {
                return false;
            }
        }
        return true;
    }

    public boolean isAssignableFrom( Class anInterface )
    {
        return getMixin( anInterface ) != null || anInterface.isAssignableFrom( compositeInterface ) || ( wrappedComposite != null && wrappedComposite.isAssignableFrom( anInterface ) );
    }

    public List<ModifierModel> getModifiers( Method aMethod )
    {
        return modifiers.get( aMethod );
    }

    // Object overrides ---------------------------------------------
    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( compositeModel.getCompositeClass().getName() );
        for( Map.Entry<Class, MixinModel> entry : mixins.entrySet() )
        {
            Class interfaceClass = entry.getKey();
            MixinModel mixinModel = entry.getValue();
            out.println( "  " + interfaceClass.getName() );
            out.println( "  implemented by " + mixinModel.getFragmentClass().getName() );
            Method[] methods = interfaceClass.getMethods();
            for( Method method : methods )
            {
                out.println( "    " + method.toGenericString() );
                List<ModifierModel> methodModifierModels = getModifiers( method );
                for( ModifierModel methodModifierModel : methodModifierModels )
                {
                    out.println( "      " + methodModifierModel.getFragmentClass().getName() );
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

        CompositeObject that = (CompositeObject) o;

        if( !compositeModel.equals( that.compositeModel ) )
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
    private void findModifiers( Method method )
    {
        List<ModifierModel> modifierClassModels = new ArrayList<ModifierModel>();

        // 1) Interface modifiers
        addModifiers( method, compositeModel.getModifiers(), modifierClassModels );

        // 2) MixinModel modifiers
        Class<?> methodClass = method.getDeclaringClass();
        MixinModel mixinModel = getMixin( methodClass );
        if( mixinModel != null )
        {
            addModifiers( method, mixinModel.getModifiers(), modifierClassModels );
        }

        // 3) Modifiers from other mixins
        for( Map.Entry<Class, MixinModel> mapping : mixins.entrySet() )
        {
            if( !methodClass.equals( mapping.getKey() ) )
            {
                addModifiers( method, mapping.getValue().getModifiers(), modifierClassModels );
            }
        }

        modifiers.put( method, modifierClassModels );
    }

    private void addModifiers( Method method, List<ModifierModel> aModifierList, List<ModifierModel> aMethodModifierList )
    {
        nextmodifier:
        for( ModifierModel modifierModel : aModifierList )
        {
            if( !aMethodModifierList.contains( modifierModel ) )
            {
                // Check AppliesTo
                Class appliesTo = modifierModel.getAppliesTo();
                if( appliesTo != null )
                {
                    // Check AppliesTo
                    if( appliesTo.isAnnotation() )
                    {
                        MixinModel mixinModel = getMixin( method.getDeclaringClass() );

                        if( mixinModel.getFragmentClass().getAnnotation( appliesTo ) == null )
                        {
                            // Check method
                            if( !mixinModel.isGeneric() )
                            {
                                try
                                {
                                    Method implMethod = mixinModel.getFragmentClass().getMethod( method.getName(), method.getParameterTypes() );
                                    if( implMethod.getAnnotation( appliesTo ) == null )
                                    {
                                        continue; // Skip this modifierModel
                                    }
                                }
                                catch( NoSuchMethodException e )
                                {
                                    continue; // Skip this modifierModel
                                }
                            }
                            else
                            {
                                continue; // Skip this modifierModel
                            }
                        }
                    }
                    else
                    {
                        Class<?> methodDeclaringClass = method.getDeclaringClass();
                        MixinModel mixin = getMixin( methodDeclaringClass );
                        Class fragmentClass = mixin.getFragmentClass();
                        if( !appliesTo.isAssignableFrom( fragmentClass ) && !appliesTo.isAssignableFrom( methodDeclaringClass ) )
                        {
                            continue; // Skip this modifierModel
                        }
                    }
                }

                // Check interface
                if( !modifierModel.isGeneric() )
                {
                    if( !method.getDeclaringClass().isAssignableFrom( modifierModel.getFragmentClass() ) )
                    {
                        continue; // ModifierModel does not implement interface of this method
                    }
                }

                // Check @Uses
                List<Field> uses = modifierModel.getUsesFields();
                for( Field use : uses )
                {
                    if (!use.getAnnotation( Uses.class).optional())
                    {
                        // The field is not optional - verify that it can be resolved
                        if( getMixin( use.getType() ) != null && wrappedComposite != null && !wrappedComposite.isAssignableFrom( use.getType() ) )
                        {
                            continue nextmodifier;
                        }
                    }
                }

                // ModifierModel is ok!
                aMethodModifierList.add( modifierModel );
            }
        }
    }
}
