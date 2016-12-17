/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.api.query;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.GenericAssociationInfo;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.State;
import org.apache.polygene.api.property.GenericPropertyInfo;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.query.grammar.AndPredicate;
import org.apache.polygene.api.query.grammar.AssociationFunction;
import org.apache.polygene.api.query.grammar.AssociationNotNullPredicate;
import org.apache.polygene.api.query.grammar.AssociationNullPredicate;
import org.apache.polygene.api.query.grammar.ContainsAllPredicate;
import org.apache.polygene.api.query.grammar.ContainsPredicate;
import org.apache.polygene.api.query.grammar.EqPredicate;
import org.apache.polygene.api.query.grammar.GePredicate;
import org.apache.polygene.api.query.grammar.GtPredicate;
import org.apache.polygene.api.query.grammar.LePredicate;
import org.apache.polygene.api.query.grammar.LtPredicate;
import org.apache.polygene.api.query.grammar.ManyAssociationContainsPredicate;
import org.apache.polygene.api.query.grammar.ManyAssociationFunction;
import org.apache.polygene.api.query.grammar.MatchesPredicate;
import org.apache.polygene.api.query.grammar.NamedAssociationContainsNamePredicate;
import org.apache.polygene.api.query.grammar.NamedAssociationContainsPredicate;
import org.apache.polygene.api.query.grammar.NamedAssociationFunction;
import org.apache.polygene.api.query.grammar.NePredicate;
import org.apache.polygene.api.query.grammar.Notpredicate;
import org.apache.polygene.api.query.grammar.OrPredicate;
import org.apache.polygene.api.query.grammar.OrderBy;
import org.apache.polygene.api.query.grammar.PropertyFunction;
import org.apache.polygene.api.query.grammar.PropertyNotNullPredicate;
import org.apache.polygene.api.query.grammar.PropertyNullPredicate;
import org.apache.polygene.api.query.grammar.PropertyReference;
import org.apache.polygene.api.query.grammar.Variable;
import org.apache.polygene.api.util.NullArgumentException;

import static org.apache.polygene.api.identity.HasIdentity.IDENTITY_METHOD;

/**
 * Static factory methods for query expressions and operators.
 */
