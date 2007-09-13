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

/**
 * This annotation is required once in each modifier, to mark the
 * field where the next element in the call sequence should be
 * written.
 * <p/>
 * The type of the field must be of the same type as the modifier
 * itself, or an InvocationHandler.
 * <p/>
 * If the modifier is an InvocationHandler and the field marked
 * by this annotation is not an InvocationHandler, then TODO:??? WHAT ???
 * <p/>
 * <p/>
 * Example;
 * <pre><code>
 * public interface MyStuff
 * {
 *     void doSomething();
 * }
 * <p/>
 * public class MyStuffModifier
 *     implements MyStuff
 * {
 *     @Modifier MyStuff next;
 * <p/>
 *     public void doSomething()
 *     {
 *         // HERE DO THE MODIFIER STUFF.
 * <p/>
 *         // Delegate to the underlying mixin/modifier.
 *         next.doSomething();
 *     }
 * }
 * </code></pre>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.PARAMETER } )
@Documented
@DependencyScope
public @interface Modifies
{
}
