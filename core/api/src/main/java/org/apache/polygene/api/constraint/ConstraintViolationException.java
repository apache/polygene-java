/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.api.constraint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.polygene.api.composite.CompositeDescriptor;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.util.Classes;

/**
 * This Exception is thrown when there is one or more Constraint Violations in a method
 * call.
 * <p>
 * The Constraint Violations are aggregated per method, and this exception will contain those
 * violations, together with the Composite instance it happened on as well as the Method that
 * was invoked. The Exception also has support for localized messages of these violations.
 * </p>
 */
public class ConstraintViolationException extends IllegalArgumentException
{
    private static final String NL = System.getProperty( "line.separator" );
    private static final boolean longNames = Boolean.getBoolean( "polygene.constraints.longNames" );
    private static final String DEFAULT_PATTERN = NL + "\tConstraint Violation(s) in {0} of types [{3}]." + NL;
    private static final String ENTITY_DEFAULT_PATTERN = NL + "\tConstraint Violation(s) in entity {0} with id=[{2}]." + NL;
    private static final String SERVICE_DEFAULT_PATTERN = NL + "\tConstraint Violation(s) in service {0} with id=[{2}]." + NL;
    private static final String MIXIN_DEFAULT_PATTERN = "\t\t@{2}({3}) on {0}.{1}(). Parameter [{4}] does not allow value [{5}]." + NL;

    private String instanceToString;                              // arg {0}
    private Class<?> primaryType;                                 // arg {1}
    private List<? extends Type> instanceTypes;                   // arg {2}
    private Collection<ValueConstraintViolation> constraintViolations; // arg {4} and {5}
    private String identity;                                      // arg {6}
    private boolean isService;
    private boolean isEntity;

    public ConstraintViolationException( Collection<ValueConstraintViolation> violations )
    {
        this.constraintViolations = new ArrayList<>();
        this.constraintViolations.addAll( violations );
    }

    public Collection<ValueConstraintViolation> constraintViolations()
    {
        return constraintViolations;
    }