public final class QueryExpressions
{
    // This is used for eq(Association,Composite)

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
                                                       array( clazz ),
                                                       new TemplateHandler<T>( null, null, null, null ) ) );
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
                                                               array( field.getType() ),
                                                               new PropertyReferenceHandler<>( new PropertyFunction<T>( null, null, null, null, field ) ) ) );
                        }
                        else if( field.getType().equals( Association.class ) )
                        {
                            field.set( mixin,
                                       Proxy.newProxyInstance( field.getType().getClassLoader(),
                                                               array( field.getType() ),
                                                               new AssociationReferenceHandler<>( new AssociationFunction<T>( null, null, null, field ) ) ) );
                        }
                        else if( field.getType().equals( ManyAssociation.class ) )
                        {
                            field.set( mixin,
                                       Proxy.newProxyInstance( field.getType().getClassLoader(),
                                                               array( field.getType() ),
                                                               new ManyAssociationReferenceHandler<>( new ManyAssociationFunction<T>( null, null, null, field ) ) ) );
                        }
                        else if( field.getType().equals( NamedAssociation.class ) )
                        {
                            field.set( mixin,
                                       Proxy.newProxyInstance( field.getType().getClassLoader(),
                                                               array( field.getType() ),
                                                               new NamedAssociationReferenceHandler<>( new NamedAssociationFunction<T>( null, null, null, field ) ) ) );
                        }
                    }
                }
                return mixin;
            }
            catch( IllegalAccessException | IllegalArgumentException | InstantiationException | SecurityException e )
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
                                                       array( mixinType ),
                                                       new TemplateHandler<T>( null,
                                                                               association( association ),
                                                                               null,
                                                                               null ) ) );
    }

    public static <T> T oneOf( final ManyAssociation<T> association )
    {
        NullArgumentException.validateNotNull( "Association", association );
        return association.get( 0 );
    }

    public static <T> T oneOf( final NamedAssociation<T> association )
    {
        NullArgumentException.validateNotNull( "Association", association );
        Iterator<String> iterator = association.iterator();
        return association.get( iterator.hasNext() ? iterator.next() : null );
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
        return ( (PropertyReferenceHandler<T>) Proxy.getInvocationHandler( property ) ).property();
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
            return (Property<T>) Proxy.newProxyInstance(
                mixinClass.getClassLoader(),
                array( field.getType() ),
                new PropertyReferenceHandler<>( new PropertyFunction<T>( null, null, null, null, field ) ) );
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
        return ( (AssociationReferenceHandler<T>) Proxy.getInvocationHandler( association ) ).association();
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
        return ( (ManyAssociationReferenceHandler<T>) Proxy.getInvocationHandler( association ) ).manyAssociation();
    }

    /**
     * Create a new Query Template NamedAssociationFunction.
     *
     * @param <T> type of the NamedAssociation
     * @param association a NamedAssociation
     *
     * @return a new Query Template NamedAssociationFunction
     */
    @SuppressWarnings( "unchecked" )
    public static <T> NamedAssociationFunction<T> namedAssociation( NamedAssociation<T> association )
    {
        return ( (NamedAssociationReferenceHandler<T>) Proxy.getInvocationHandler( association ) ).namedAssociation();
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
    @SafeVarargs
    public static AndPredicate and( Predicate<Composite> left,
                                    Predicate<Composite> right,
                                    Predicate<Composite>... optionalRight
    )
    {
        List<Predicate<Composite>> predicates = new ArrayList<>( 2 + optionalRight.length );
        predicates.add( left );
        predicates.add( right );
        Collections.addAll( predicates, optionalRight );
        return new AndPredicate( predicates );
    }

    /**
     * Create a new OR specification.
     *
     * @param specs operands
     *
     * @return a new OR specification
     */
    @SafeVarargs
    public static OrPredicate or( Predicate<Composite>... specs )
    {
        return new OrPredicate( Arrays.asList( specs ) );
    }

    /**
     * Create a new NOT specification.
     *
     * @param operand specification to be negated
     *
     * @return a new NOT specification
     */
    public static Notpredicate not( Predicate<Composite> operand )
    {
        return new Notpredicate( operand );
    }

    // Comparisons -----------------------------------------------------------|

    /**
     * Create a new EQUALS specification for a Property.
     *
     * @param <T> Property type
     * @param property a Property
     * @param value its value
     *
     * @return a new EQUALS specification for a Property.
     */
    public static <T> EqPredicate<T> eq( Property<T> property, T value )
    {
        return new EqPredicate<>( property( property ), value );
    }

    /**
     * Create a new EQUALS specification for a Property using a named Variable.
     *
     * @param <T> Property type
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new EQUALS specification for a Property using a named Variable.
     */
    @SuppressWarnings( {"raw", "unchecked"} )
    public static <T> EqPredicate<T> eq( Property<T> property, Variable variable )
    {
        return new EqPredicate( property( property ), variable );
    }

    /**
     * Create a new EQUALS specification for an Association.
     *
     * @param <T> Association type
     * @param association an Association
     * @param value its value
     *
     * @return a new EQUALS specification for an Association.
     */
    public static <T> EqPredicate<Identity> eq( Association<T> association, T value )
    {
        return new EqPredicate<>(
                new PropertyFunction<>(
                        null,
                        association(association),
                        null,
                        null,
                        IDENTITY_METHOD),
                ((HasIdentity) value).identity().get());
    }

    /**
     * Create a new GREATER OR EQUALS specification for a Property.
     *
     * @param <T> Property type
     * @param property a Property
     * @param value its value
     *
     * @return a new GREATER OR EQUALS specification for a Property.
     */
    public static <T> GePredicate<T> ge( Property<T> property, T value )
    {
        return new GePredicate<>( property( property ), value );
    }

    /**
     * Create a new GREATER OR EQUALS specification for a Property using a named Variable.
     *
     * @param <T> Property type
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new GREATER OR EQUALS specification for a Property using a named Variable.
     */
    @SuppressWarnings( {"raw", "unchecked"} )
    public static <T> GePredicate<T> ge( Property<T> property, Variable variable )
    {
        return new GePredicate( property( property ), variable );
    }

    /**
     * Create a new GREATER THAN specification for a Property.
     *
     * @param <T> Property type
     * @param property a Property
     * @param value its value
     *
     * @return a new GREATER THAN specification for a Property.
     */
    public static <T> GtPredicate<T> gt( Property<T> property, T value )
    {
        return new GtPredicate<>( property( property ), value );
    }

    /**
     * Create a new GREATER THAN specification for a Property using a named Variable.
     *
     * @param <T> Property type
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new GREATER THAN specification for a Property using a named Variable.
     */
    @SuppressWarnings( {"raw", "unchecked"} )
    public static <T> GtPredicate<T> gt( Property<T> property, Variable variable )
    {
        return new GtPredicate( property( property ), variable );
    }

    /**
     * Create a new LESS OR EQUALS specification for a Property.
     *
     * @param <T> Property type
     * @param property a Property
     * @param value its value
     *
     * @return a new LESS OR EQUALS specification for a Property.
     */
    public static <T> LePredicate<T> le( Property<T> property, T value )
    {
        return new LePredicate<>( property( property ), value );
    }

    /**
     * Create a new LESS OR EQUALS specification for a Property using a named Variable.
     *
     * @param <T> Property type
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new LESS OR EQUALS specification for a Property using a named Variable.
     */
    @SuppressWarnings( {"raw", "unchecked"} )
    public static <T> LePredicate<T> le( Property<T> property, Variable variable )
    {
        return new LePredicate( property( property ), variable );
    }

    /**
     * Create a new LESSER THAN specification for a Property.
     *
     * @param <T> Property type
     * @param property a Property
     * @param value its value
     *
     * @return a new LESSER THAN specification for a Property.
     */
    public static <T> LtPredicate<T> lt( Property<T> property, T value )
    {
        return new LtPredicate<>( property( property ), value );
    }

    /**
     * Create a new LESSER THAN specification for a Property using a named Variable.
     *
     * @param <T> Property type
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new LESSER THAN specification for a Property using a named Variable.
     */
    @SuppressWarnings( {"raw", "unchecked"} )
    public static <T> LtPredicate<T> lt( Property<T> property, Variable variable )
    {
        return new LtPredicate( property( property ), variable );
    }

    /**
     * Create a new NOT EQUALS specification for a Property.
     *
     * @param <T> Property type
     * @param property a Property
     * @param value its value
     *
     * @return a new NOT EQUALS specification for a Property.
     */
    public static <T> NePredicate<T> ne( Property<T> property, T value )
    {
        return new NePredicate<>( property( property ), value );
    }

    /**
     * Create a new NOT EQUALS specification for a Property using a named Variable.
     *
     * @param <T> Property type
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new NOT EQUALS specification for a Property using a named Variable.
     */
    @SuppressWarnings( {"raw", "unchecked"} )
    public static <T> NePredicate<T> ne( Property<T> property, Variable variable )
    {
        return new NePredicate( property( property ), variable );
    }

    /**
     * Create a new REGULAR EXPRESSION specification for a Property.
     *
     * @param property a Property
     * @param regexp its value
     *
     * @return a new REGULAR EXPRESSION specification for a Property.
     */
    public static MatchesPredicate matches( Property<String> property, String regexp )
    {
        return new MatchesPredicate( property( property ), regexp );
    }

    /**
     * Create a new REGULAR EXPRESSION specification for a Property using a named Variable.
     *
     * @param property a Property
     * @param variable a Query Variable
     *
     * @return a new REGULAR EXPRESSION specification for a Property using a named Variable.
     */
    public static MatchesPredicate matches( Property<String> property, Variable variable )
    {
        return new MatchesPredicate( property( property ), variable );
    }

    // Null checks -----------------------------------------------------------|

    /**
     * Create a new NOT NULL specification for a Property.
     *
     * @param <T> Property type
     * @param property a Property
     *
     * @return a new NOT NULL specification for a Property.
     */
    public static <T> PropertyNotNullPredicate<T> isNotNull( Property<T> property )
    {
        return new PropertyNotNullPredicate<>( property( property ) );
    }

    /**
     * Create a new NULL specification for a Property.
     *
     * @param <T> Property type
     * @param property a Property
     *
     * @return a new NULL specification for a Property.
     */
    public static <T> PropertyNullPredicate<T> isNull( Property<T> property )
    {
        return new PropertyNullPredicate<>( property( property ) );
    }

    /**
     * Create a new NOT NULL specification for an Association.
     *
     * @param <T> Association type
     * @param association an Association
     *
     * @return a new NOT NULL specification for an Association.
     */
    public static <T> AssociationNotNullPredicate<T> isNotNull( Association<T> association )
    {
        return new AssociationNotNullPredicate<>( association( association ) );
    }

    /**
     * Create a new NULL specification for an Association.
     *
     * @param <T> Association type
     * @param association an Association
     *
     * @return a new NULL specification for an Association.
     */
    public static <T> AssociationNullPredicate<T> isNull( Association<T> association )
    {
        return new AssociationNullPredicate<>( association( association ) );
    }

    // Collections -----------------------------------------------------------|

    /**
     * Create a new CONTAINS ALL specification for a Collection Property.
     *
     * @param <T> Collection property type
     * @param collectionProperty a Collection Property
     * @param values its values
     *
     * @return a new CONTAINS ALL specification for a Collection Property.
     */
    public static <T> ContainsAllPredicate<T> containsAll( Property<? extends Collection<T>> collectionProperty,
                                                           Collection<T> values )
    {
        NullArgumentException.validateNotNull( "Values", values );
        return new ContainsAllPredicate<>( property( collectionProperty ), values );
    }

    /**
     * Create a new CONTAINS ALL specification for a Collection Property using named Variables.
     *
     * @param <T> Collection property type
     * @param collectionProperty a Collection Property
     * @param variables named Variables
     *
     * @return a new CONTAINS ALL specification for a Collection Property using named Variables.
     */
    @SuppressWarnings( {"raw", "unchecked"} )
    public static <T> ContainsAllPredicate<T> containsAllVariables(
        Property<? extends Collection<T>> collectionProperty,
        Collection<Variable> variables )
    {
        NullArgumentException.validateNotNull( "Variables", variables );
        return new ContainsAllPredicate( property( collectionProperty ), variables );
    }

    /**
     * Create a new CONTAINS specification for a Collection Property.
     *
     * @param <T> Collection property type
     * @param collectionProperty a Collection Property
     * @param value the value
     *
     * @return a new CONTAINS specification for a Collection Property.
     */
    public static <T> ContainsPredicate<T> contains( Property<? extends Collection<T>> collectionProperty,
                                                         T value )
    {
        NullArgumentException.validateNotNull( "Value", value );
        return new ContainsPredicate<>( property( collectionProperty ), value );
    }

    /**
     * Create a new CONTAINS specification for a Collection Property using named Variables.
     *
     * @param <T> Collection property type
     * @param collectionProperty a Collection Property
     * @param variable named Variable
     *
     * @return a new CONTAINS specification for a Collection Property using named Variables.
     */
    @SuppressWarnings( {"raw", "unchecked"} )
    public static <T> ContainsPredicate<T> contains( Property<? extends Collection<T>> collectionProperty,
                                                         Variable variable )
    {
        NullArgumentException.validateNotNull( "Variable", variable );
        return new ContainsPredicate( property( collectionProperty ), variable );
    }

    /**
     * Create a new CONTAINS specification for a ManyAssociation.
     *
     * @param <T> ManyAssociation type
     * @param manyAssoc  a ManyAssociation
     * @param value the value
     *
     * @return a new CONTAINS specification for a ManyAssociation.
     */
    public static <T> ManyAssociationContainsPredicate<T> contains( ManyAssociation<T> manyAssoc, T value )
    {
        return new ManyAssociationContainsPredicate<>( manyAssociation( manyAssoc ), value );
    }

    /**
     * Create a new CONTAINS specification for a NamedAssociation.
     *
     * @param <T> NamedAssociation type
     * @param namedAssoc  a NamedAssociation
     * @param value the value
     *
     * @return a new CONTAINS specification for a NamedAssociation.
     */
    public static <T> NamedAssociationContainsPredicate<T> contains( NamedAssociation<T> namedAssoc, T value )
    {
        return new NamedAssociationContainsPredicate<>( namedAssociation( namedAssoc ), value );
    }

    /**
     * Create a new CONTAINS NAME specification for a NamedAssociation.
     *
     * @param <T> NamedAssociation type
     * @param namedAssoc  a NamedAssociation
     * @param name the name
     *
     * @return a new CONTAINS NAME specification for a NamedAssociation.
     */
    public static <T> NamedAssociationContainsNamePredicate<T> containsName( NamedAssociation<T> namedAssoc,
                                                                                 String name )
    {
        return new NamedAssociationContainsNamePredicate<>( namedAssociation( namedAssoc ), name );
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
        private final PropertyFunction<?> compositeProperty;
        private final AssociationFunction<?> compositeAssociation;
        private final ManyAssociationFunction<?> compositeManyAssociation;
        private final NamedAssociationFunction<?> compositeNamedAssociation;

        private TemplateHandler( PropertyFunction<?> compositeProperty,
                                 AssociationFunction<?> compositeAssociation,
                                 ManyAssociationFunction<?> compositeManyAssociation,
                                 NamedAssociationFunction<?> compositeNamedAssociation
        )
        {
            this.compositeProperty = compositeProperty;
            this.compositeAssociation = compositeAssociation;
            this.compositeManyAssociation = compositeManyAssociation;
            this.compositeNamedAssociation = compositeNamedAssociation;
        }

        @Override
        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance(
                    method.getReturnType().getClassLoader(),
                    array( method.getReturnType() ),
                    new PropertyReferenceHandler<>( new PropertyFunction<T>( compositeProperty,
                                                                             compositeAssociation,
                                                                             compositeManyAssociation,
                                                                             compositeNamedAssociation,
                                                                             method ) ) );
            }
            else if( Association.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance(
                    method.getReturnType().getClassLoader(),
                    array( method.getReturnType() ),
                    new AssociationReferenceHandler<>( new AssociationFunction<T>( compositeAssociation,
                                                                                   compositeManyAssociation,
                                                                                   compositeNamedAssociation,
                                                                                   method ) ) );
            }
            else if( ManyAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance(
                    method.getReturnType().getClassLoader(),
                    array( method.getReturnType() ),
                    new ManyAssociationReferenceHandler<>( new ManyAssociationFunction<T>( compositeAssociation,
                                                                                           compositeManyAssociation,
                                                                                           compositeNamedAssociation,
                                                                                           method ) ) );
            }
            else if( NamedAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance(
                    method.getReturnType().getClassLoader(),
                    array( method.getReturnType() ),
                    new NamedAssociationReferenceHandler<>( new NamedAssociationFunction<T>( compositeAssociation,
                                                                                             compositeManyAssociation,
                                                                                             compositeNamedAssociation,
                                                                                             method ) ) );
            }

            return null;
        }
    }

    private static class PropertyReferenceHandler<T>
        implements InvocationHandler
    {
        private final PropertyFunction<T> property;

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
                                                   array( (Class<?>) propertyType, PropertyReference.class ),
                                                   new TemplateHandler<T>( property, null, null, null ) );
                }
            }

            return null;
        }
    }

    private static class AssociationReferenceHandler<T>
        implements InvocationHandler
    {
        private final AssociationFunction<T> association;

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
                                                   array( (Class) associationType, PropertyReference.class ),
                                                   new TemplateHandler<T>( null, association, null, null ) );
                }
            }

            return null;
        }
    }

    private static class ManyAssociationReferenceHandler<T>
        implements InvocationHandler
    {
        private final ManyAssociationFunction<T> manyAssociation;

        private ManyAssociationReferenceHandler( ManyAssociationFunction<T> manyAssociation )
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
                                                   array( (Class) manyAssociationType, PropertyReference.class ),
                                                   new TemplateHandler<T>( null, null, manyAssociation, null ) );
                }
            }

            return null;
        }
    }

    private static class NamedAssociationReferenceHandler<T>
        implements InvocationHandler
    {
        private final NamedAssociationFunction<T> namedAssociation;

        private NamedAssociationReferenceHandler( NamedAssociationFunction<T> namedAssociation )
        {
            this.namedAssociation = namedAssociation;
        }

        public NamedAssociationFunction<T> namedAssociation()
        {
            return namedAssociation;
        }

        @Override
        public Object invoke( Object o, final Method method, Object[] objects )
            throws Throwable
        {
            if( method.equals( NamedAssociation.class.getMethod( "get", String.class ) ) )
            {
                Type namedAssociationType = GenericAssociationInfo.associationTypeOf( namedAssociation.accessor() );
                if( namedAssociationType.getClass().equals( Class.class ) )
                {
                    return Proxy.newProxyInstance( method.getDeclaringClass().getClassLoader(),
                                                   array( (Class) namedAssociationType, PropertyReference.class ),
                                                   new TemplateHandler<T>( null, null, null, namedAssociation ) );
                }
            }

            return null;
        }
    }

    @SafeVarargs
    private static <T> T[] array( T... array )
    {
        return array;
    }

    private QueryExpressions()
    {
    }
}
