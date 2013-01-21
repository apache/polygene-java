package org.qi4j.library.struts2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import ognl.*;
import org.qi4j.api.Qi4j;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.library.struts2.ConstraintViolationInterceptor.FieldConstraintViolations;

import static com.opensymphony.xwork2.conversion.impl.XWorkConverter.CONVERSION_PROPERTY_FULLNAME;
import static ognl.OgnlRuntime.getConvertedType;
import static ognl.OgnlRuntime.getFieldValue;
import static org.qi4j.library.struts2.ConstraintViolationInterceptor.CONTEXT_CONSTRAINT_VIOLATIONS;

/**
 * <p>An implementation of the ObjectPropertyAccessor that provides conversion for Qi4j properties.  The typical way that
 * OGNL gets/sets object attributes is by finding the corresponding JavaBean getter/setter methods.  This
 * ObjectPropertyAccessor checks if there is a Qi4j property on the Composite and if there is uses the properties
 * get/set methods.</p>
 *
 * <p>When setting Property values, if a ConstraintViolationException is thrown it is added to the context so that
 * it can be processed and by the ConstraintViolationInterceptor, similar to how conversion exceptions are handled by
 * the ConversionErrorInterceptor</p>
 *
 * <p>When setting Association values, we attempt to convert the value to the association type using the normal XWork
 * converter mechanisms.  If the type is an EntityComposite, we already have a converter registered
 * {@link EntityCompositeConverter} to handle conversion from a string identity to an object.  If the type is not an
 * EntityComposite, but the actual values are EntityComposites, you can register the {@link EntityCompositeConverter}
 * for your type in your xwork-conversion.properties file.</p>
 *
 * <p>NOTE: We can't do this as a regular converter because Qi4j composites doesn't (nor should it be) following the
 * JavaBean standard.  We might be able to only override the getProperty() method here and have regular converters for
 * Property, Association and SetAssociation but I haven't tried that yet so it may not work as expected.</>
 *
 * <p>TODO: Doesn't yet handle ManyAssociations, but these shouldn't be too hard to add</p>
 */
public class Qi4jPropertyAccessor
    extends ObjectPropertyAccessor
{
    private static final Object[] BLANK_ARGUMENTS = new Object[0];

    private final ObjectMethodAccessor methodAccessor = new ObjectMethodAccessor();

    @Structure
    Qi4j api;

    @Override
    public final Object getProperty( Map aContext, Object aTarget, Object aPropertyName )
        throws OgnlException
    {
        String fieldName = aPropertyName.toString();
        Object qi4jField = getQi4jField( aContext, aTarget, fieldName );
        if( qi4jField != null )
        {
            Class memberClass = qi4jField.getClass();
            if( Property.class.isAssignableFrom( memberClass ) )
            {
                Property property = (Property) qi4jField;
                return property.get();
            }
            else if( Association.class.isAssignableFrom( memberClass ) )
            {
                Association association = (Association) qi4jField;
                return association.get();
            }
            else if( ManyAssociation.class.isAssignableFrom( memberClass ) )
            {
                return qi4jField;
            }
        }

        return super.getProperty( aContext, aTarget, fieldName );
    }

    @SuppressWarnings( "unchecked" )
    private Object getQi4jField( Map aContext, Object aTarget, String aFieldName )
        throws OgnlException
    {
        if( aTarget != null )
        {
            // Is target#name a method? e.g. cat.name()
            try
            {
                return methodAccessor.callMethod( aContext, aTarget, aFieldName, BLANK_ARGUMENTS );
            }
            catch( MethodFailedException e )
            {
                // Means not a property/association
            }

            // Is target#name a field? e.g. action.field1, where field1 is extracted from a composite
            OgnlContext ognlContext = (OgnlContext) aContext;
            try
            {
                return getFieldValue( ognlContext, aTarget, aFieldName, true );
            }
            catch( NoSuchFieldException e )
            {
                // Means not a field
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public final void setProperty( Map aContext, Object aTarget, Object aPropertyName, Object aPropertyValue )
        throws OgnlException
    {
        String fieldName = aPropertyName.toString();
        Object qi4jField = getQi4jField( aContext, aTarget, fieldName );

        if( qi4jField != null )
        {
            Class memberClass = qi4jField.getClass();

            if( Property.class.isAssignableFrom( memberClass ) )
            {
                Property property = (Property) qi4jField;

                OgnlContext ognlContext = (OgnlContext) aContext;
                Class propertyType = (Class) api.propertyDescriptorFor( property ).type();
                Object convertedValue = getConvertedType(
                    ognlContext, aTarget, null, fieldName, aPropertyValue, propertyType );
                try
                {
                    property.set( convertedValue );
                }
                catch( ConstraintViolationException e )
                {
                    Collection<ConstraintViolation> violations = e.constraintViolations();
                    handleConstraintViolation( aContext, aTarget, fieldName, convertedValue, violations );
                }

                return;
            }
            else if( Association.class.isAssignableFrom( memberClass ) )
            {
                Association association = (Association) qi4jField;
                OgnlContext ognlContext = (OgnlContext) aContext;
                Class associationType = (Class) api.associationDescriptorFor( association ).type();
                Object convertedValue = getConvertedType(
                    ognlContext, aTarget, null, fieldName, aPropertyValue, associationType );
                if( convertedValue == OgnlRuntime.NoConversionPossible )
                {
                    throw new OgnlException( "Could not convert value to association type" );
                }
                try
                {
                    association.set( convertedValue );
                }
                catch( ConstraintViolationException e )
                {
                    Collection<ConstraintViolation> violations = e.constraintViolations();
                    handleConstraintViolation( aContext, aTarget, fieldName, aPropertyValue, violations );
                }

                return;
            }
            else if( ManyAssociation.class.isAssignableFrom( memberClass ) )
            {
                throw new OgnlException( "Setting many association [" + fieldName + "] is impossible." );
            }
        }

        super.setProperty( aContext, aTarget, aPropertyName, aPropertyValue );
    }

    @SuppressWarnings( "unchecked" )
    protected final void handleConstraintViolation(
        Map aContext, Object aTarget, String aPropertyName, Object aPropertyValue,
        Collection<ConstraintViolation> violations
    )
    {
        Map<String, FieldConstraintViolations> allPropertyConstraintViolations =
            (Map<String, FieldConstraintViolations>) aContext.get( CONTEXT_CONSTRAINT_VIOLATIONS );
        if( allPropertyConstraintViolations == null )
        {
            allPropertyConstraintViolations = new HashMap<String, FieldConstraintViolations>();
            aContext.put( CONTEXT_CONSTRAINT_VIOLATIONS, allPropertyConstraintViolations );
        }

        String realFieldName = aPropertyName;
        String fieldFullName = (String) aContext.get( CONVERSION_PROPERTY_FULLNAME );
        if( fieldFullName != null )
        {
            realFieldName = fieldFullName;
        }
        // Add another violation
        allPropertyConstraintViolations.put(
            realFieldName, new FieldConstraintViolations( aTarget, aPropertyName, aPropertyValue, violations ) );
    }
}
