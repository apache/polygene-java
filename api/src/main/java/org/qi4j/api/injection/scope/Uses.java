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
package org.qi4j.api.injection.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.qi4j.api.injection.InjectionScope;

/**
 * Annotation to denote the injection of a dependency to be used by a Mixin. The injected
 * object is provided by the ValueBuilder, TransientBuilder or ObjectBuilder.
 * Call {@link org.qi4j.api.composite.TransientBuilder#use} to provide the instance
 * to be injected.
 *
 * The injection can be done in a couple of ways:
 * <ul>
 * <li>Direct type injection: @Uses SomeType someInstance;
 * <li>Iterable type injection. This will inject a ValueBuilder, ObjectBuilder or TransientBuilder for the type: @Uses Iterable<SomeType> someIterable;
 * <li>Builder type injection. This will inject a ValueBuilder, ObjectBuilder or TransientBuilder for the type: @Uses ObjectBuilder<SomeType> someBuilder;
 * </ul>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.PARAMETER, ElementType.FIELD } )
@Documented
@InjectionScope
public @interface Uses
{
}