    /**
     * Creates localized message of all the constraint violations that has occured.
     * <p>
     * Each ConstraintViolationException concerns one Composite instance, but may have many violations on that
     * instance. For the composite instance related message following entries in the ResourceBundle will be searched
     * for a pattern in the following order;
     * </p>
     * <ol>
     * <li><code>polygene.constraint.<i><strong>CompositeType</strong></i></code></li>
     * <li><code>polygene.constraint.composite</code></li>
     * </ol>
     * <p>
     * where <strong><code><i>CompositeType</i></code></strong> is the
     * class name of the Composite instance. If such key does not exist, or if the resourceBundle argument is null,
     * then the default patterns will be used;
     * </p>
     * <table summary="Default localization of constraint violations for composite.">
     * <tr><th>Type of Composite</th><th>Pattern used</th></tr>
     * <tr><td>Composite</td>
     * <td><code>\tConstraint Violation(s) in {0} with types {3}\n</code></td>
     * </tr>
     * <tr><td>EntityComposite</td>
     * <td><code>Constraint Violation in {2}.{3} with constraint {4}, in entity {1}[id={0}]</code></td>
     * </tr>
     * <tr><td>ServiceComposite</td>
     * <td><code>Constraint Violation in {2}.{3} with constraint {4}, in service {0}</code></td>
     * </tr>
     * </table>
     * The ResourceBundle arguments are defined as;
     * <p>
     * <p>
     * Then format each ConstraintViolation according to such pattern, where the following argument are passed;
     * <table summary="List of arguments available."><tr><th>Arg</th><th>Value</th></tr>
     * <tr>
     * <td>{0}</td>
     * <td>Primary Type of Composite</td>
     * </tr>
     * <tr>
     * <td>{1}</td>
     * <td>Composite instance toString()</td>
     * </tr>
     * <tr>
     * <td>{2}</td>
     * <td>Identity if composite implements HasIdentity</td>
     * </tr>
     * <tr>
     * <td>{3}</td>
     * <td>Comma-separeated list of types implemented by Composite</td>
     * </tr>
     * </table>
     * <p>
     * Once the message at the composite type level has been established, the message will contain each of the found
     * violations. For each such violation, the resource bundle will be searched in the following order;
     * <ol>
     * <li><code>polygene.constraint.<i><strong>MixinType</strong></i>.<i><strong>member</strong></i></code></li>
     * <li><code>polygene.constraint.<i><strong>MixinType</strong></i></code></li>
     * <li><code>polygene.constraint.mixin</code></li>
     * </ol>
     * where <code><i><strong>MixinType</strong></i></code> refers to the mixin type of the member (method, field or
     * constructor) and the <code><i><strong>member</strong></i></code> is the name of such Member.
     * <table summary="Default localization of constraint violations for mixin.">
     * <tr><th>Type of Composite</th><th>Pattern used</th></tr>
     * <tr><td>Mixin</td>
     * <td><code>\t\t@{2} {0}.{1} does not allow value [{4}]</code></td>
     * </tr>
     * </table>
     * For these the ResourceBundle arguments are;
     * <table summary="List of arguments available."><tr><th>Arg</th><th>Value</th></tr>
     * <tr>
     * <td>{0}</td>
     * <td>Mixin Type Name</td>
     * </tr>
     * <tr>
     * <td>{1}</td>
     * <td>Mixin Member Name</td>
     * </tr>
     * <tr>
     * <td>{2}</td>
     * <td>Annotation type</td>
     * </tr>
     * <tr>
     * <td>{3}</td>
     * <td>Annotation toString</td>
     * </tr>
     * <tr>
     * <td>{4}</td>
     * <td>Name of the Member, see {@link Name}</td>
     * </tr>
     * <tr>
     * <td>{5}</td>
     * <td>Value attempted</td>
     * </tr>
     * </table>
     *
     * @param bundle The ResourceBundle for Localization, or null if default formatting and locale to be used.
     * @return An array of localized messages of the violations incurred.
     */
    public String localizedMessageFrom( ResourceBundle bundle )
    {
        Locale locale;
        if( bundle != null )
        {
            locale = bundle.getLocale();
        }
        else
        {
            locale = Locale.getDefault();
        }
        StringBuffer message = new StringBuffer();
        {
            String[] searchKeys = new String[]{ "polygene.constraint." + primaryType, "polygene.constraint.composite" };
            String compositePattern = findPattern( bundle, searchKeys, defaultPattern() );
            String types = instanceTypes == null
                           ? null
                           : instanceTypes.stream()
                                          .map( this::nameOf )
                                          .collect( Collectors.joining( "," ) );
            String name = "";
            if( primaryType != null )
            {
                if( longNames )
                {
                    name = primaryType.getName();
                }
                else
                {
                    name = primaryType.getSimpleName();
                }
            }
            Object[] args = new Object[]{ name, instanceToString, identity, types };
            MessageFormat formatter = new MessageFormat( compositePattern, locale );
            formatter.format( args, message, null );
        }
        for( ValueConstraintViolation violation : constraintViolations )
        {
            String[] searchKeys = new String[]{ "polygene.constraint." + primaryType, "polygene.constraint.composite" };
            String mixinPattern = findPattern( bundle, searchKeys, MIXIN_DEFAULT_PATTERN );

            Annotation annotation = violation.constraint();
            Class<? extends Annotation> annotatioType = annotation.annotationType();
            Class<?> mixinType = violation.mixinType();
            Object[] args = new Object[]
                {
                    longNames ? mixinType.getName() : mixinType.getSimpleName(),
                    violation.methodName(),
                    longNames ? annotatioType.getName() : annotatioType.getSimpleName(),
                    annotation.toString(),
                    violation.name(),
                    violation.value()
                };
            MessageFormat formatter = new MessageFormat( mixinPattern, locale );
            formatter.format( args, message, null );
        }
        String result = message.toString();
        message.setLength( 0 ); // TODO: is this still needed to avoid JVM memory leak??
        return result;
    }

    private String nameOf( Type type )
    {
        Class<?> clazz = Classes.RAW_CLASS.apply( type );
        if( longNames )
        {
            return clazz.getName();
        }
        else
        {
            return clazz.getSimpleName();
        }
    }

    @Override
    public String getMessage()
    {
        return localizedMessageFrom( null );
    }

    private String findPattern( ResourceBundle bundle, String[] searchKeys, String defaultPattern )
    {
        String compositePattern;
        if( bundle != null )
        {
            compositePattern = Stream.of( searchKeys )
                                     .map( name -> findPattern( bundle, name ) )
                                     .filter( Objects::nonNull )
                                     .findFirst().orElse( defaultPattern );
        }
        else
        {
            compositePattern = defaultPattern;
        }
        return compositePattern;
    }

    private String findPattern( ResourceBundle bundle, String name )
    {
        try
        {
            return bundle.getString( name );
        }
        catch( Exception e )
        {
            return null;
        }
    }

    private String defaultPattern()
    {
        if( isEntity )
        {
            return ENTITY_DEFAULT_PATTERN;
        }
        if( isService )
        {
            return SERVICE_DEFAULT_PATTERN;
        }
        return DEFAULT_PATTERN;
    }

    public void setCompositeDescriptor( CompositeDescriptor descriptor )
    {
        this.primaryType = descriptor.primaryType();
        this.instanceTypes = descriptor.mixinTypes().collect( Collectors.toList() );
        this.isEntity = descriptor instanceof EntityDescriptor;
        this.isService = descriptor instanceof ServiceDescriptor;
    }

    public void setIdentity( Identity identity )
    {
        if( identity == null )
        {
            return;
        }
        this.identity = identity.toString();
    }

    public void setInstanceString( String instanceString )
    {
        instanceToString = instanceString;
    }
}