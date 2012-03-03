/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.api.common;

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
 * It is also possible to change the default values for Composites during the assembly. This is done by calling the
 * {@link org.qi4j.bootstrap.ModuleAssembly#forMixin(Class)} method.
 * </p>
 * <p>
 * Example;
 * Let's assume that we have the following mixin type;
 *
 * <code><pre>
 * public interface SomeType
 * {
 *     &#64;UseDefaults
 *     Property<String> someValue();
 * }
 * </pre></code>
 * And that we want to have {@code someValue()} to be initialized to "&lt;unknown&gt;" instead of the empty string.
 * Then we need to declare the default for that with the following in the assembler.
 * <code><pre>
 * public void assemble( ModuleAssembly module )
 * {
 *     module.forMixin( SomeType.class ).declareDefaults().someValue().set( "&lt;unknown&gt;" );
 * }
 * }
 * </pre></code>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD, ElementType.FIELD } )
@Documented
public @interface UseDefaults
{
}