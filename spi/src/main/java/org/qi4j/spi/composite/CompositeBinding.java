/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.spi.composite;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.qi4j.spi.association.AssociationBinding;
import org.qi4j.spi.property.PropertyBinding;

/**
 * A Composite Binding is the result of taking a Composite Resolution and performing
 * dependency binding on all its dependencies.
 */
public final class CompositeBinding
{
    private final CompositeResolution compositeResolution;
    private final Collection<CompositeMethodBinding> compositeMethodBindings;

    private final Set<MixinBinding> mixinBindings; // List of used mixin bindings

    private final Map<Method, CompositeMethodBinding> methodMappings; // Method -> method resolution

    private Iterable<PropertyBinding> propertyBindings;
    private Iterable<AssociationBinding> associationBindings;

    public CompositeBinding( CompositeResolution compositeResolution, Collection<CompositeMethodBinding> methodBindings, Set<MixinBinding> mixinBindings, Map<Method, CompositeMethodBinding> methodMappings, Iterable<PropertyBinding> propertyBindings, Iterable<AssociationBinding> associationBindings )
    {
        this.associationBindings = associationBindings;
        this.propertyBindings = propertyBindings;
        this.methodMappings = methodMappings;
        this.mixinBindings = mixinBindings;
        this.compositeResolution = compositeResolution;
        this.compositeMethodBindings = methodBindings;
    }

    public CompositeResolution getCompositeResolution()
    {
        return compositeResolution;
    }

    public Set<MixinBinding> getMixinBindings()
    {
        return mixinBindings;
    }

    public Collection<CompositeMethodBinding> getCompositeMethodBindings()
    {
        return compositeMethodBindings;
    }

    public CompositeMethodBinding getCompositeMethodBinding( Method method )
    {
        return methodMappings.get( method );
    }

    public Iterable<PropertyBinding> getPropertyBindings()
    {
        return propertyBindings;
    }

    public Iterable<AssociationBinding> getAssociationBindings()
    {
        return associationBindings;
    }

    public String toString()
    {
        return compositeResolution.toString();
/*
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( compositeModel.getCompositeType().getName() );

        for( MethodResolution methodResolution : methodResolutionList )
        {
            out.println( "      " + methodResolution );
            out.println( "        implemented by " + methodResolution.getMixinResolution().getMixinModel().getModelClass().getName() + " (declared by " + methodResolution.getMixinResolution().getMixinModel().getDeclaredBy().getName() + ")" );

            List<ConcernResolution> methodConcerns = methodResolution.getConcerns();
            if( methodConcerns.size() > 0 )
            {
                out.println( "        concerns" );
            }
            for( ConcernResolution methodConcern : methodConcerns )
            {
                out.print( "          " + methodConcern.getFragmentModel().getModelClass().getName() + " (declared by " + methodConcern.getConcernModel().getDeclaredBy().getName() + ")" );
                if( methodConcern.getFragmentModel() != null )
                {
                    if( !methodConcern.getConcernModel().getAppliesTo().isEmpty() )
                    {
                        out.print( " applies to " + methodConcern.getConcernModel().getAppliesTo() );
                    }
                }
                out.println();
            }

            List<SideEffectResolution> methodSideEffects = methodResolution.getSideEffects();
            if( methodSideEffects.size() > 0 )
            {
                out.println( "        side-effects" );
            }
            for( SideEffectResolution methodSideEffect : methodSideEffects )
            {
                out.print( "          " + methodSideEffect.getFragmentModel().getModelClass().getName() + " (declared by " + methodSideEffect.getSideEffectModel().getDeclaredBy().getName() + ")" );
                if( methodSideEffect.getFragmentModel() != null )
                {
                    if( !methodSideEffect.getSideEffectModel().getAppliesTo().isEmpty() )
                    {
                        out.print( " applies to " + methodSideEffect.getSideEffectModel().getAppliesTo() );
                    }
                }
                out.println();
            }
        }
        out.close();
        return str.toString();
*/
    }


    public int hashCode()
    {
        return compositeResolution.hashCode();
    }
}
