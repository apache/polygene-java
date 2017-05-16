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
 * Annotation to denote that something is optional.
 * <ul>
 * <li>
 * If applied to a method parameter, then the value is allowed to be null. Default
 * is that method parameters have to be non-null.
 * </li>
 * <li>
 * If applied to a Property declaration, then the value may be null after construction of
 * the instance, or may be set to null at a later time.
 * </li>
 * <li>
 * If applied to an injected member field, it is allowed tha none get injected. For instance, an <code>&#64;Optional
 * &#64;Service</code> would allow a service to not have been declared and the field will be null.
 * </li>
 * </ul>
 * <p>
 * Optionality is not the default in Polygene, and if injections, property values and parameters in methods are not
 * non-null, the Polygene runtime will throw an {@link org.apache.polygene.api.constraint.ConstraintViolationException}, indicating
 * which field/property/parameter in which composite and mixin the problem has been detected.
 * </p>
 * <p>
 * Example;
 * </p>
 * <pre><code>
 * &#64;Optional &#64;Service
 * MyService service;   // If no MyService instance is declared and visible to this service injection point
 *                      // the 'service' field will be null.
 *
 * &#64;Service
 * YourService other;   // If no YourService instance is declared and visible to this service injection point
 *                      // the Polygene runtime will throw a ConstraintViolationException.
 *
 * </code></pre>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD } )
@Documented
public @interface Optional
{
}