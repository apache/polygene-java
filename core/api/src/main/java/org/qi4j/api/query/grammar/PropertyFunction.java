package org.qi4j.api.query.grammar;

import org.qi4j.api.association.Association;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.NotQueryableException;
import org.qi4j.api.query.QueryExpressionException;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Function;

import java.lang.reflect.*;

/**
* TODO
*/
public class PropertyFunction<T>
        implements Function<Composite, Property<T>>
{
    private PropertyFunction traversedProperty;
    private AssociationFunction traversedAssociation;
    private ManyAssociationFunction<?> traversedManyAssociation;
    private final AccessibleObject accessor;

    public PropertyFunction( PropertyFunction traversedProperty, AssociationFunction traversedAssociation, ManyAssociationFunction<?> traversedManyAssociation, AccessibleObject accessor )
    {
        this.traversedProperty = traversedProperty;
        this.traversedAssociation = traversedAssociation;
        this.traversedManyAssociation = traversedManyAssociation;
        this.accessor = accessor;

        // Verify that the property accessor is not marked as non queryable
        NotQueryableException.throwIfNotQueryable( accessor );
        // Verify that the property type itself (value composites) is not marked as non queryable

        Type returnType = Classes.TYPE_OF.map( accessor );
        if( !Property.class.isAssignableFrom( Classes.RAW_CLASS.map( returnType ) ) )
        {
            throw new QueryExpressionException( "Not a property type:" + returnType );
        }
        Type propertyTypeAsType = GenericPropertyInfo.getPropertyType( returnType );
        if( propertyTypeAsType instanceof ParameterizedType )
        {
            propertyTypeAsType = ( (ParameterizedType) propertyTypeAsType ).getRawType();
        }

        if( !( propertyTypeAsType instanceof Class ) )
        {
            throw new QueryExpressionException( "Unsupported property type:" + propertyTypeAsType );
        }
        Class type = (Class<T>) propertyTypeAsType;
        NotQueryableException.throwIfNotQueryable( type );
    }

    public PropertyFunction getTraversedProperty()
    {
        return traversedProperty;
    }

    public AssociationFunction getTraversedAssociation()
    {
        return traversedAssociation;
    }

    public ManyAssociationFunction getTraversedManyAssociation()
    {
        return traversedManyAssociation;
    }

    public AccessibleObject getAccessor()
    {
        return accessor;
    }

    @Override
    public Property<T> map( Composite entity )
    {
        try
        {
            Object target = entity;
            if (traversedProperty != null)
            {
                Property<?> property = traversedProperty.map( entity );
                if (property == null)
                    return null;
                target = property.get();
            }
            else if (traversedAssociation != null)
            {
                Association<?> association = traversedAssociation.map( entity );
                if (association == null)
                    return null;
                target = association.get();
            }
            else if (traversedManyAssociation != null)
                throw new IllegalArgumentException( "Cannot evaluate a ManyAssociation" );

            if (target == null)
                return null;

            CompositeInstance handler = (CompositeInstance) Proxy.getInvocationHandler( target );
            return handler.state().propertyFor( accessor );
        } catch( IllegalArgumentException e )
        {
            throw e;
        }catch( Throwable e )
        {
            throw new IllegalArgumentException( e );
        }
    }

    @Override
    public String toString()
    {
        if (traversedProperty != null)
            return traversedProperty.toString()+"."+ ((Member)accessor).getName();
        else if (traversedAssociation != null)
            return traversedAssociation.toString()+"."+ ((Member)accessor).getName();
        else
            return ((Member)accessor).getName();
    }
}
