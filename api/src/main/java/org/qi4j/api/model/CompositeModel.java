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
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.api.Composite;

/**
 * Composites are descriptors of what an interface represent. <TODO better docs needed here>
 */
public final class CompositeModel<T extends Composite>
{
    private Class<T> compositeClass;
    private Class<? extends T> proxyClass;
    private Collection<MethodModel> methodModels;
    private Iterable<MixinModel> mixinModels;
    private Iterable<AssertionModel> assertionModels;
    private Iterable<SideEffectModel> sideEffectModels;

    public CompositeModel( Class<T> compositeClass, Class<? extends T> proxyClass, Collection<MethodModel> methodModels, Iterable<MixinModel> mixinModels, Iterable<AssertionModel> assertionModels, Iterable<SideEffectModel> sideEffectModels )
    {
        this.methodModels = methodModels;
        this.compositeClass = compositeClass;
        this.proxyClass = proxyClass;
        this.mixinModels = mixinModels;
        this.assertionModels = assertionModels;
        this.sideEffectModels = sideEffectModels;
    }

    public Class<T> getCompositeClass()
    {
        return compositeClass;
    }

    public Class<? extends T> getProxyClass()
    {
        return proxyClass;
    }

    public Collection<MethodModel> getMethodModels()
    {
        return methodModels;
    }

    public Iterable<MixinModel> getMixinModels()
    {
        return mixinModels;
    }

    public Iterable<AssertionModel> getAssertionModels()
    {
        return assertionModels;
    }

    public Iterable<SideEffectModel> getSideEffectModels()
    {
        return sideEffectModels;
    }

    public Iterable<MethodModel> getThisAsModels()
    {
        Set<MethodModel> methodModels = new HashSet<MethodModel>();
        for( MixinModel mixinModel : mixinModels )
        {
            methodModels.addAll( mixinModel.getThisAsMethods() );
            Iterable<AssertionModel> assertions = mixinModel.getAssertions();
            for( AssertionModel assertionModel : assertions )
            {
                methodModels.addAll( assertionModel.getThisAsMethods() );
            }
            Iterable<SideEffectModel> iterable = mixinModel.getSideEffects();
            for( SideEffectModel sideEffectModel : iterable )
            {
                methodModels.addAll( sideEffectModel.getThisAsMethods() );
            }
        }
        for( AssertionModel assertionModel : assertionModels )
        {
            methodModels.addAll( assertionModel.getThisAsMethods() );
        }
        for( SideEffectModel sideEffectModel : sideEffectModels )
        {
            methodModels.addAll( sideEffectModel.getThisAsMethods() );
        }
        return methodModels;
    }

    public List<MixinModel> getImplementations( Class aType )
    {
        List<MixinModel> impls = new ArrayList<MixinModel>();

        // Check non-generic impls first
        for( MixinModel implementation : mixinModels )
        {
            if( !implementation.isGeneric() )
            {
                Class fragmentClass = implementation.getModelClass();
                if( aType.isAssignableFrom( fragmentClass ) )
                {
                    impls.add( implementation );
                }
            }
        }

        // Check generic impls
        for( MixinModel implementation : mixinModels )
        {
            if( implementation.isGeneric() )
            {
                // Check AppliesTo
                Collection<Class> appliesTo = implementation.getAppliesTo();
                if( appliesTo == null )
                {
                    impls.add( implementation ); // This generic mixin can handle the given type
                }
                else
                {
                    for( Class appliesToClass : appliesTo )
                    {
                        if( appliesToClass.isAssignableFrom( aType ) )
                        {
                            impls.add( implementation );
                        }
                    }
                }
            }
        }

        return impls;
    }

    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( compositeClass.getName() );

        out.println( "  implementations available" );
        for( MixinModel implementation : mixinModels )
        {
            out.println( "    " + implementation.getModelClass().getName() );
        }

        out.println( "  assertions available" );
        for( AssertionModel assertionModel : assertionModels )
        {
            out.println( "    " + assertionModel.getModelClass().getName() );
        }

        out.println( "  side-effects available" );
        for( SideEffectModel sideEffectModel : sideEffectModels )
        {
            out.println( "    " + sideEffectModel.getModelClass().getName() );
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

        CompositeModel composite1 = (CompositeModel) o;

        return compositeClass.equals( composite1.compositeClass );

    }

    public int hashCode()
    {
        return compositeClass.hashCode();
    }

    public Iterable<Dependency> getDependenciesByScope( Class<? extends Annotation> aClass )
    {
        List<Dependency> dependencies = new ArrayList<Dependency>();
        for( MixinModel mixinModel : mixinModels )
        {
            Iterable<Dependency> scope = mixinModel.getDependenciesByScope( aClass );
            for( Dependency dependency : scope )
            {
                dependencies.add( dependency );
            }
        }
        for( AssertionModel assertionModel : assertionModels )
        {
            Iterable<Dependency> scope = assertionModel.getDependenciesByScope( aClass );
            for( Dependency dependency : scope )
            {
                dependencies.add( dependency );
            }
        }
        for( SideEffectModel sideEffectModel : sideEffectModels )
        {
            Iterable<Dependency> scope = sideEffectModel.getDependenciesByScope( aClass );
            for( Dependency dependency : scope )
            {
                dependencies.add( dependency );
            }
        }

        return dependencies;
    }
}
