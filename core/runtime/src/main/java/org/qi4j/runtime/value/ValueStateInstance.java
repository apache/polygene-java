package org.qi4j.runtime.value;

import java.lang.reflect.AccessibleObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.runtime.association.AssociationInfo;
import org.qi4j.runtime.association.AssociationInstance;
import org.qi4j.runtime.association.AssociationModel;
import org.qi4j.runtime.association.ManyAssociationInstance;
import org.qi4j.runtime.association.ManyAssociationModel;
import org.qi4j.runtime.property.PropertyInfo;
import org.qi4j.runtime.property.PropertyInstance;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.structure.ModelModule;
import org.qi4j.runtime.structure.ModuleInstance;

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

    public ValueStateInstance( ModelModule<ValueModel> compositeModelModule,
                                                            ModuleInstance currentModule,
                                                            ValueStateModel.StateResolver stateResolver )
    {
        ValueModel valueModel = compositeModelModule.model();
        this.properties = new LinkedHashMap<AccessibleObject, PropertyInstance<?>>();
        for( PropertyDescriptor propertyDescriptor : valueModel.state().properties() )
        {
            PropertyInfo builderInfo = ( (PropertyModel) propertyDescriptor ).getBuilderInfo();
            Object value = stateResolver.getPropertyState( propertyDescriptor );
            PropertyInstance<Object> propertyInstance = new PropertyInstance<Object>( builderInfo, value );
            properties.put( propertyDescriptor.accessor(), propertyInstance );
        }

        this.associations = new LinkedHashMap<AccessibleObject, AssociationInstance<?>>();
        for( AssociationDescriptor associationDescriptor : valueModel.state().associations() )
        {
            AssociationInfo builderInfo = ( (AssociationModel) associationDescriptor ).getBuilderInfo();
            EntityReference value = stateResolver.getAssociationState( associationDescriptor );
            AssociationInstance<Object> associationInstance1 =
                new AssociationInstance<Object>( builderInfo, currentModule.getEntityFunction(), new ReferenceProperty( value ) );
            associations.put( associationDescriptor.accessor(), associationInstance1 );
        }

        this.manyAssociations = new LinkedHashMap<AccessibleObject, ManyAssociationInstance<?>>();
        for( AssociationDescriptor associationDescriptor : valueModel.state().manyAssociations() )
        {
            AssociationInfo builderInfo = ( (ManyAssociationModel) associationDescriptor ).getBuilderInfo();
            List<EntityReference> value = stateResolver.getManyAssociationState( associationDescriptor );
            ManyAssociationValueState manyAssociationState = new ManyAssociationValueState( value );
            ManyAssociationInstance<Object> associationInstance = new ManyAssociationInstance<Object>( builderInfo, currentModule.getEntityFunction(),
                                                                                                       manyAssociationState );
            manyAssociations.put( associationDescriptor.accessor(), associationInstance );
        }
    }

    @Override
    public <T> PropertyInstance<T> propertyFor( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        PropertyInstance<T> property = (PropertyInstance<T>) properties.get( accessor );

        if( property == null )
        {
            throw new IllegalArgumentException( "No such property:" + accessor );
        }

        return property;
    }

    @Override
    public Iterable<PropertyInstance<?>> properties()
    {
        return properties.values();
    }

    @Override
    public <T> AssociationInstance<T> associationFor( AccessibleObject accessor )
    {
        AssociationInstance<T> association = (AssociationInstance<T>) associations.get( accessor );

        if( association == null )
        {
            throw new IllegalArgumentException( "No such association:" + accessor );
        }

        return association;
    }

    @Override
    public Iterable<AssociationInstance<?>> allAssociations()
    {
        return associations.values();
    }

    @Override
    public <T> ManyAssociationInstance<T> manyAssociationFor( AccessibleObject accessor )
    {
        ManyAssociationInstance<T> manyAssociation = (ManyAssociationInstance<T>) manyAssociations.get( accessor );

        if( manyAssociation == null )
        {
            throw new IllegalArgumentException( "No such many-association:" + accessor );
        }

        return manyAssociation;
    }

    @Override
    public Iterable<ManyAssociationInstance<?>> allManyAssociations()
    {
        return manyAssociations.values();
    }

    @Override
    public boolean equals( Object obj )
    {
        ValueStateInstance state = (ValueStateInstance) obj;
        if( !properties.equals( state.properties ) )
        {
            return false;
        }
        if( !associations.equals( state.associations ) )
        {
            return false;
        }
        if( !manyAssociations.equals( state.manyAssociations ) )
        {
            return false;
        }

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
