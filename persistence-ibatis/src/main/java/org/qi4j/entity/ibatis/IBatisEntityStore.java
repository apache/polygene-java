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
package org.qi4j.entity.ibatis;

import com.ibatis.sqlmap.client.SqlMapClient;
import static com.ibatis.sqlmap.client.SqlMapClientBuilder.buildSqlMapClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.association.ManyAssociation;
import org.qi4j.association.SetAssociation;
import org.qi4j.composite.Composite;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.entity.EntitySession;
import static org.qi4j.entity.ibatis.IBatisEntityState.STATUS;
import static org.qi4j.entity.ibatis.IBatisEntityState.STATUS.statusLoadFromDb;
import static org.qi4j.entity.ibatis.IBatisEntityState.STATUS.statusNew;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializer;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.runtime.association.AssociationInstance;
import org.qi4j.runtime.association.ListAssociationInstance;
import org.qi4j.service.Activatable;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.AssociationResolution;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.CompositeResolution;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.property.AssociationBinding;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.structure.ServiceDescriptor;

/**
 * TODO: Figure out how does transaction supposed for all EntityStore methods.
 * TODO: identity is a keyword in SQL. We need to have an alias for this identity property for query purposes.
 *
 * @author edward.yakop@gmail.com
 */
final class IBatisEntityStore
    implements EntityStore<IBatisEntityState>, Activatable
{
    private final IBatisEntityStoreServiceInfo serviceInfo;
    private final DBInitializerInfo dbInitializerInfo;

    private SqlMapClient client;

    /**
     * Construct a new instance of {@code IBatisEntityStore}.
     *
     * @param aServiceDescriptor The service descriptor. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if the specified {@code aServiceDescriptor} argument is {@code null}.
     * @since 0.1.0
     */
    IBatisEntityStore( ServiceDescriptor aServiceDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aServiceDescriptor", aServiceDescriptor );

        Map<Class, Object> serviceInfos = aServiceDescriptor.getServiceInfos();
        serviceInfo = (IBatisEntityStoreServiceInfo) serviceInfos.get( IBatisEntityStoreServiceInfo.class );
        dbInitializerInfo = (DBInitializerInfo) serviceInfos.get( DBInitializerInfo.class );

        client = null;
    }

    /**
     * Returns {@code true}  if the specified {@code compositeType} for the specified {@code identity} is found,
     * {@code false} otherwise.
     *
     * @param anIdentity        The identity.
     * @param aCompositeBinding The composite binding. This argument must not be {@code null}.
     * @return A {@code boolean} indicator whether there exists a composite type with the specified identity.
     * @throws StoreException Thrown if retrieval failed or this method is invoked when this service is not active.
     * @since 0.1.0
     */
    public boolean exists( final String anIdentity, CompositeBinding aCompositeBinding )
        throws StoreException
    {
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "aCompositeBinding", aCompositeBinding );

        throwIfNotActive();


        Object rawData = getRawData( anIdentity, aCompositeBinding );
        return rawData != null;
    }

    /**
     * Returns raw data given the composite class.
     *
     * @param anIdentity        The identity. This argument must not be {@code null}.
     * @param aCompositeBinding The composite class. This argument must not be {@code null}.
     * @return The raw data given input.
     * @throws StoreException Thrown if retrieval failed.
     * @since 0.1.0
     */
    private Map getRawData( String anIdentity, CompositeBinding aCompositeBinding )
        throws StoreException
    {
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "aCompositeBinding", aCompositeBinding );
        CompositeResolution compositeResolution = aCompositeBinding.getCompositeResolution();
        CompositeModel compositeModel = compositeResolution.getCompositeModel();
        Class<? extends Composite> compositeClass = compositeModel.getCompositeClass();
        String statementId = compositeClass.getName() + ".getById";

        // TODO: Transaction?
        try
        {
            return (Map) client.queryForObject( statementId, anIdentity );
        }
        catch( SQLException e )
        {
            throw new StoreException( e );
        }
    }

    /**
     * Throws {@link StoreException} if this service is not active.
     *
     * @throws StoreException Thrown if this service instance is not active.
     * @since 0.1.0
     */
    private void throwIfNotActive()
        throws StoreException
    {
        if( client == null )
        {
            String message = "Possibly bug in the qi4j where the store is not activate but its service is invoked.";
            throw new StoreException( message );
        }
    }

    /**
     * Construct a new entity instance.
     *
     * @param aSession          The entity session.
     * @param anIdentity        The new entity identity. This argument must not be {@code null}.
     * @param aCompositeBinding The composite binding. This argument must not be {@code null}.
     * @param propertyValues    The property values. This argument must not be {@code null}.
     * @return The new entity state.
     * @throws IllegalArgumentException Thrown if one or some or all arguments are {@code null}.
     * @throws StoreException           Thrown if creational failed.
     * @since 0.1.0
     */
    public final IBatisEntityState newEntityInstance(
        EntitySession aSession, String anIdentity, CompositeBinding aCompositeBinding,
        Map<String, Object> propertyValues )
        throws IllegalArgumentException, StoreException
    {
        validateNotNull( "aSession", aSession );
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "aCompositeBinding", aCompositeBinding );
        validateNotNull( "propertyValues", propertyValues );

        return newEntityInstance( anIdentity, aCompositeBinding, propertyValues, true, statusNew );
    }

    /**
     * Actual implementation of {@code newEntityInstance}. Must not return {@code null}.
     *
     * @param anIdentity        anIdentity.
     * @param aCompositeBinding The composite binding. This argument must not be {@code null}.
     * @param fieldValues       The field values (this applies for both property and associations.
     *                          This argument must not be {@code null}.
     * @param isUseDefaultValue Sets to {@code true} to lookup default value when the initial value is not located in
     *                          {@code fieldValues} argument, {@code false} to disable this feature.
     * @param aStatus           The initial status for the created state.
     * @return A new entity instance.
     * @throws StoreException Thrown if creating new instance failed.
     * @since 0.1.0
     */
    private IBatisEntityState newEntityInstance(
        String anIdentity, CompositeBinding aCompositeBinding,
        Map<String, Object> fieldValues, boolean isUseDefaultValue, STATUS aStatus )
        throws StoreException
    {
        throwIfNotActive();

        Map<String, Property> properties = transformToProperties( aCompositeBinding, fieldValues, isUseDefaultValue );
        Map<String, AbstractAssociation> associations = transformToAssociations( aCompositeBinding, fieldValues );
        return new IBatisEntityState( anIdentity, aCompositeBinding, properties, associations, aStatus, this );
    }

    /**
     * Transform the map of property values to map of property. Must not return {@code null}.
     *
     * @param aCompositeBinding The composite binding. This argument must not be {@code null}.
     * @param propertyValues    The property values. This argument must not be {@code null}.
     * @param useDefaultValue   Sets to {@code true} if one should use the default values.
     * @return The map of properties.
     * @since 0.1.0
     */
    final Map<String, Property> transformToProperties(
        CompositeBinding aCompositeBinding, Map<String, Object> propertyValues, boolean useDefaultValue )
    {
        Map<String, Property> properties = new HashMap<String, Property>();
        Iterable<PropertyBinding> propertyBindings = aCompositeBinding.getPropertyBindings();
        for( PropertyBinding propertyBinding : propertyBindings )
        {
            String propQualifiedName = propertyBinding.getQualifiedName();
            Object value = computePropertyValue( propertyBinding, propertyValues, useDefaultValue );
            Property<Object> propertyInstance = newPropertyInstance( propertyBinding, value );

            properties.put( propQualifiedName, propertyInstance );
        }

        return properties;
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
        String propertyName = propertyBinding.getName();

        Object value = null;
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
     * Check The property value type against its property declaration.
     * <p/>
     * Note: This method only check {@code Class} value type. GenericArrayType, TypeVariable, WildcardType and
     * ParameterizedType are not handled yet.
     *
     * @param aPropertyValue   The property value.
     * @param aPropertyBinding The property binding.
     * @throws IllegalStateException Thrown if there is a mismatch of expected and actual value type.
     * @since 0.1.0
     */
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
            return new IBatisMutablePropertyInstance<Object>( aPropertyBinding, aPropertyValue );
        }
    }

    // TODO: This is copied from MemoryEntityStore need to be revised
    private Map<String, AbstractAssociation> transformToAssociations(
        CompositeBinding compositeBinding, Map<String, Object> propertyValues )
    {
        Map<String, AbstractAssociation> associations = new HashMap<String, AbstractAssociation>();
        Iterable<AssociationBinding> associationBindings = compositeBinding.getAssociationBindings();
        for( AssociationBinding associationBinding : associationBindings )
        {
            AssociationResolution associationResolution = associationBinding.getAssociationResolution();
            AssociationModel associationModel = associationResolution.getAssociationModel();
            Method accessor = associationModel.getAccessor();
            Class<?> type = accessor.getReturnType();
            String assocationQualifiedName = associationBinding.getQualifiedName();
            if( SetAssociation.class.isAssignableFrom( type ) )
            {
//                associations.put( assocationQualifiedName, new SetAssociationInstance())
            }
            else if( ManyAssociation.class.isAssignableFrom( type ) )
            {

                ListAssociationInstance<Object> listInstance =
                    new ListAssociationInstance<Object>( new ArrayList<Object>(), associationBinding );
                associations.put( assocationQualifiedName, listInstance );
            }
            else
            {
                AssociationInstance<Object> instance = new AssociationInstance<Object>( associationBinding, null );
                associations.put( assocationQualifiedName, instance );
            }
        }
        return associations;
    }

    /**
     * Returns existing entity instance. Returns {@code null} if not found.
     *
     * @param anEntitySession   The entity session. This argument must not be {@code null}.
     * @param anIdentity        The identity. This argument must not be {@code null}.
     * @param aCompositeBinding The composite binding. This argument must not be {@code null}.
     * @return The entity instance with id as {@code anIdentity}.
     * @throws IllegalArgumentException Thrown if one or some or all arguments are {@code null}.
     * @throws StoreException           Thrown if retrieval fail.
     * @since 0.1.0
     */
    @SuppressWarnings( "unchecked" )
    public final IBatisEntityState getEntityInstance(
        EntitySession anEntitySession, String anIdentity, CompositeBinding aCompositeBinding )
        throws IllegalArgumentException, StoreException
    {
        validateNotNull( "anEntitySession", anEntitySession );
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "aCompositeBinding", aCompositeBinding );

        throwIfNotActive();

        Map rawData = getRawData( anIdentity, aCompositeBinding );
        if( rawData == null )
        {
            return null;
        }

        return newEntityInstance( anIdentity, aCompositeBinding, rawData, false, statusLoadFromDb );
    }

    /**
     * Complete or persists the list of entity state.
     *
     * @param session The entity session. This argument must not be {@code null}.
     * @param states  The states to complete. This argument must not be {@code null}.
     * @throws StoreException Thrown if the complete failed.
     * @since 0.1.0
     */
    public final void complete( EntitySession session, List<IBatisEntityState> states )
        throws StoreException
    {
        throwIfNotActive();

        for( IBatisEntityState state : states )
        {
            state.persist();
        }
    }

    final boolean deleteComposite( String anIdentity, CompositeBinding aCompositeBinding )
    {
        // TODO
        return false;
    }

    /**
     * Activate this service.
     *
     * @throws IOException  If reading sql map configuration failed.
     * @throws SQLException Thrown if database initialization failed.
     * @since 0.1.0
     */
    public final void activate()
        throws IOException, SQLException
    {
        // Initialize database if required.
        if( dbInitializerInfo != null )
        {
            DBInitializer dbInitializer = new DBInitializer( dbInitializerInfo );
            dbInitializer.initialize();
        }

        // Initialize client
        String configURL = serviceInfo.getSQLMapConfigURL();
        InputStream configStream = new URL( configURL ).openStream();
        InputStreamReader streamReader = new InputStreamReader( configStream );
        Reader bufferedReader = new BufferedReader( streamReader );

        Properties properties = serviceInfo.getConfigProperties();
        if( properties == null )
        {
            client = buildSqlMapClient( bufferedReader );
        }
        else
        {
            client = buildSqlMapClient( bufferedReader, properties );
        }
    }

    /**
     * Passivate this service.
     *
     * @throws Exception Thrown if there is any passivation problem.
     * @since 0.1.0
     */
    public final void passivate()
        throws Exception
    {
        // clean up client
        client = null;
    }
}
