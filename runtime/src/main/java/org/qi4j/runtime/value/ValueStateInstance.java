package org.qi4j.runtime.value;

import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.property.Property;
import org.qi4j.runtime.association.AssociationInstance;
import org.qi4j.runtime.association.ManyAssociationInstance;
import org.qi4j.runtime.property.PropertyInstance;

import java.lang.reflect.AccessibleObject;
import java.util.Map;

/**
* TODO
*/
public final class ValueStateInstance
    implements AssociationStateHolder
{
    protected Map<AccessibleObject, PropertyInstance<?>> properties;
    protected Map<AccessibleObject, AssociationInstance<?>> associations;
    protected Map<AccessibleObject, ManyAssociationInstance<?>> manyAssociations;

    public ValueStateInstance( Map<AccessibleObject, PropertyInstance<?>> properties,
                               Map<AccessibleObject, AssociationInstance<?>> associations,
                               Map<AccessibleObject, ManyAssociationInstance<?>> manyAssociations
    )
    {
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
    }

    public <T> PropertyInstance<T> propertyFor( AccessibleObject accessor )
            throws IllegalArgumentException
    {
        PropertyInstance<T> property = (PropertyInstance<T>) properties.get( accessor );

        if (property == null)
            throw new IllegalArgumentException( "No such property:"+accessor );

        return property;
    }

    @Override
    public Iterable<PropertyInstance<?>> properties()
    {
        return properties.values();
    }

    public <T> AssociationInstance<T> associationFor( AccessibleObject accessor )
    {
        AssociationInstance<T> association = (AssociationInstance<T>) associations.get( accessor );

        if (association == null)
            throw new IllegalArgumentException( "No such association:"+accessor );

        return association;
    }

    @Override
    public Iterable<AssociationInstance<?>> associations()
    {
        return associations.values();
    }

    public <T> ManyAssociationInstance<T> manyAssociationFor( AccessibleObject accessor )
    {
        ManyAssociationInstance<T> manyAssociation = (ManyAssociationInstance<T>) manyAssociations.get( accessor );

        if (manyAssociation == null)
            throw new IllegalArgumentException( "No such many-association:"+accessor );

        return manyAssociation;
    }

    @Override
    public Iterable<ManyAssociationInstance<?>> manyAssociations()
    {
        return manyAssociations.values();
    }

    @Override
    public boolean equals( Object obj )
    {
        ValueStateInstance state = (ValueStateInstance) obj;
        if (!properties.equals( state.properties ))
            return false;
        if (!associations.equals( state.associations ))
            return false;
        if (!manyAssociations.equals( state.manyAssociations ))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = properties.hashCode();
        result = 31 * result + associations.hashCode();
        result = 31 * result + manyAssociations.hashCode();
        return result;
    }
}
