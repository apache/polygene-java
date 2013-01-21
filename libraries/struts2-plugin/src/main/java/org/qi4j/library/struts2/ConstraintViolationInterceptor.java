package org.qi4j.library.struts2;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ValidationAware;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.interceptor.PreResultListener;
import com.opensymphony.xwork2.util.ValueStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.library.struts2.util.ClassNameMapper;

import static java.util.Collections.emptyMap;
import static org.qi4j.library.struts2.util.ClassNameFilters.removeSuffixes;
import static org.qi4j.library.struts2.util.ClassNames.classNameInDotNotation;

/**
 * <p>ConstraintViolationInterceptor adds constraint violations from the ActionContext to the Action's field errors.</p>
 *
 * <p>This interceptor adds any error found in the {@link ActionContext}'s constraint violations map as a field error
 * (provided that the action implements {@link ValidationAware}). In addition, any field that contains a constraint
 * violation has its original value saved such that any subsequent requests for that value return the original value
 * rather than the value in the action. This is important because if the value "abc" is submitted and can't be set
 * on a property requiring at least 5 characters, we want to display the original string ("abc") again rather than the
 * original value (likely an empty string, which would make very little sense to the user).</p>
 *
 * <p>This is similar, in principle, to the XWork ConversionErrorInterceptor and much of the code is reflects that.</p>
 */
public class ConstraintViolationInterceptor
    extends AbstractInterceptor
{
    static final long serialVersionUID = 1L;

    public static final String CONTEXT_CONSTRAINT_VIOLATIONS = ConstraintViolationInterceptor.class.getName() + ".constraintViolations";

    protected Object getOverrideExpr( ActionInvocation invocation, FieldConstraintViolations violations )
    {
        return "'" + violations.value() + "'";
    }

    @Override
    public String intercept( ActionInvocation invocation )
        throws Exception
    {
        ActionContext invocationContext = invocation.getInvocationContext();
        ValueStack stack = invocationContext.getValueStack();

        Object action = invocation.getAction();
        if( action instanceof ValidationAware )
        {
            ValidationAware va = (ValidationAware) action;
            HashMap<Object, Object> propertyOverrides = new HashMap<Object, Object>();
            for( Map.Entry<String, FieldConstraintViolations> fieldViolations : fieldConstraintViolations( invocationContext )
                .entrySet() )
            {
                addConstraintViolationFieldErrors( stack, va, fieldViolations.getKey(), fieldViolations.getValue() );
                propertyOverrides.put( fieldViolations.getKey(), getOverrideExpr( invocation, fieldViolations.getValue() ) );
            }
            // if there were some errors, put the original (fake) values in place right before the result
            if( !propertyOverrides.isEmpty() )
            {
                overrideActionValues( invocation, stack, propertyOverrides );
            }
        }

        return invocation.invoke();
    }

    private void overrideActionValues(
        ActionInvocation invocation, ValueStack stack, final HashMap<Object, Object> propertyOverrides
    )
    {
        invocation.addPreResultListener( new PreResultListener()
        {
            @Override
            public void beforeResult( ActionInvocation invocation, String resultCode )
            {
                invocation.getStack().setExprOverrides( propertyOverrides );
            }
        } );
    }

    private void addConstraintViolationFieldErrors(
        ValueStack stack, ValidationAware va, String fieldName, FieldConstraintViolations violations
    )
    {
        for( ConstraintViolation constraintViolation : violations.constraintViolations() )
        {
            Object target = violations.target();
            String message = message( target, violations.propertyName(), constraintViolation, stack );
            va.addFieldError( fieldName, message );
        }
    }

    @SuppressWarnings( "unchecked" )
    private Map<String, FieldConstraintViolations> fieldConstraintViolations( ActionContext context )
    {
        Map<String, FieldConstraintViolations> violations =
            (Map<String, FieldConstraintViolations>) context.get( CONTEXT_CONSTRAINT_VIOLATIONS );
        if( violations == null )
        {
            return emptyMap();
        }

        return violations;
    }

    protected String message( Object target,
                              String propertyName,
                              ConstraintViolation constraintViolation,
                              ValueStack stack
    )
    {
        String messageKey = messageKey( target, propertyName, constraintViolation );
        String getTextExpression = "getText('" + messageKey + "')";
        String message = (String) stack.findValue( getTextExpression );

        if( message == null )
        {
            message = messageKey;
        }

        return message;
    }

    /**
     * <p>The message key is generated based on the type of the target, the name of the property and the type of the
     * constraint violation.  So, if the target has type ItemEntity with a name property that has a not empty constraint
     * and the user doesn't enter anything for the value, the corresponding message key would be
     * 'item.name.not.empty.constraint.violated'.</p>
     *
     * <p>Note that if the type name of the target ends with 'Composite' or 'Entity', those will be removed and the
     * rest of the name will be converted from camel-case to a dot notation.  This is true of the constraint types as
     * well.  So a constraint named NotEmpty will be converted to not.empty as in the example above.</p>
     * @param target JAVADOC
     * @param propertyName JAVADOC
     * @param violation JAVADOC
     * @return JAVADOC
     */
    protected String messageKey( Object target, String propertyName, ConstraintViolation violation )
    {
        Iterable<Class<?>> types;
        if( target instanceof Composite )
        {
            Composite composite = (Composite) target;
            types = Qi4j.FUNCTION_DESCRIPTOR_FOR.map( composite ).types();
        }
        else
        {
            ArrayList<Class<?>> list = new ArrayList<Class<?>>( 1 );
            list.add(target.getClass());
            types = list;
        }

        return classNameInDotNotation( types, withoutCompositeOrEntitySuffix )
               + "." + propertyName
               + "." + constraintKeyPart( violation )
               + ".constraint.violated";
    }

    private static final ClassNameMapper withoutCompositeOrEntitySuffix = removeSuffixes( "Composite", "Entity" );

    private String constraintKeyPart( ConstraintViolation constraintViolation )
    {
        return classNameInDotNotation( constraintViolation.constraint().annotationType() );
    }

    public static class FieldConstraintViolations
    {
        private final Object target;
        private final String propertyName;
        private final Object value;
        private final Collection<ConstraintViolation> constraintViolations;

        public FieldConstraintViolations(
            Object aTarget,
            String aPropertyName,
            Object aValue,
            Collection<ConstraintViolation> constraintViolations
        )
        {
            target = aTarget;
            propertyName = aPropertyName;
            value = aValue;
            this.constraintViolations = constraintViolations;
        }

        public Object target()
        {
            return target;
        }

        public String propertyName()
        {
            return propertyName;
        }

        public Object value()
        {
            return value;
        }

        public Collection<ConstraintViolation> constraintViolations()
        {
            return constraintViolations;
        }
    }
}
