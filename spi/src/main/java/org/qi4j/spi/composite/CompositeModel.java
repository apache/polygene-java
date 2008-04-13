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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.qi4j.composite.Composite;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.injection.InjectionModel;
import org.qi4j.spi.property.PropertyModel;

/**
 * Composite Models define what a particular Composite type declares through annotations and method declarations.
 */
public final class CompositeModel
{

    /**
     * Get URI for a composite class.
     *
     * @param compositeClass composite class
     * @return composite URI
     */
    public static String toURI( final Class<? extends Composite> compositeClass )
    {
        if( compositeClass == null )
        {
            return null;
        }
        String className = compositeClass.getName();
        className = className.replace( '$', '&' );
        return "urn:qi4j:" + className;
    }

    private Class<? extends Composite> compositeClass;
    private Class proxyClass;
    private Collection<CompositeMethodModel> compositeMethodModels;
    private Iterable<ConstraintModel> constraintModels;
    private Iterable<MixinModel> mixinModels;
    private Iterable<ConcernModel> concernModels;
    private Iterable<SideEffectModel> sideEffectModels;
    private Iterable<CompositeMethodModel> thisAsModels;
    private Iterable<PropertyModel> properties;
    private Iterable<AssociationModel> associations;
    private Iterable<MixinTypeModel> mixinTypes;

    private Map<Class<? extends Annotation>, List<ConstraintModel>> constraintModelMappings;
    private Map<Method, CompositeMethodModel> compositeMethodModelMap;

    public CompositeModel( Class<? extends Composite> compositeClass, Class proxyClass, Collection<CompositeMethodModel> methodModels, Iterable<MixinModel> mixinModels, Iterable<ConstraintModel> constraintModels, Iterable<ConcernModel> concernModels, Iterable<SideEffectModel> sideEffectModels, Iterable<CompositeMethodModel> thisAsModels, Map<Class<? extends Annotation>, List<ConstraintModel>> constraintModelMappings, Iterable<PropertyModel> properties, Iterable<AssociationModel> associations )
    {
        this.associations = associations;
        this.properties = properties;
        this.proxyClass = proxyClass;
        this.constraintModelMappings = constraintModelMappings;
        this.constraintModels = constraintModels;
        this.thisAsModels = thisAsModels;
        this.compositeMethodModels = methodModels;
        this.compositeClass = compositeClass;
        this.mixinModels = mixinModels;
        this.concernModels = concernModels;
        this.sideEffectModels = sideEffectModels;

        compositeMethodModelMap = new HashMap<Method, CompositeMethodModel>();
        for( CompositeMethodModel methodModel : methodModels )
        {
            compositeMethodModelMap.put( methodModel.getMethod(), methodModel );
        }
        for( CompositeMethodModel thisAsModel : thisAsModels )
        {
            compositeMethodModelMap.put( thisAsModel.getMethod(), thisAsModel );
        }
    }

    public Class<? extends Composite> getCompositeType()
    {
        return compositeClass;
    }

    public Iterable<MixinTypeModel> getMixinTypeModels()
    {
        if( mixinTypes == null )
        {
            mixinTypes = new TreeSet<MixinTypeModel>( extractSubTypes( compositeClass ) );
        }
        return mixinTypes;
    }

    private static Collection<MixinTypeModel> extractSubTypes( final Class clazz )
    {
        final Collection<MixinTypeModel> subTypes = new HashSet<MixinTypeModel>();
        for( Class subType : clazz.getInterfaces() )
        {
            subTypes.add( new MixinTypeModel( subType ) );
            subTypes.addAll( extractSubTypes( subType ) );
        }
        return subTypes;
    }

    public Class getProxyClass()
    {
        return proxyClass;
    }

    public Collection<CompositeMethodModel> getCompositeMethodModels()
    {
        return compositeMethodModels;
    }

    public Iterable<MixinModel> getMixinModels()
    {
        return mixinModels;
    }

    public Iterable<ConstraintModel> getConstraintModels()
    {
        return constraintModels;
    }

    public Iterable<ConcernModel> getConcernModels()
    {
        return concernModels;
    }

    public Iterable<SideEffectModel> getSideEffectModels()
    {
        return sideEffectModels;
    }

    public Iterable<CompositeMethodModel> getThisModels()
    {
        return thisAsModels;
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

    public ConstraintModel getConstraintModel( Class<? extends Annotation> annotationType, Type parameterType )
    {
        Iterable<ConstraintModel> possibleConstraintModels = constraintModelMappings.get( annotationType );

        if( possibleConstraintModels == null )
        {
            return null;
        }

        while( true )
        {
            for( ConstraintModel possibleConstraintModel : possibleConstraintModels )
            {
                if( possibleConstraintModel.getValueType().equals( parameterType ) )
                {
                    return possibleConstraintModel;
                }
            }

            if( parameterType.equals( Object.class ) )
            {
                return null; // No suitable constraint implementation found for this annotation
            }

            if( parameterType instanceof Class )
            {
                parameterType = ( (Class) parameterType ).getSuperclass(); // Try super-class
            }
            else
            {
                return null;
            }
        }
    }

    public Iterable<PropertyModel> getPropertyModels()
    {
        return properties;
    }

    public Iterable<AssociationModel> getAssociationModels()
    {
        return associations;
    }

    public String toURI()
    {
        return toURI( compositeClass );
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

        out.println( "  concerns available" );
        for( ConcernModel concernModel : concernModels )
        {
            out.println( "    " + concernModel.getModelClass().getName() );
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

    public Iterable<InjectionModel> getInjectionsByScope( Class<? extends Annotation> aClass )
    {
        List<InjectionModel> injectionModels = new ArrayList<InjectionModel>();
        for( MixinModel mixinModel : mixinModels )
        {
            Iterable<InjectionModel> scope = mixinModel.getInjectionsByScope( aClass );
            for( InjectionModel injectionModel : scope )
            {
                injectionModels.add( injectionModel );
            }
        }
        for( ConcernModel concernModel : concernModels )
        {
            Iterable<InjectionModel> scope = concernModel.getInjectionsByScope( aClass );
            for( InjectionModel injectionModel : scope )
            {
                injectionModels.add( injectionModel );
            }
        }
        for( SideEffectModel sideEffectModel : sideEffectModels )
        {
            Iterable<InjectionModel> scope = sideEffectModel.getInjectionsByScope( aClass );
            for( InjectionModel injectionModel : scope )
            {
                injectionModels.add( injectionModel );
            }
        }

        return injectionModels;
    }

    public CompositeMethodModel getCompositeMethodModel( Method key )
    {
        return compositeMethodModelMap.get( key );
    }
}
