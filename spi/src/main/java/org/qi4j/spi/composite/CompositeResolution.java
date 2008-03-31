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
package org.qi4j.spi.composite;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import org.qi4j.spi.structure.CompositeDescriptor;

/**
 * Composite Resolutions are Composite Models that are resolved in a particular Module.
 */
public final class CompositeResolution
{
    private CompositeDescriptor compositeDescriptor;
    private Iterable<CompositeMethodResolution> compositeMethodResolutions;
    private Iterable<MixinResolution> mixinResolutions;
    private int mixinCount;
    private Iterable<PropertyResolution> propertyResolutions;
    private Iterable<AssociationResolution> associationResolutions;

    public CompositeResolution( CompositeDescriptor compositeDescriptor, Iterable<CompositeMethodResolution> compositeMethodResolutions, Collection<MixinResolution> mixinResolutions, Iterable<PropertyResolution> propertyResolutions, Iterable<AssociationResolution> associationResolutions )
    {
        this.associationResolutions = associationResolutions;
        this.propertyResolutions = propertyResolutions;
        this.mixinResolutions = mixinResolutions;
        this.compositeDescriptor = compositeDescriptor;
        this.compositeMethodResolutions = compositeMethodResolutions;
        mixinCount = mixinResolutions.size();
    }

    public CompositeDescriptor getCompositeDescriptor()
    {
        return compositeDescriptor;
    }

    public CompositeModel getCompositeModel()
    {
        return compositeDescriptor.getCompositeModel();
    }

    public Iterable<CompositeMethodResolution> getCompositeMethodResolutions()
    {
        return compositeMethodResolutions;
    }

    public Iterable<MixinResolution> getMixinResolutions()
    {
        return mixinResolutions;
    }

    public Iterable<PropertyResolution> getPropertyResolutions()
    {
        return propertyResolutions;
    }

    public Iterable<AssociationResolution> getAssociationResolutions()
    {
        return associationResolutions;
    }

    @Override public int hashCode()
    {
        return compositeDescriptor.getCompositeModel().hashCode();
    }

    public String toString()
    {
        return compositeDescriptor.getCompositeModel().toString();
/*
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( compositeModel.getCompositeType().getName() );

        for( MethodResolution compositeMethodResolution : methodResolutionList )
        {
            out.println( "      " + compositeMethodResolution );
            out.println( "        implemented by " + compositeMethodResolution.getMixinResolution().getMixinModel().getModelClass().getName() + " (declared by " + compositeMethodResolution.getMixinResolution().getMixinModel().getDeclaredBy().getName() + ")" );

            List<ConcernResolution> methodConcerns = compositeMethodResolution.getConcernBindings();
            if( methodConcerns.size() > 0 )
            {
                out.println( "        concernBindings" );
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

            List<SideEffectResolution> methodSideEffects = compositeMethodResolution.getSideEffectBindings();
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

    public int getMixinCount()
    {
        return mixinCount;
    }

    public static class MethodResolutionComparator
        implements Comparator<MethodResolution>
    {
        public int compare( MethodResolution methodResolution1, MethodResolution methodResolution2 )
        {
            Method method1 = methodResolution1.getMethodModel().getMethod();
            Method method2 = methodResolution2.getMethodModel().getMethod();
            Class<?> declaringClass1 = method1.getDeclaringClass();
            Class<?> declaringClass2 = method2.getDeclaringClass();
            if( declaringClass1 != declaringClass2 )
            {
                String name = declaringClass1.getName();
                String name1 = declaringClass2.getName();
                return name.compareTo( name1 );
            }

            String methodName1 = method1.getName();
            String methodName2 = method2.getName();
            return methodName1.compareTo( methodName2 );
        }
    }
}