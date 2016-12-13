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
package org.apache.polygene.api.concern.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.polygene.api.injection.InjectionScope;

/**
 * This annotation is required once in each Concern, to mark the
 * field where the next element in the call sequence should be
 * injected.
 * <p>
 * The type of the field must be of the same type as the Concern
 * itself, or an InvocationHandler.
 * </p>
 * <p>
 * Example;
 * </p>
 * <pre><code>
 * public interface MyStuff
 * {
 *     void doSomething();
 * }
 *
 * public class MyStuffConcern
 *     implements MyStuff
 * {
 *     &#64;ConcernFor MyStuff next;
 *
 *     public void doSomething()
 *     {
 *         // HERE DO THE MODIFIER STUFF.
 *
 *         // Delegate to the underlying mixin/modifier.
 *         next.doSomething();
 *     }
 * }
 * </code></pre>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.PARAMETER } )
@Documented
@InjectionScope
public @interface ConcernFor
{
}