/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** TODO
 * 
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.PARAMETER } )
@Documented
public @interface DependsOn
{
    Scope value() default Scope.service;

    String name() default ""; // This name can be used for lookups

    boolean optional() default false; // If the dependency cannot be resolved, only fail if this is false

    enum Scope
    {
        // Fragment scopes
        service, // DependencyOld is provided by DependencyResolver
        this_as, // Composite must implement the type

        // Mixin scopes
        adapt,   // CompositeBuilder.adapt must be called with this type
        decorate, // CompositeBuilder.decorate must be called with this type. Mixin and Composite must implement the type

        // Modifier scopes
        composite // Provides access to method AnnotationElement, the Composite class
    }
}
