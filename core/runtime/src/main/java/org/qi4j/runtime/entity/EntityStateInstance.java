package org.qi4j.runtime.entity;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Function;
import org.qi4j.functional.Function2;
import org.qi4j.functional.Iterables;
import org.qi4j.runtime.association.AssociationInstance;
import org.qi4j.runtime.association.AssociationModel;
import org.qi4j.runtime.association.ManyAssociationInstance;
import org.qi4j.runtime.association.ManyAssociationModel;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.unitofwork.BuilderEntityState;
import org.qi4j.spi.entity.EntityState;

/**
 * TODO
 */
public final class EntityStateInstance
    implements AssociationStateHolder
{
    protected Map<AccessibleObject, Object> state;

    private final EntityStateModel stateModel;
    private EntityState entityState;
    protected Function2<EntityReference, Type, Object> entityFunction;

    public EntityStateInstance( EntityStateModel stateModel, final UnitOfWork uow, EntityState entityState
    )
    {
        this.stateModel = stateModel;
        this.entityState = entityState;

        entityFunction = new Function2<EntityReference, Type, Object>()
        {
            @Override
            public Object map( EntityReference entityReference, Type type )
            {
                return uow.get( Classes.RAW_CLASS.map( type ), entityReference.identity() );
            }
        };
    }

    @Override
    public <T> Property<T> propertyFor( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        Map<AccessibleObject, Object> state = state();

        Property<T> property = (Property<T>) state.get( accessor );

        if( property == null )
        {
            PropertyModel entityPropertyModel = stateModel.propertyModelFor( accessor );
            if( entityPropertyModel == null )
            {
                throw new IllegalArgumentException( "No such property:" + accessor );
            }

            property = new EntityPropertyInstance<T>( entityState instanceof BuilderEntityState ? entityPropertyModel.getBuilderInfo() : entityPropertyModel, entityState );
            state.put( accessor, property );
        }

        return property;
    }

    @Override
    public Iterable<Property<?>> properties()
    {
        return Iterables.map( new Function<PropertyDescriptor, Property<?>>()
        {
            @Override
            public Property<?> map( PropertyDescriptor propertyDescriptor )
            {
                return propertyFor( propertyDescriptor.accessor() );
            }
        }, stateModel.properties() );
    }

    @Override
    public <T> Association<T> associationFor( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        Map<AccessibleObject, Object> state = state();
        Association<T> association = (Association<T>) state.get( accessor );

        if( association == null )
        {
            final AssociationModel associationModel = stateModel.getAssociation( accessor );
            if( associationModel == null )
            {
                throw new IllegalArgumentException( "No such association:" + accessor );
            }

            association = new AssociationInstance<T>( entityState instanceof BuilderEntityState ? associationModel.getBuilderInfo() : associationModel, entityFunction, new Property<EntityReference>()
            {
                @Override
                public EntityReference get()
                {
                    return entityState.associationValueOf( associationModel.qualifiedName() );
                }

                @Override
                public void set( EntityReference newValue )
                    throws IllegalArgumentException, IllegalStateException
                {
                    entityState.setAssociationValue( associationModel.qualifiedName(), newValue );
                }
            } );
            state.put( accessor, association );
        }

        return association;
    }

    @Override
    public Iterable<Association<?>> allAssociations()
    {
        return Iterables.map( new Function<AssociationDescriptor, Association<?>>()
        {
            @Override
            public Association<?> map( AssociationDescriptor associationDescriptor )
            {
                return associationFor( associationDescriptor.accessor() );
            }
        }, stateModel.associations() );
    }

    @Override
    public <T> ManyAssociation<T> manyAssociationFor( AccessibleObject accessor )
    {
        Map<AccessibleObject, Object> state = state();

        ManyAssociation<T> manyAssociation = (ManyAssociation<T>) state.get( accessor );

        if( manyAssociation == null )
        {
            final ManyAssociationModel associationModel = stateModel.getManyAssociation( accessor );
            if( associationModel == null )
            {
                throw new IllegalArgumentException( "No such many-association:" + accessor );
            }

            manyAssociation = new ManyAssociationInstance<T>( entityState instanceof BuilderEntityState ? associationModel
                .getBuilderInfo() : associationModel, entityFunction, entityState.manyAssociationValueOf( associationModel.qualifiedName() ) );
            state.put( accessor, manyAssociation );
        }

        return manyAssociation;
    }

    @Override
    public Iterable<ManyAssociation<?>> allManyAssociations()
    {
        return Iterables.map( new Function<AssociationDescriptor, ManyAssociation<?>>()
        {
            @Override
            public ManyAssociation<?> map( AssociationDescriptor associationDescriptor )
            {
                return manyAssociationFor( associationDescriptor.accessor() );
            }
        }, stateModel.manyAssociations() );
    }

    public void checkConstraints()
    {
        for( PropertyDescriptor propertyDescriptor : stateModel.properties() )
        {
            ( (ConstraintsCheck) propertyDescriptor ).checkConstraints( this.<Object>propertyFor( propertyDescriptor.accessor() )
                                                                            .get() );
        }

        for( AssociationDescriptor associationDescriptor : stateModel.associations() )
        {
            ( (ConstraintsCheck) associationDescriptor ).checkConstraints( this.<Object>associationFor( associationDescriptor
                                                                                                            .accessor() )
                                                                               .get() );
        }

        // TODO Should ManyAssociations be checked too?
    }

    private Map<AccessibleObject, Object> state()
    {
        if( state == null )
        {
            state = new HashMap<AccessibleObject, Object>();
        }

        return state;
    }
}
