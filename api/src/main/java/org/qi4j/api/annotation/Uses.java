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
 * This annotation is to inject a type-safe reference back to other
 * MixinTypes of the current composite.
 * <p/>
 * Example;
 * <pre><code>
 * public class MyFunkyMixin
 *     implements Funky
 * {
 *     @Uses Music meAsMusic;
 *    :
 * }
 * </code></pre>
 * This mixin requires access to a Music mixin in the same composite. So, if
 * the composite that this mixin belongs to doesn't also includes a Music
 * mixin, there will be an error.
 * <p/>
 * Optionality is also possiple;
 * <pre><code>
 * public class MyFunkyMixin
 *     implements Funky
 * {
 *     @Uses(optional=true) Music meAsMusic;
 *    :
 * }
 * </code></pre>
 * Here the <code>meAsMusic</code> reference will be null if the mixin does not belong to
 * a composite which contains a Music mixin. It is expected that the mixin code will
 * know how to handle such sitution.
 * <p/>
 * It is important to understand that the reference marked by the @Uses annotation is
 * fully type-safe and can not be cast to other types of the same composite. In fact,
 * the object assigned in the reference will not necessarily be the same proxy, although
 * the same <code>InvocationHandler</code> is used under the hood.
 * <p/>
 * This is illegal;
 * <pre><code>
 *
 * @ImplementedBy( MyStateMixin.class )
 * public interface MyComposite extends Composite, SessionState, SomeStuff, AnotherStuff
 * {}
 * <p/>
 * public class MyStateMixin
 * implements SessionState
 * {
 * @Uses SomeStuff meAsSomeStuff;
 * <p/>
 * public void doSomething()
 * {
 * AnotherStuff another = (AnotherStuff) meAsSomeStuff;  // <--- Runtime Exception.
 * }
 * }
 * </code></pre>
 * @Uses can be used all <i>fragments</i>, both mixins and modifiers.
 * <p/>
 * <p/>
 * It is a recommended naming convention to use the <code>"meAs" + type</code> style, to
 * clearly indicate what it means.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.PARAMETER } )
@Documented
public @interface Uses
{
    boolean optional() default false; // If the usage cannot be resolved, only fail if this is false
}
