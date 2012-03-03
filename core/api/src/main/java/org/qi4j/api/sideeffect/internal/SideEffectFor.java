/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.sideeffect.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.qi4j.api.injection.InjectionScope;

/**
 * This annotation is required once in each SideEffect, to mark the
 * field where the element providing the invocation result should be
 * injected.
 * <p/>
 * The type of the field must be of the same type as the SideEffect
 * itself, or an InvocationHandler.
 * <p/>
 * <p/>
 * Example;
 * <pre><code>
 * public interface MyStuff
 * {
 *     SomeResult doSomething();
 * }
 * <p/>
 * public class MyStuffSideEffect
 *     implements MyStuff
 * {
 *     &#64;SideEffectFor MyStuff next;
 * <p/>
 *     public SomeResult doSomething()
 *     {
 *          SomeResult result = next.doSomething();
 * <p/>
 *         // HERE DO THE SIDEEFFECT STUFF.
 * <p/>
 *          return result; // Result value is ignored, null would work too.
 *     }
 * }
 * </code></pre>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.PARAMETER } )
@Documented
@InjectionScope
public @interface SideEffectFor
{
}
