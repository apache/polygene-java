/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.api.constraint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;

/**
 * This Exception is thrown when there is one or more Constraint Violations in a method
 * call.
 * <p>
 * The Constraint Violations are aggregated per method, and this exception will contain those
 * violations, together with the Composite instance it happened on as well as the Method that
 * was invoked. The Exception also has support for localized messages of these violations.
 * </p>
 * <p>
 * <b>This class is still under evolution. Beware that the methods, formatting, Locale spec may
 * change. It will be stable before the 1.0 release.
 * </p>
 */
public class ConstraintViolationException
    extends IllegalArgumentException
{
    private static final long serialVersionUID = 1L;

    private final Collection<ConstraintViolation> constraintViolations;
    private String methodName;
    private String mixinTypeName;
    private String instanceToString;
    private Iterable<Class<?>> instanceTypes;

    public ConstraintViolationException( Composite instance, Member method,
                                         Collection<ConstraintViolation> constraintViolations
    )
    {
        this( instance.toString(), Qi4j.FUNCTION_DESCRIPTOR_FOR.map( instance ).types(), method, constraintViolations );
    }

    public ConstraintViolationException( String instanceToString,
                                         Iterable<Class<?>> instanceTypes,
                                         Member method,
                                         Collection<ConstraintViolation> violations
    )
    {
        this.instanceToString = instanceToString;
        this.instanceTypes = instanceTypes;
        mixinTypeName = method.getDeclaringClass().getName();
        methodName = method.getName();
        this.constraintViolations = violations;
    }

    public ConstraintViolationException( String instanceToString,
                                         Iterable<Class<?>> instanceTypes,
                                         String mixinTypeName,
                                         String methodName,
                                         Collection<ConstraintViolation> violations
    )
    {
        this.instanceToString = instanceToString;
        this.instanceTypes = instanceTypes;
        this.mixinTypeName = mixinTypeName;
        this.methodName = methodName;
        this.constraintViolations = violations;
    }

    public Collection<ConstraintViolation> constraintViolations()
    {
        return constraintViolations;
    }

    /**
     * Creates localized messages of all the constraint violations that has occured.
     * <p/>
     * The key &nbsp;"<code>Qi4j_ConstraintViolation_<i><strong>CompositeType</strong></code></i>" will be used to lookup the text formatting
     * pattern from the ResourceBundle, where <strong><code><i>CompositeType</i></code></strong> is the
     * class name of the Composite where the constraint was violated. If such key does not exist, then the
     * key &nbsp;"<code>Qi4j_ConstraintViolation</code>" will be used, and if that one also doesn't exist, or
     * the resourceBundle argument is null, then the default patterns will be used;
     * <table><tr><th>Type of Composite</th><th>Pattern used</th></tr>
     * <tr><td>Composite</td>
     * <td><code>Constraint Violation in {2}.{3} with constraint {4}, in composite \n{0} of type {1}</code></td>
     * </tr>
     * <tr><td>EntityComposite</td>
     * <td><code>Constraint Violation in {2}.{3} with constraint {4}, in entity {1}[id={0}]</code></td>
     * </tr>
     * <tr><td>ServiceComposite</td>
     * <td><code>Constraint Violation in {2}.{3} with constraint {4}, in service {0}</code></td>
     * </tr>
     * </table>
     * Then format each ConstraintViolation according to such pattern, where the following argument are passed;
     * <table><tr><th>Arg</th><th>Value</th></tr>
     * <tr>
     * <td>{0}</td>
     * <td>Composite instance toString()</td>
     * </tr>
     * <tr>
     * <td>{1}</td>
     * <td>CompositeType class name</td>
     * </tr>
     * <tr>
     * <td>{2}</td>
     * <td>MixinType class name</td>
     * </tr>
     * <tr>
     * <td>{3}</td>
     * <td>MixinType method name</td>
     * </tr>
     * <tr>
     * <td>{4}</td>
     * <td>Annotation toString()</td>
     * </tr>
     * <tr>
     * <td>{5}</td>
     * <td>toString() of value passed as the argument, or "null" text if argument was null.</td>
     * </tr>
     * </table>
     * <p/>
     * <b>NOTE!!!</b> This class is still under construction and will be modified further.
     *
     * @param bundle The ResourceBundle for Localization, or null if default formatting and locale to be used.
     *
     * @return An array of localized messages of the violations incurred.
     */
    public String[] localizedMessagesFrom( ResourceBundle bundle )
    {
        String pattern = "Constraint violation in {0}.{1} for method ''{3}'' with constraint \"{4}({6})\", for value ''{5}''";

        ArrayList<String> list = new ArrayList<String>();
        for( ConstraintViolation violation : constraintViolations )
        {
            Locale locale;
            if( bundle != null )
            {
                try
                {
                    pattern = bundle.getString( "qi4j.constraint." + mixinTypeName + "." + methodName );
                }
                catch( MissingResourceException e1 )
                {
                    try
                    {
                        pattern = bundle.getString( "qi4j.constraint" );
                    }
                    catch( MissingResourceException e2 )
                    {
                        // ignore. The default pattern will be used.
                    }
                }
                locale = bundle.getLocale();
            }
            else
            {
                locale = Locale.getDefault();
            }
            MessageFormat format = new MessageFormat( pattern, locale );

            Annotation annotation = violation.constraint();
            String name = violation.name();
            Object value = violation.value();
            String classes;
            if( Iterables.count( instanceTypes) == 1 )
            {
                classes = Iterables.first( instanceTypes ).getSimpleName();
            }
            else
            {
                classes = "[" + Iterables.<Class<?>>toString( instanceTypes, new Function<Class<?>, String>()
                {
                    @Override
                    public String map( Class<?> from )
                    {
                        return from.getSimpleName();
                    }
                }, "," ) + "]";
            }
            Object[] args = new Object[]
                {
                    instanceToString,
                    classes,
                    mixinTypeName,
                    methodName,
                    annotation.toString(),
                    "" + value,
                    name
                };
            StringBuffer text = new StringBuffer();
            format.format( args, text, null );
            list.add( text.toString() );
        }
        String[] result = new String[ list.size() ];
        list.toArray( result );
        return result;
    }

    public String localizedMessage()
    {
        String[] messages = localizedMessagesFrom( null );
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for( String message : messages )
        {
            if( !first )
            {
                result.append( ',' );
            }
            first = false;
            result.append( message );
        }
        return result.toString();
    }

    @Override
    public String getLocalizedMessage()
    {
        return localizedMessage();
    }

    @Override
    public String getMessage()
    {
        return localizedMessage();
    }

    public String methodName()
    {
        return methodName;
    }

    public String mixinTypeName()
    {
        return mixinTypeName;
    }
}