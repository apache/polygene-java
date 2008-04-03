/*  Copyright 2008 Edward Yakop.
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
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.ibatis.internal;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.qi4j.association.AbstractAssociation;
import static org.qi4j.composite.NullArgumentException.*;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.ibatis.internal.common.Status;
import static org.qi4j.entity.ibatis.internal.common.Status.*;
import org.qi4j.entity.ibatis.internal.property.MutablePropertyInstance;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.entity.EntityStateInstance;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.serialization.EntityId;

/**
 * {@code IBatisEntityState} represents {@code IBatis} version of {@link org.qi4j.spi.entity.EntityState}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class IBatisEntityState
    extends EntityStateInstance
{
    private final Map<String, Object> values;
    private Status status;
    private final IBatisEntityStateDao dao;
    private final UnitOfWork unitOfWork;

    /**
     * Construct an instance of {@code IBatisEntityState}.
     *
     * @param anIdentity        The identity of the composite that this {@code IBatisEntityState} represents.
     *                          This argument must not be {@code null}.
     * @param aCompositeBinding The composite binding. This argument must not be {@code null}.
     * @param valuez            The field valuez of this entity state. This argument must not be {@code null}.
     * @param aStatus           The initial entity state status. This argument must not be {@code null}.
     * @param anUnitOfWork      The unit of work. This argument must not be {@code null}.
     * @param aDao              The dao to retrieve associations and complete this entity state.
     *                          This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if one or some or all arguments are {@code null}.
     * @since 0.1.0
     */
    public IBatisEntityState(
        EntityId anIdentity, CompositeBinding aCompositeBinding, Map<String, Object> valuez,
        EntityStatus status,
        Status aStatus, UnitOfWork anUnitOfWork, IBatisEntityStateDao aDao )
        throws IllegalArgumentException
    {
        super( 0, anIdentity, status, new HashMap<String, Object>(), new HashMap<String, EntityId>(), new HashMap<String, Collection<EntityId>>() );

        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "aCompositeBinding", aCompositeBinding );
        validateNotNull( "valuez", valuez );
        validateNotNull( "aDao", aDao );

        this.values = valuez;
        this.status = aStatus;
        unitOfWork = anUnitOfWork;
        dao = aDao;

        capitalizeKeys();
    }

    /**
     * Capitalize keys of the values. This is needed to ensure that regardless the backing database it will return
     * the right property names.
     *
     * @since 0.1.0
     */
    private void capitalizeKeys()
    {
        Set<String> keys = values.keySet();
        String[] keysArray = keys.toArray( new String[keys.size()] );
        for( String key : keysArray )
        {
            Object value = values.remove( key );
            String capitalizeKey = key.toUpperCase();
            values.put( capitalizeKey, value );
        }
    }

    /**
     * Returns the property for the specified {@code propertyMethod}.
     *
     * @param aPropertyMethod The property method. This argument must not be {@code null}.
     * @return The property instance given the property method. This argument must not be {@code null}.
     * @since 0.1.0
     */
    public final Property getProperty( Method aPropertyMethod )
    {
/*
        Property propertyInstance = properties.get( aPropertyMethod );

        if( propertyInstance == null )
        {
            CompositeMethodBinding compositeMethodBinding = getCompositeBinding().getCompositeMethodBinding( aPropertyMethod );
            PropertyBinding propertyBinding = compositeMethodBinding.getPropertyBinding();
            Boolean useDefaultValue = ( status == statusNew ) || ( status == statusNewToDeleted );
            Object value = computePropertyValue( propertyBinding, values, useDefaultValue );
            propertyInstance = newPropertyInstance( propertyBinding, value );

            properties.put( aPropertyMethod, propertyInstance );
        }

        return propertyInstance;
*/
        return null;
    }

    /**
     * Compute the property value. Returns the default value if the argument {@code isUseDefaultValue} is sets to
     * {@code true} and the property value does not exists in {@code propertyValues} argument.
     *
     * @param propertyBinding   The binding to use to look up default value.
     *                          This argument must not be {@code null} if {@code isUseDefaultValue} value
     *                          is {@code true}.
     * @param propertyValues    The property values. The key is property name (not qualified name).
     *                          This argument must not be {@code null}.
     * @param isUseDefaultValue Sets to {@code true} to use default value, {@code false} otherwise.
     * @return The computed property value.
     * @throws IllegalStateException Thrown if debug mode is <b>on</b> and there is a mismatch between actual and
     *                               expected value type.
     * @since 0.1.0
     */
    final Object computePropertyValue(
        PropertyBinding propertyBinding, Map<String, Object> propertyValues,
        boolean isUseDefaultValue )
        throws IllegalStateException
    {
        Object value = null;

        String propertyName = propertyBinding.getName().toUpperCase();
        if( propertyValues.containsKey( propertyName ) )
        {
            // TODO: Handle mapping of compound property?
            value = propertyValues.get( propertyName );
        }
        else if( isUseDefaultValue )
        {
            value = propertyBinding.getDefaultValue();
        }

        return value;
    }

    /**
     * Construct a new instance of {@link Property}. Must not return {@code null}.
     *
     * @param aPropertyBinding The property binding. This argument must not be {@code null}.
     * @param aPropertyValue   The property value.
     * @return A new property instance.
     * @since 0.1.0
     */
    final Property<Object> newPropertyInstance( PropertyBinding aPropertyBinding, Object aPropertyValue )
    {
        PropertyResolution propertyResolution = aPropertyBinding.getPropertyResolution();
        PropertyModel propertyModel = propertyResolution.getPropertyModel();
        Method accessor = propertyModel.getAccessor();
        Class<?> type = accessor.getReturnType();

        if( ImmutableProperty.class.isAssignableFrom( type ) )
        {
            return new ImmutablePropertyInstance<Object>( aPropertyBinding, aPropertyValue );
        }
        else
        {
            return new MutablePropertyInstance<Object>( aPropertyBinding, aPropertyValue );
        }
    }

    /**
     * Returns the association given the association method.
     *
     * @param anAssociationMethod The association method. This argument must not be {@code null}.
     * @return The association given the association method.
     * @since 0.1.0
     */
    public AbstractAssociation getAssociation( Method anAssociationMethod )
    {
/*
        IBatisAbstractAssociationInstance association = (IBatisAbstractAssociationInstance) associations.get( anAssociationMethod );
        if( association != null )
        {
            return association;
        }

        // Check whether the association exists.
        CompositeMethodBinding methodBinding = getCompositeBinding().getCompositeMethodBinding( anAssociationMethod );
        AssociationBinding associationBinding = methodBinding.getAssociationBinding();
        if( associationBinding == null )
        {
            CompositeResolution compositeResolution = getCompositeBinding().getCompositeResolution();
            CompositeModel compositeModel = compositeResolution.getCompositeModel();
            Class<? extends Composite> compositeClass = compositeModel.getCompositeType();
            String msg = "There is no association associated with [" + anAssociationMethod +
                         "] for Composite [" + compositeClass + "].";
            throw new IllegalArgumentException( msg );
        }

        Class<?> type = anAssociationMethod.getReturnType();
        if( Association.class.isAssignableFrom( type ) )
        {
            String associationNameKey = anAssociationMethod.getName().toUpperCase();

            String associationIdentity = (String) values.get( associationNameKey );
            return new IBatisAssociation( associationIdentity, associationBinding, status, unitOfWork );
        }
*/


        return null;
    }

    // TODO: This is copied from MemoryEntityStore need to be revised
    private Map<Method, AbstractAssociation> transformToAssociations(
        CompositeBinding compositeBinding, Map<Method, Object> propertyValues )
    {
        Map<Method, AbstractAssociation> associations = new HashMap<Method, AbstractAssociation>();
/*
        Iterable<AssociationBinding> associationBindings = compositeBinding.getAssociationBindings();
        for( AssociationBinding associationBinding : associationBindings )
        {
            AssociationResolution associationResolution = associationBinding.getAssociationResolution();
            AssociationModel associationModel = associationResolution.getAssociationModel();
            Method accessor = associationModel.getAccessor();
            Class<?> type = accessor.getReturnType();
            if( SetAssociation.class.isAssignableFrom( type ) )
            {
//                associations.put( accessor, new SetAssociationInstance())
            }
            else if( ManyAssociation.class.isAssignableFrom( type ) )
            {

                ListAssociationInstance<Object> listInstance =
                    new ListAssociationInstance<Object>( new ArrayList<Object>(), associationBinding, unitOfWork );
                associations.put( accessor, listInstance );
            }
            else
            {
                AssociationInstance<Object> instance = new AssociationInstance<Object>( associationBinding, null );
                associations.put( accessor, instance );
            }
        }
*/
        return associations;
    }

    public final void refresh()
    {
        // Check whether refresh is required at all
        if( status == statusNew || status == statusNewToDeleted || status == statusLoadToDeleted )
        {
            return;
        }

        // TODO
    }

    public void remove()
    {
/*        switch( status )
        {
        case statusNew:
        case statusNewToDeleted:
        case statusLoadToDeleted:
            status = statusNewToDeleted;

        case statusLoadFromDb:
            dao.deleteComposite( getIdentity(), getCompositeBinding() );
        }*/
    }

    /**
     * Persist this entity state.
     *
     * @since 0.1.0
     */
    public final void persist()
    {
        // TODO
    }
}