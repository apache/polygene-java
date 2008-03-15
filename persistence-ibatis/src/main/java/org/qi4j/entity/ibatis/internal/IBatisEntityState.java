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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.association.ManyAssociation;
import org.qi4j.association.SetAssociation;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.entity.ibatis.IBatisEntityStoreServiceInfo;
import static org.qi4j.entity.ibatis.internal.IBatisEntityStateStatus.statusLoadToDeleted;
import static org.qi4j.entity.ibatis.internal.IBatisEntityStateStatus.statusNew;
import static org.qi4j.entity.ibatis.internal.IBatisEntityStateStatus.statusNewToDeleted;
import org.qi4j.entity.ibatis.internal.property.MutablePropertyInstance;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.runtime.association.AssociationInstance;
import org.qi4j.runtime.association.ListAssociationInstance;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.AssociationResolution;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeMethodBinding;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.property.AssociationBinding;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;

/**
 * {@code IBatisEntityState} represents {@code IBatis} version of {@link org.qi4j.spi.entity.EntityState}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class IBatisEntityState
    implements EntityState
{
    private final String identity;
    private final CompositeBinding compositeBinding;
    private final Map<String, Object> values;
    private final Map<Method, Property> properties;
    private IBatisEntityStateStatus status;
    private final IBatisEntityStateDao dao;
    private final IBatisEntityStoreServiceInfo serviceInfo;

    /**
     * Construct an instance of {@code IBatisEntityState}.
     *
     * @param anIdentity        The identity of the composite that this {@code IBatisEntityState} represents.
     *                          This argument must not be {@code null}.
     * @param aCompositeBinding The composite binding. This argument must not be {@code null}.
     * @param valuez            The field valuez of this entity state. This argument must not be {@code null}.
     * @param aStatus           The initial entity state status. This argument must not be {@code null}.
     * @param aDao              The dao to retrieve associations and complete this entity state.
     *                          This argument must not be {@code null}.
     * @param aServiceInfo      The service info. This is used to check whether printing debug information is required.
     *                          This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if one or some or all arguments are {@code null}.
     * @since 0.1.0
     */
    public IBatisEntityState(
        String anIdentity, CompositeBinding aCompositeBinding,
        Map<String, Object> valuez, IBatisEntityStateStatus aStatus, IBatisEntityStateDao aDao,
        IBatisEntityStoreServiceInfo aServiceInfo )
        throws IllegalArgumentException
    {
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "aCompositeBinding", aCompositeBinding );
        validateNotNull( "valuez", valuez );
        validateNotNull( "aDao", aDao );
        validateNotNull( "aServiceInfo", aServiceInfo );

        identity = anIdentity;
        compositeBinding = aCompositeBinding;
        values = valuez;
        status = aStatus;
        dao = aDao;
        serviceInfo = aServiceInfo;
        properties = new HashMap<Method, Property>();

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
     * Returns the identity of the entity that this EntityState represents.
     *
     * @return the identity of the entity that this EntityState represents.
     * @since 0.1.0
     */
    public final String getIdentity()
    {
        return identity;
    }

    /**
     * Returns the composite binding.
     *
     * @return The composite binding.
     * @since 0.1.0
     */
    public final CompositeBinding getCompositeBinding()
    {
        return compositeBinding;
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
        Property propertyInstance = properties.get( aPropertyMethod );

        if( propertyInstance == null )
        {
            CompositeMethodBinding compositeMethodBinding = compositeBinding.getCompositeMethodBinding( aPropertyMethod );
            PropertyBinding propertyBinding = compositeMethodBinding.getPropertyBinding();
            Boolean useDefaultValue = ( status == statusNew ) || ( status == statusNewToDeleted );
            Object value = computePropertyValue( propertyBinding, values, useDefaultValue );
            propertyInstance = newPropertyInstance( propertyBinding, value );

            properties.put( aPropertyMethod, propertyInstance );
        }

        return propertyInstance;
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

        // Check if debug mode
        if( value != null && serviceInfo.isDebugMode() )
        {
            checkPropertyValueType( value, propertyBinding );
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

    private void checkPropertyValueType(
        Object aPropertyValue, PropertyBinding aPropertyBinding )
        throws IllegalStateException
    {
        String propertyQualifiedName = aPropertyBinding.getQualifiedName();
        PropertyResolution propertyResolution = aPropertyBinding.getPropertyResolution();
        PropertyModel propertyModel = propertyResolution.getPropertyModel();
        Type type = propertyModel.getType();
        Class<? extends Type> typeClass = type.getClass();
        Class<?> valueClass = aPropertyValue.getClass();

        if( Class.class.isAssignableFrom( typeClass ) )
        {
            Class typeAsClass = (Class) type;
            if( !typeAsClass.isAssignableFrom( valueClass ) )
            {
                String msg = "Mismatch propervy [" + propertyQualifiedName + "] value type. Expected [" +
                             typeAsClass.getName() + "] Actual [" + valueClass + "].";
                throw new IllegalStateException( msg );
            }
        }
    }

    public AbstractAssociation getAssociation( Method associationMethod )
    {
        return null;
    }

    // TODO: This is copied from MemoryEntityStore need to be revised
    private Map<Method, AbstractAssociation> transformToAssociations(
        CompositeBinding compositeBinding, Map<Method, Object> propertyValues )
    {
        Map<Method, AbstractAssociation> associations = new HashMap<Method, AbstractAssociation>();
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
                    new ListAssociationInstance<Object>( new ArrayList<Object>(), associationBinding );
                associations.put( accessor, listInstance );
            }
            else
            {
                AssociationInstance<Object> instance = new AssociationInstance<Object>( associationBinding, null );
                associations.put( accessor, instance );
            }
        }
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

    public boolean delete()
        throws StoreException
    {
        switch( status )
        {
        case statusNew:
        case statusNewToDeleted:
        case statusLoadToDeleted:
            status = statusNewToDeleted;
            return true;

        case statusLoadFromDb:
            return dao.deleteComposite( identity, compositeBinding );
        }

        return false;
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