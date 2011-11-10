/*
 * Copyright 2007 Rickard Öberg.
 * Copyright 2007 Niclas Hedhman.
 * Copyright 2008 Alin Dreghiciu.
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

import org.qi4j.api.association.Association;
import org.qi4j.api.association.GenericAssociationInfo;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.grammar.*;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.functional.Specification;

import java.lang.reflect.*;
import java.util.Collection;

import static org.qi4j.functional.Iterables.iterable;
import static org.qi4j.functional.Iterables.prepend;

/**
 * Static factory methods for query expressions and operators.
 */
public final class QueryExpressions
{
    // This is used for eq(Association,Composite)
    private static Method idComposite;

    static
    {
        try
        {
            idComposite = Identity.class.getMethod( "identity" );
        }
        catch( NoSuchMethodException e )
        {
            e.printStackTrace();
        }
    }

    // Templates and variables
    public static <T> T templateFor( Class<T> clazz )
    {
        NullArgumentException.validateNotNull( "Template class", clazz );

        if (clazz.isInterface())
            return clazz.cast( Proxy.newProxyInstance( clazz.getClassLoader(), new Class[]{clazz}, new TemplateHandler<Object>( null, null, null ) ) );
        else
        {
            try
            {
                T mixin = clazz.newInstance();
                for( Field field : clazz.getFields() )
                {
                    if (field.getAnnotation( State.class ) != null)
                    {
                        if (field.getType().equals( Property.class ))
                        {
                            field.set(mixin, Proxy.newProxyInstance( field.getType().getClassLoader(),
                                    new Class[]{field.getType()},
                                    new PropertyReferenceHandler( new PropertyFunction( null, null, null, field ) ) ));
                        }
                        else if (field.getType().equals( Association.class ))
                        {
                            field.set(mixin, Proxy.newProxyInstance( field.getType().getClassLoader(),
                                    new Class[]{field.getType()},
                                    new AssociationReferenceHandler( new AssociationFunction( null, null, field ) ) ));
                        }
                        else if (field.getType().equals( Property.class ))
                        {
                            field.set(mixin, Proxy.newProxyInstance( field.getType().getClassLoader(),
                                    new Class[]{field.getType()},
                                    new ManyAssociationReferenceHandler( new ManyAssociationFunction( null, null, field ) ) ));
                        }
                    }
                }
                return mixin;
            } catch( Throwable e )
            {
                throw new IllegalArgumentException( "Cannot use class as template", e );
            }
        }
    }

