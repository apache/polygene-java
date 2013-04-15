/*
 * Copyright 2007 Rickard Öberg.
 * Copyright 2007 Niclas Hedhman.
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2012 Paul Merlin.
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
 * ied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.api.query;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.GenericAssociationInfo;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.grammar.AndSpecification;
import org.qi4j.api.query.grammar.AssociationFunction;
import org.qi4j.api.query.grammar.AssociationNotNullSpecification;
import org.qi4j.api.query.grammar.AssociationNullSpecification;
import org.qi4j.api.query.grammar.ContainsAllSpecification;
import org.qi4j.api.query.grammar.ContainsSpecification;
import org.qi4j.api.query.grammar.EqSpecification;
import org.qi4j.api.query.grammar.GeSpecification;
import org.qi4j.api.query.grammar.GtSpecification;
import org.qi4j.api.query.grammar.LeSpecification;
import org.qi4j.api.query.grammar.LtSpecification;
import org.qi4j.api.query.grammar.ManyAssociationContainsSpecification;
import org.qi4j.api.query.grammar.ManyAssociationFunction;
import org.qi4j.api.query.grammar.MatchesSpecification;
import org.qi4j.api.query.grammar.NeSpecification;
import org.qi4j.api.query.grammar.NotSpecification;
import org.qi4j.api.query.grammar.OrSpecification;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.query.grammar.PropertyNotNullSpecification;
import org.qi4j.api.query.grammar.PropertyNullSpecification;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.query.grammar.Variable;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.functional.Specification;

import static org.qi4j.functional.Iterables.prepend;

/**
 * Static factory methods for query expressions and operators.
 */
public final class QueryExpressions
{
    // This is used for eq(Association,Composite)
    private static final Method IDENTITY_METHOD;

