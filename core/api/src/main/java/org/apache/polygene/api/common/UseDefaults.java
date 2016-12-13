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
package org.apache.polygene.api.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to denote that the initial value of a Property will be the default value for the type if none is
 * specified during construction.
 * <p>
 * These are the default values used for various types:
 * </p>
 * <pre>
 * Byte: 0
 * Short: 0
 * Character: 0
 * Integer: 0
 * Long: 0L
 * Double: 0.0d
 * Float: 0.0f
 * Boolean: false
 * String: ""
 * List: empty java.util.ArrayList
 * Set: empty java.util.HashSet
 * Collection: empty java.util.ArrayList
 * enum: first declared value
 * </pre>
 * <p>
 * If this annotation is not used, the property will be set to null, and unless {@code &#64;Optional} is declared
 * is not allowed.
 * </p>
 * <p>
 * The <code>&#64;UseDefaults</code> annotation can also have a value in its declaration. This value is used,
 * unless it is overridden in the assembly (see below). Java does not support generic types of annotation values,
 * so it accepts String values, which are deserialized from JSON using the ValueSerialization SPI. This allows
 * for (albeit somewhat tedious) any object type to have a default value declared on it. If the property type is
 * String, then no value deserialization is done.
 * </p>
 * <p>
 * It is also possible to change the default values for Composites during the assembly. This is done by calling the
 * {@code org.apache.polygene.bootstrap.ModuleAssembly#forMixin(Class)} method.
 * </p>
 * <p>
 * Example;
 * Let's assume that we have the following mixin type;
 *
 * <pre><code>
 * public interface SomeType
 * {
 *     &#64;UseDefaults
 *     Property&lt;String&gt; someValue();
 * }
 * </code></pre>
 * And that we want to have {@code someValue()} to be initialized to "&lt;unknown&gt;" instead of the empty string.
 * Then we need to declare the default for that with the following in the assembler.
 * <pre><code>
 * public void assemble( ModuleAssembly module )
 * {
 *     module.forMixin( SomeType.class ).declareDefaults().someValue().set( "&lt;unknown&gt;" );
 * }
 * }
 * </code></pre>
 */
@SuppressWarnings( "JavadocReference" )
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD, ElementType.FIELD } )
@Documented
public @interface UseDefaults
{
    String value() default "";
}