    public static <T> T templateFor( final Class<T> mixinType, Association<?> association )
    {
        NullArgumentException.validateNotNull( "Mixin class", mixinType );
        NullArgumentException.validateNotNull( "Association", association );
        return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{mixinType}, new TemplateHandler<Object>( null, association( association ), null ) ) );
    }

    public static <T> T oneOf( final ManyAssociation<T> association )
    {
        NullArgumentException.validateNotNull( "Association", association );
        return association.get( 0 );
    }

    public static <T> T variable(String name)
    {
        NullArgumentException.validateNotNull( "Variable name", name );
        return (T) new Variable( name );
    }

    public static <T> PropertyFunction<T> property( Property<T> property )
    {
        return (PropertyFunction<T>) ((PropertyReferenceHandler<T>)Proxy.getInvocationHandler( property )).getProperty();
    }

    public static <T> Property<T> property(Class mixinClass, String fieldName)
    {
        try
        {
            Field field = mixinClass.getField( fieldName );

            if (!Property.class.isAssignableFrom( field.getType()))
                throw new IllegalArgumentException( "Field must be of type Property<?>" );

            return (Property<T>) Proxy.newProxyInstance( mixinClass.getClassLoader(), new Class[]{field.getType()}, new PropertyReferenceHandler<T>(new PropertyFunction<T>(null, null, null, field )) );
        } catch( NoSuchFieldException e )
        {
            throw new IllegalArgumentException( "No such field '"+fieldName+"' in mixin "+mixinClass.getName() );
        }
    }

    public static <T> AssociationFunction<T> association( Association<T> association )
    {
        return (AssociationFunction<T>) ((AssociationReferenceHandler<T>)Proxy.getInvocationHandler( association )).getAssociation();
    }

    public static <T> ManyAssociationFunction<T> manyAssociation( ManyAssociation<T> association )
    {
        return (ManyAssociationFunction<T>) ((ManyAssociationReferenceHandler<T>)Proxy.getInvocationHandler( association )).getManyAssociation();
    }

    // And/Or/Not
    public static AndSpecification and(Specification<Composite> left, Specification<Composite> right, Specification<Composite>... optionalRight)
    {
        return new AndSpecification(prepend( left, prepend( right, iterable( optionalRight ) ) ));
    }

    public static OrSpecification or(Specification<Composite> left, Specification<Composite> right, Specification<Composite>... optionalRight)
    {
        return new OrSpecification(prepend( left, prepend( right, iterable( optionalRight ) ) ));
    }

    public static NotSpecification not(Specification<Composite> operand)
    {
        return new NotSpecification(operand);
    }

    // Comparisons
    public static <T> EqSpecification<T> eq( Property<T> property, T value )
    {
        return new EqSpecification<T>(  property( property ), value );
    }

    public static <T> EqSpecification<String> eq( Association<T> association, T value )
    {
        return new EqSpecification<String>(  new PropertyFunction<String>(null, association( association ), null, idComposite), value.toString() );
    }

    public static <T> GeSpecification<T> ge( Property<T> property, T value )
    {
        return new GeSpecification<T>(  property( property ), value );
    }

    public static <T> GtSpecification<T> gt( Property<T> property, T value )
    {
        return new GtSpecification<T>(  property( property ), value );
    }

    public static <T> LeSpecification<T> le( Property<T> property, T value )
    {
        return new LeSpecification<T>(  property( property ), value );
    }

    public static <T> LtSpecification<T> lt( Property<T> property, T value )
    {
        return new LtSpecification<T>(  property( property ), value );
    }

    public static <T> NeSpecification<T> ne( Property<T> property, T value )
    {
        return new NeSpecification<T>(  property( property ), value );
    }

    public static MatchesSpecification matches(Property<String> property, String regexp)
    {
        return new MatchesSpecification(property(property), regexp);
    }

    // Null checks
    public static <T> PropertyNotNullSpecification<T> isNotNull( Property<T> property)
    {
        return new PropertyNotNullSpecification<T>(  property( property ));
    }

    public static <T> PropertyNullSpecification<T> isNull( Property<T> property)
    {
        return new PropertyNullSpecification<T>(  property( property ));
    }

    public static <T> AssociationNotNullSpecification<T> isNotNull( Association<T> association)
    {
        return new AssociationNotNullSpecification<T>(  association( association ));
    }

    public static <T> AssociationNullSpecification<T> isNull( Association<T> association)
    {
        return new AssociationNullSpecification<T>(  association( association ));
    }

    // Collections
    public static <T> ContainsAllSpecification<T> containsAll( Property<? extends Collection<T>> collectionProperty, Iterable<T> values )
    {
        NullArgumentException.validateNotNull( "Values", values );
        return new ContainsAllSpecification<T>(property( collectionProperty ), values);
    }

    public static <T> ContainsSpecification<T> contains( Property<? extends Collection<T>> collectionProperty, T value )
    {
        NullArgumentException.validateNotNull( "Value", value );
        return new ContainsSpecification<T>(property( collectionProperty ), value);
    }

    public static <T> ManyAssociationContainsSpecification<T> contains( ManyAssociation<T> manyAssoc, T value )
    {
        return new ManyAssociationContainsSpecification<T>(manyAssociation( manyAssoc ), value);
    }

    // Ordering
    public static <T> OrderBy orderBy( final Property<T> property )
    {
        return orderBy( property, OrderBy.Order.ASCENDING );
    }

    public static <T> OrderBy orderBy( final Property<T> property, final OrderBy.Order order )
    {
        return new OrderBy(property( property ), order );
    }

    public static class TemplateHandler<T>
            implements InvocationHandler
    {
        private PropertyFunction<?> CompositeProperty;
        private AssociationFunction<?> CompositeAssociation;
        private ManyAssociationFunction<?> CompositeManyAssociation;

        private TemplateHandler(PropertyFunction<?> CompositeProperty, AssociationFunction<?> CompositeAssociation, ManyAssociationFunction<?> CompositeManyAssociation)
        {
            this.CompositeProperty = CompositeProperty;
            this.CompositeAssociation = CompositeAssociation;
            this.CompositeManyAssociation = CompositeManyAssociation;
        }

        @Override
        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance( method.getReturnType().getClassLoader(), new Class[]{method.getReturnType()}, new PropertyReferenceHandler( new PropertyFunction( CompositeProperty, CompositeAssociation, CompositeManyAssociation, method ) ) );
            }
            else if( Association.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance( method.getReturnType().getClassLoader(), new Class[]{method.getReturnType()}, new AssociationReferenceHandler( new AssociationFunction( CompositeAssociation, CompositeManyAssociation, method ) ) );
            }
            else if( ManyAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance( method.getReturnType().getClassLoader(), new Class[]{method.getReturnType()}, new ManyAssociationReferenceHandler( new ManyAssociationFunction( CompositeAssociation, CompositeManyAssociation, method ) ) );
            }

            return null;
        }
    }

    private static class PropertyReferenceHandler<T>
            implements InvocationHandler
    {
        private PropertyFunction<?> property;

        public PropertyReferenceHandler( PropertyFunction<?> property )
        {
            this.property = property;
        }

        public PropertyFunction<?> getProperty()
        {
            return property;
        }

        @Override
        public Object invoke( Object o, final Method method, Object[] objects ) throws Throwable
        {
            if( method.equals( Property.class.getMethod( "get" ) ) )
            {
                Type propertyType = GenericPropertyInfo.getPropertyType( property.getAccessor() );
                if( propertyType.getClass().equals( Class.class ) )
                    return Proxy.newProxyInstance( method.getDeclaringClass().getClassLoader(),
                            new Class[]{(Class) propertyType, PropertyReference.class},
                            new TemplateHandler( property, null, null ));
            }

            return null;
        }
    }

    private static class AssociationReferenceHandler<T>
            implements InvocationHandler
    {
        private AssociationFunction<?> association;

        public AssociationReferenceHandler( AssociationFunction<?> association )
        {
            this.association = association;
        }

        public AssociationFunction<?> getAssociation()
        {
            return association;
        }

        @Override
        public Object invoke( Object o, final Method method, Object[] objects ) throws Throwable
        {
            if( method.equals( Association.class.getMethod( "get" ) ) )
            {
                Type associationType = GenericAssociationInfo.getAssociationType( association.getAccessor() );
                if( associationType.getClass().equals( Class.class ) )
                    return Proxy.newProxyInstance( method.getDeclaringClass().getClassLoader(),
                            new Class[]{(Class) associationType, PropertyReference.class},
                            new TemplateHandler( null, association, null ));
            }

            return null;
        }
    }

    private static class ManyAssociationReferenceHandler<T>
            implements InvocationHandler
    {
        private ManyAssociationFunction<?> manyAssociation;

        public ManyAssociationReferenceHandler( ManyAssociationFunction<?> manyAssociation )
        {
            this.manyAssociation = manyAssociation;
        }

        public ManyAssociationFunction<?> getManyAssociation()
        {
            return manyAssociation;
        }

        @Override
        public Object invoke( Object o, final Method method, Object[] objects ) throws Throwable
        {
            if( method.equals( ManyAssociation.class.getMethod( "get", Integer.TYPE ) ) )
            {
                Type manyAssociationType = GenericAssociationInfo.getAssociationType( manyAssociation.getAccessor() );
                if( manyAssociationType.getClass().equals( Class.class ) )
                    return Proxy.newProxyInstance( method.getDeclaringClass().getClassLoader(),
                            new Class[]{(Class) manyAssociationType, PropertyReference.class},
                            new TemplateHandler( null, null, manyAssociation ));
            }

            return null;
        }
    }
}