    static
    {
        try
        {
            IDENTITY_METHOD = Identity.class.getMethod( "identity" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core API codebase is corrupted. Contact Qi4j team: QueryExpressions" );
        }
    }

    // Templates and variables -----------------------------------------------|

    /**
     * Create a Query Template using the given type.
     *
     * @param <T> the type of the template
     * @param clazz a class declaring the type of the template
     *
     * @return a new Query Template
     */
    public static <T> T templateFor( Class<T> clazz )
    {
        NullArgumentException.validateNotNull( "Template class", clazz );

        if( clazz.isInterface() )
        {
            return clazz.cast( Proxy.newProxyInstance( clazz.getClassLoader(),
                                                       new Class<?>[] { clazz },
                                                       new TemplateHandler<T>( null, null, null ) ) );
        }
        else
        {
            try
            {
                T mixin = clazz.newInstance();
                for( Field field : clazz.getFields() )
                {
                    if( field.getAnnotation( State.class ) != null )
                    {
                        if( field.getType().equals( Property.class ) )
                        {
                            field.set( mixin,
                                       Proxy.newProxyInstance( field.getType().getClassLoader(),
                                                               new Class<?>[]{ field.getType() },
                                                               new PropertyReferenceHandler<T>( new PropertyFunction<T>( null, null, null, field ) ) ) );
                        }
                        else if( field.getType().equals( Association.class ) )
                        {
                            field.set( mixin,
                                       Proxy.newProxyInstance( field.getType().getClassLoader(),
                                                               new Class<?>[]{ field.getType() },
                                                               new AssociationReferenceHandler<T>( new AssociationFunction<T>( null, null, field ) ) ) );
                        }
                        else if( field.getType().equals( Property.class ) )
                        {
                            field.set( mixin,
                                       Proxy.newProxyInstance( field.getType().getClassLoader(),
                                                               new Class<?>[]{ field.getType() },
                                                               new ManyAssociationReferenceHandler<T>( new ManyAssociationFunction<T>( null, null, field ) ) ) );
                        }
                    }
                }
                return mixin;
            }
            catch( Throwable e )
            {
                throw new IllegalArgumentException( "Cannot use class as template", e );
            }
        }
    }

    /**
     * Create a Query Template using the given mixin class and association.
     *
     * @param <T> the type of the template
     * @param mixinType  a class declaring the type of the template
     * @param association an association
     *
     * @return a new Query Template
     */
    public static <T> T templateFor( final Class<T> mixinType, Association<?> association )
    {
        NullArgumentException.validateNotNull( "Mixin class", mixinType );
        NullArgumentException.validateNotNull( "Association", association );
        return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(),
                                                       new Class<?>[]{ mixinType },
                                                       new TemplateHandler<T>( null, association( association ), null ) ) );
    }

    public static <T> T oneOf( final ManyAssociation<T> association )
    {
        NullArgumentException.validateNotNull( "Association", association );
        return association.get( 0 );
    }

    /**
     * Create a new Query Variable.
     *
     * @param name a name for the Variable
     *
     * @return a new Query Variable.
     */
    public static Variable variable( String name )
    {
        NullArgumentException.validateNotNull( "Variable name", name );
        return new Variable( name );
    }

    /**
     * Create a new Query Template PropertyFunction.
     *
     * @param <T> type of the Property
     * @param property a Property
     *
     * @return a new Query Template PropertyFunction
     */
    @SuppressWarnings( "unchecked" )
    public static <T> PropertyFunction<T> property( Property<T> property )
    {
        return ( ( PropertyReferenceHandler<T> ) Proxy.getInvocationHandler( property ) ).property();
    }

    /**
     * Create a new Query Property instance.
     *
     * @param <T> type of the Property
     * @param mixinClass mixin of the Property
     * @param fieldName name of the Property field
     *
     * @return a new Query Property instance for the given mixin and property name.
     */
    @SuppressWarnings( "unchecked" )
    public static <T> Property<T> property( Class<?> mixinClass, String fieldName )
    {
        try
        {
            Field field = mixinClass.getField( fieldName );
            if( !Property.class.isAssignableFrom( field.getType() ) )
            {
                throw new IllegalArgumentException( "Field must be of type Property<?>" );
            }
            return ( Property<T> ) Proxy.newProxyInstance(
                    mixinClass.getClassLoader(),
                    new Class<?>[]{ field.getType() },
                    new PropertyReferenceHandler<T>( new PropertyFunction<T>( null, null, null, field ) ) );
        }
        catch( NoSuchFieldException e )
        {
            throw new IllegalArgumentException( "No such field '" + fieldName + "' in mixin " + mixinClass.getName() );
        }
    }

    /**
     * Create a new Query Template AssociationFunction.
     *
     * @param <T> type of the Association
     * @param association an Association
     *
     * @return a new Query Template AssociationFunction
     */
    @SuppressWarnings( "unchecked" )
    public static <T> AssociationFunction<T> association( Association<T> association )
    {
        return ( ( AssociationReferenceHandler<T> ) Proxy.getInvocationHandler( association ) ).association();
    }

    /**
     * Create a new Query Template ManyAssociationFunction.
     *
     * @param <T> type of the ManyAssociation
     * @param association a ManyAssociation
     *
     * @return a new Query Template ManyAssociationFunction
     */
    @SuppressWarnings( "unchecked" )
    public static <T> ManyAssociationFunction<T> manyAssociation( ManyAssociation<T> association )
    {
        return ( ( ManyAssociationReferenceHandler<T> ) Proxy.getInvocationHandler( association ) ).manyAssociation();
    }

    // And/Or/Not ------------------------------------------------------------|

    /**
     * Create a new AND specification.
     *
     * @param left first operand
     * @param right second operand
     * @param optionalRight optional operands
     *
     * @return a new AND specification
     */
    public static AndSpecification and( Specification<Composite> left,
                                        Specification<Composite> right,
                                        Specification<Composite>... optionalRight
    )
    {
        return new AndSpecification( prepend( left, prepend( right, Arrays.asList( optionalRight ) ) ) );
    }

    /**
     * Create a new OR specification.
     *
     * @param left first operand
     * @param right second operand
     * @param optionalRight optional operands
     *
     * @return a new OR specification
     */
    public static OrSpecification or( Specification<Composite> left,
                                      Specification<Composite> right,
                                      Specification<Composite>... optionalRight
    )
    {
        return new OrSpecification( prepend( left, prepend( right, Arrays.asList( optionalRight ) ) ) );
    }

    /**
     * Create a new NOT specification.
     *
     * @param operand specification to be negated
     *
     * @return a new NOT specification
     */
    public static NotSpecification not( Specification<Composite> operand )
    {
        return new NotSpecification( operand );
    }

    // Comparisons -----------------------------------------------------------|

    /**
     * Create a new EQUALS specification for a Property.
     *
     * @param property a Property
     * @param value its value
     *
     * @return a new EQUALS specification for a Property.
     */
    public static <T> EqSpecification<T> eq( Property<T> property, T value )
    {
        return new EqSpecification<T>( property( property ), value );
    }

    /**
     * Create a new EQUALS specification for a Property using a named Variable.
     *
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new EQUALS specification for a Property using a named Variable.
     */
    public static <T> EqSpecification<T> eq( Property<T> property, Variable variable )
    {
        return new EqSpecification( property( property ), variable );
    }

    /**
     * Create a new EQUALS specification for an Association.
     *
     * @param association an Association
     * @param value its value
     *
     * @return a new EQUALS specification for an Association.
     */
    public static <T> EqSpecification<String> eq( Association<T> association, T value )
    {
        return new EqSpecification<String>( new PropertyFunction<String>( null,
                                                                          association( association ),
                                                                          null,
                                                                          IDENTITY_METHOD ),
                                            value.toString() );
    }

    /**
     * Create a new GREATER OR EQUALS specification for a Property.
     *
     * @param property a Property
     * @param value its value
     *
     * @return a new GREATER OR EQUALS specification for a Property.
     */
    public static <T> GeSpecification<T> ge( Property<T> property, T value )
    {
        return new GeSpecification<T>( property( property ), value );
    }

    /**
     * Create a new GREATER OR EQUALS specification for a Property using a named Variable.
     *
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new GREATER OR EQUALS specification for a Property using a named Variable.
     */
    public static <T> GeSpecification<T> ge( Property<T> property, Variable variable )
    {
        return new GeSpecification( property( property ), variable );
    }

    /**
     * Create a new GREATER THAN specification for a Property.
     *
     * @param property a Property
     * @param value its value
     *
     * @return a new GREATER THAN specification for a Property.
     */
    public static <T> GtSpecification<T> gt( Property<T> property, T value )
    {
        return new GtSpecification<T>( property( property ), value );
    }

    /**
     * Create a new GREATER THAN specification for a Property using a named Variable.
     *
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new GREATER THAN specification for a Property using a named Variable.
     */
    public static <T> GtSpecification<T> gt( Property<T> property, Variable variable )
    {
        return new GtSpecification( property( property ), variable );
    }

    /**
     * Create a new LESS OR EQUALS specification for a Property.
     *
     * @param property a Property
     * @param value its value
     *
     * @return a new LESS OR EQUALS specification for a Property.
     */
    public static <T> LeSpecification<T> le( Property<T> property, T value )
    {
        return new LeSpecification<T>( property( property ), value );
    }

    /**
     * Create a new LESS OR EQUALS specification for a Property using a named Variable.
     *
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new LESS OR EQUALS specification for a Property using a named Variable.
     */
    public static <T> LeSpecification<T> le( Property<T> property, Variable variable )
    {
        return new LeSpecification( property( property ), variable );
    }

    /**
     * Create a new LESSER THAN specification for a Property.
     *
     * @param property a Property
     * @param value its value
     *
     * @return a new LESSER THAN specification for a Property.
     */
    public static <T> LtSpecification<T> lt( Property<T> property, T value )
    {
        return new LtSpecification<T>( property( property ), value );
    }

    /**
     * Create a new LESSER THAN specification for a Property using a named Variable.
     *
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new LESSER THAN specification for a Property using a named Variable.
     */
    public static <T> LtSpecification<T> lt( Property<T> property, Variable variable )
    {
        return new LtSpecification( property( property ), variable );
    }

    /**
     * Create a new NOT EQUALS specification for a Property.
     *
     * @param property a Property
     * @param value its value
     *
     * @return a new NOT EQUALS specification for a Property.
     */
    public static <T> NeSpecification<T> ne( Property<T> property, T value )
    {
        return new NeSpecification<T>( property( property ), value );
    }

    /**
     * Create a new NOT EQUALS specification for a Property using a named Variable.
     *
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new NOT EQUALS specification for a Property using a named Variable.
     */
    public static <T> NeSpecification<T> ne( Property<T> property, Variable variable )
    {
        return new NeSpecification( property( property ), variable );
    }

    /**
     * Create a new REGULAR EXPRESSION specification for a Property.
     *
     * @param property a Property
     * @param regexp its value
     *
     * @return a new REGULAR EXPRESSION specification for a Property.
     */
    public static MatchesSpecification matches( Property<String> property, String regexp )
    {
        return new MatchesSpecification( property( property ), regexp );
    }

    /**
     * Create a new REGULAR EXPRESSION specification for a Property using a named Variable.
     *
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new REGULAR EXPRESSION specification for a Property using a named Variable.
     */
    public static MatchesSpecification matches( Property<String> property, Variable variable )
    {
        return new MatchesSpecification( property( property ), variable );
    }

    // Null checks -----------------------------------------------------------|

    /**
     * Create a new NOT NULL specification for a Property.
     *
     * @param property a Property
     *
     * @return a new NOT NULL specification for a Property.
     */
    public static <T> PropertyNotNullSpecification<T> isNotNull( Property<T> property )
    {
        return new PropertyNotNullSpecification<T>( property( property ) );
    }

    /**
     * Create a new NULL specification for a Property.
     *
     * @param property a Property
     *
     * @return a new NULL specification for a Property.
     */
    public static <T> PropertyNullSpecification<T> isNull( Property<T> property )
    {
        return new PropertyNullSpecification<T>( property( property ) );
    }

    /**
     * Create a new NOT NULL specification for an Association.
     *
     * @param association an Association
     *
     * @return a new NOT NULL specification for an Association.
     */
    public static <T> AssociationNotNullSpecification<T> isNotNull( Association<T> association )
    {
        return new AssociationNotNullSpecification<T>( association( association ) );
    }

    /**
     * Create a new NULL specification for an Association.
     *
     * @param association an Association
     *
     * @return a new NULL specification for an Association.
     */
    public static <T> AssociationNullSpecification<T> isNull( Association<T> association )
    {
        return new AssociationNullSpecification<T>( association( association ) );
    }

    // Collections -----------------------------------------------------------|

    /**
     * Create a new CONTAINS ALL specification for a Collection Property.
     *
     * @param collectionProperty a Collection Property
     * @param values its values
     *
     * @return a new CONTAINS ALL specification for a Collection Property.
     */
    public static <T> ContainsAllSpecification<T> containsAll( Property<? extends Collection<T>> collectionProperty,
                                                               Iterable<T> values )
    {
        NullArgumentException.validateNotNull( "Values", values );
        return new ContainsAllSpecification<T>( property( collectionProperty ), values );
    }

    /**
     * Create a new CONTAINS ALL specification for a Collection Property using named Variables.
     *
     * @param collectionProperty a Collection Property
     * @param variables named Variables
     *
     * @return a new CONTAINS ALL specification for a Collection Property using named Variables.
     */
    public static <T> ContainsAllSpecification<T> containsAllVariables(
            Property<? extends Collection<T>> collectionProperty,
            Iterable<Variable> variables )
    {
        NullArgumentException.validateNotNull( "Variables", variables );
        return new ContainsAllSpecification( property( collectionProperty ), variables );
    }

    /**
     * Create a new CONTAINS specification for a Collection Property.
     *
     * @param collectionProperty a Collection Property
     * @param value the value
     *
     * @return a new CONTAINS specification for a Collection Property.
     */
    public static <T> ContainsSpecification<T> contains( Property<? extends Collection<T>> collectionProperty,
                                                         T value )
    {
        NullArgumentException.validateNotNull( "Value", value );
        return new ContainsSpecification<T>( property( collectionProperty ), value );
    }

    /**
     * Create a new CONTAINS specification for a Collection Property using named Variables.
     *
     * @param collectionProperty a Collection Property
     * @param variable named Variable
     *
     * @return a new CONTAINS specification for a Collection Property using named Variables.
     */
    public static <T> ContainsSpecification<T> contains( Property<? extends Collection<T>> collectionProperty,
                                                         Variable variable )
    {
        NullArgumentException.validateNotNull( "Variable", variable );
        return new ContainsSpecification( property( collectionProperty ), variable );
    }

    /**
     * Create a new CONTAINS specification for a ManyAssociation.
     *
     * @param manyAssoc  a ManyAssociation
     * @param value the value
     *
     * @return a new CONTAINS specification for a ManyAssociation.
     */
    public static <T> ManyAssociationContainsSpecification<T> contains( ManyAssociation<T> manyAssoc,
                                                                        T value )
    {
        return new ManyAssociationContainsSpecification<T>( manyAssociation( manyAssoc ), value );
    }

    // Ordering --------------------------------------------------------------|

    /**
     * Create a new Query ascending order segment for a Property.
     *
     * @param <T> type of the Property
     * @param property a Property
     *
     * @return a new Query ascending order segment for a Property.
     */
    public static <T> OrderBy orderBy( final Property<T> property )
    {
        return orderBy( property, OrderBy.Order.ASCENDING );
    }

    /**
     * Create a new Query ordering segment for a Property.
     *
     * @param <T> type of the Property
     * @param property a Property
     * @param order ascending or descending
     *
     * @return a new Query ordering segment for a Property.
     */
    public static <T> OrderBy orderBy( final Property<T> property, final OrderBy.Order order )
    {
        return new OrderBy( property( property ), order );
    }

    // Query Templates InvocationHandlers ------------------------------------|

    private static class TemplateHandler<T>
        implements InvocationHandler
    {
        private PropertyFunction<?> compositeProperty;
        private AssociationFunction<?> compositeAssociation;
        private ManyAssociationFunction<?> compositeManyAssociation;

        private TemplateHandler( PropertyFunction<?> CompositeProperty,
                                 AssociationFunction<?> CompositeAssociation,
                                 ManyAssociationFunction<?> CompositeManyAssociation
        )
        {
            this.compositeProperty = CompositeProperty;
            this.compositeAssociation = CompositeAssociation;
            this.compositeManyAssociation = CompositeManyAssociation;
        }

        @Override
        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance(
                        method.getReturnType().getClassLoader(),
                        new Class<?>[]{ method.getReturnType() },
                        new PropertyReferenceHandler<T>( new PropertyFunction<T>( compositeProperty,
                                                                                  compositeAssociation,
                                                                                  compositeManyAssociation,
                                                                                  method ) ) );
            }
            else if( Association.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance(
                        method.getReturnType().getClassLoader(),
                        new Class<?>[]{ method.getReturnType() },
                        new AssociationReferenceHandler<T>( new AssociationFunction<T>( compositeAssociation,
                                                                                        compositeManyAssociation,
                                                                                        method ) ) );
            }
            else if( ManyAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance(
                        method.getReturnType().getClassLoader(),
                        new Class<?>[]{ method.getReturnType() },
                        new ManyAssociationReferenceHandler<T>( new ManyAssociationFunction<T>( compositeAssociation,
                                                                                                compositeManyAssociation,
                                                                                                method ) ) );
            }

            return null;
        }
    }

    private static class PropertyReferenceHandler<T>
        implements InvocationHandler
    {
        private PropertyFunction<T> property;

        private PropertyReferenceHandler( PropertyFunction<T> property )
        {
            this.property = property;
        }

        private PropertyFunction<T> property()
        {
            return property;
        }

        @Override
        public Object invoke( Object o, final Method method, Object[] objects )
            throws Throwable
        {
            if( method.equals( Property.class.getMethod( "get" ) ) )
            {
                Type propertyType = GenericPropertyInfo.propertyTypeOf( property.accessor() );
                if( propertyType.getClass().equals( Class.class ) )
                {
                    return Proxy.newProxyInstance( method.getDeclaringClass().getClassLoader(),
                                                   new Class<?>[]{ (Class<?>) propertyType, PropertyReference.class },
                                                   new TemplateHandler<T>( property, null, null ) );
                }
            }

            return null;
        }
    }

    private static class AssociationReferenceHandler<T>
        implements InvocationHandler
    {
        private AssociationFunction<T> association;

        private AssociationReferenceHandler( AssociationFunction<T> association )
        {
            this.association = association;
        }

        private AssociationFunction<T> association()
        {
            return association;
        }

        @Override
        public Object invoke( Object o, final Method method, Object[] objects )
            throws Throwable
        {
            if( method.equals( Association.class.getMethod( "get" ) ) )
            {
                Type associationType = GenericAssociationInfo.associationTypeOf( association.accessor() );
                if( associationType.getClass().equals( Class.class ) )
                {
                    return Proxy.newProxyInstance( method.getDeclaringClass().getClassLoader(),
                                                   new Class<?>[]{ (Class) associationType, PropertyReference.class },
                                                   new TemplateHandler<T>( null, association, null ) );
                }
            }

            return null;
        }
    }

    private static class ManyAssociationReferenceHandler<T>
        implements InvocationHandler
    {
        private ManyAssociationFunction<T> manyAssociation;

        public ManyAssociationReferenceHandler( ManyAssociationFunction<T> manyAssociation )
        {
            this.manyAssociation = manyAssociation;
        }

        public ManyAssociationFunction<T> manyAssociation()
        {
            return manyAssociation;
        }

        @Override
        public Object invoke( Object o, final Method method, Object[] objects )
            throws Throwable
        {
            if( method.equals( ManyAssociation.class.getMethod( "get", Integer.TYPE ) ) )
            {
                Type manyAssociationType = GenericAssociationInfo.associationTypeOf( manyAssociation.accessor() );
                if( manyAssociationType.getClass().equals( Class.class ) )
                {
                    return Proxy.newProxyInstance( method.getDeclaringClass().getClassLoader(),
                                                   new Class<?>[]{ (Class) manyAssociationType, PropertyReference.class },
                                                   new TemplateHandler<T>( null, null, manyAssociation ) );
                }
            }

            return null;
        }
    }
}