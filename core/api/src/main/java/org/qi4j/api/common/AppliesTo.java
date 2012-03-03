/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.qi4j.api.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fragments that implement InvocationHandler and which should only be applied to methods that have a particular
 * annotation or implement a known interface should use this annotation.
 * <p>
 * &#64;AppliesTo can specify one of;
 * <ul>
 * <li>An annotation,</li>
 * <li>An interface,</li>
 * <li>An AppliesToFilter implementation.</li>
 * </ul>
 * </p>
 * <p>
 * Example with annotation:
 * </p>
 * <pre><code>
 *
 * &#64;AppliesTo( Sessional.class )   // Tells Qi4j to apply this concern on methods with &#64;Sessional annotation
 * public class SessionConcern extends GenericConcern
 * {
 *     public Object invoke( Object proxy, Method method, Object[] args )
 *         throws Throwable
 *     {
 *         ... do session stuff ...
 *     }
 * }
 *
 * &#64;Retention( RetentionPolicy.RUNTIME )
 * &#64;Target( ElementType.METHOD )
 * &#64;Documented
 * &#64;Inherited
 * public @interface Sessional
 * {
 * }
 *
 * public class MyMixin
 *     implements My
 * {
 *     &#64;Sessional
 *     public void doSomethingSessional()
 *     {
 *        // ... do your logic wrapped in a session
 *     }
 *
 *     public void doSomethingWithoutSession()
 *     {
 *        // ... do stuff that are not wrapped in session.
 *     }
 * }
 *
 * public interface My
 * {
 *     void doSomethingSessional();
 *
 *     void doSomethingWithoutSession();
 * }
 *
 * &#64;Concerns( SessionConcern.class )
 * &#64;Mixins( MyMixin.class )
 * public interface MyComposite extends My, TransientComposite
 * {}
 * </code></pre>
 * <p>
 * The doSomethingWithoutSession method do not have the &#64;Sessional annotation, therefore the SessionConcern will
 * not be placed into the call sequence of these methods, and
 * vice-versa. The &#64;Sessional annotation can be placed either on the interface method or the implementation
 * method, depending on whether it is a contract or implementation detail.
 * </p>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Documented
public @interface AppliesTo
{
    /**
     * List of interfaces, annotations or AppliesToFilter
     * implementation classes.
     * If one of them matches the current element it will be
     * accepted, so this list can be considered an "or".
     *
     * @return array of classes or interfaces to be used by the filter
     */
    Class<?>[] value();
}
