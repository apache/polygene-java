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
package org.qi4j.runtime.resolution;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.Composite;
import org.qi4j.model.CompositeModel;

/**
 * Composite model instances are resolutions in a runtime environment what a composite model will look like.
 */
public final class CompositeResolution<T extends Composite>
{
    private CompositeModel<T> compositeModel;
    private Set<MixinResolution> resolvedMixinModels; // List of used mixin models

    private Map<Class, Set<MixinResolution>> mixinsForInterfaces; // Interface -> mixin resolutions
    private Map<Method, MethodResolution> methodResolutions; // Method -> method resolution
    private List<MethodResolution> methodResolutionList;

    public CompositeResolution( CompositeModel<T> compositeModel, Iterable<MethodResolution> methods )
    {
        this.compositeModel = compositeModel;
        this.mixinsForInterfaces = new HashMap<Class, Set<MixinResolution>>();

        methodResolutions = new HashMap<Method, MethodResolution>();
        resolvedMixinModels = new HashSet<MixinResolution>();
        methodResolutionList = new ArrayList<MethodResolution>();
        for( MethodResolution methodResolution : methods )
        {
            methodResolutions.put( methodResolution.getMethodModel().getMethod(), methodResolution );
            resolvedMixinModels.add( methodResolution.getMixinResolution() );

            Set<MixinResolution> mixinResolutions = mixinsForInterfaces.get( methodResolution.getMethodModel().getMethod().getDeclaringClass() );
            if( mixinResolutions == null )
            {
                mixinsForInterfaces.put( methodResolution.getMethodModel().getMethod().getDeclaringClass(), mixinResolutions = new HashSet<MixinResolution>() );
            }
            mixinResolutions.add( methodResolution.getMixinResolution() );
            methodResolutionList.add( methodResolution );
        }
        Collections.sort( methodResolutionList, new MethodResolutionComparator() );
    }

    public CompositeModel<T> getCompositeModel()
    {
        return compositeModel;
    }

    public Set<MixinResolution> getResolvedMixinModels()
    {
        return resolvedMixinModels;
    }

    public Set<MixinResolution> getMixinsForInterface( Class interfaceType )
    {
        return mixinsForInterfaces.get( interfaceType );
    }

    public MethodResolution getMethodResolution( Method method )
    {
        return methodResolutions.get( method );
    }

    public Collection<MethodResolution> getMethodResolutions()
    {
        return methodResolutions.values();
    }

    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( compositeModel.getCompositeClass().getName() );

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

        CompositeResolution composite1 = (CompositeResolution) o;

        return compositeModel.equals( composite1.getCompositeModel() );

    }

    public int hashCode()
    {
        return compositeModel.hashCode();
    }

    public static class MethodResolutionComparator
        implements Comparator<MethodResolution>
    {
        public int compare( MethodResolution methodResolution, MethodResolution methodResolution1 )
        {
            if( methodResolution.getMethodModel().getMethod().getDeclaringClass() != methodResolution1.getMethodModel().getMethod().getDeclaringClass() )
            {
                String name = methodResolution.getMethodModel().getMethod().getDeclaringClass().getName();
                String name1 = methodResolution1.getMethodModel().getMethod().getDeclaringClass().getName();
                return name.compareTo( name1 );
            }

            String method = methodResolution.getMethodModel().getMethod().getName();
            String method1 = methodResolution1.getMethodModel().getMethod().getName();
            return method.compareTo( method1 );
        }
    }
}