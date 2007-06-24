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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Modifiers that implement InvocationHandler and which should only
 * be applied to methods that have a particular annotation or implements
 * a known interface should use this annotation.
 * <p/>
 * Example:
 * <pre><code>
 * @AppliesTo( Sessional.class )
 * public class SessionModifier
 *     implements InvocationHandler
 * {
 *     public Object invoke( Object proxy, Method method, Object[] args )
 *         throws Throwable
 *     {
 *         ... do session stuff ...
 *     }
 * }
 *
 * @Retention( RetentionPolicy.RUNTIME )
 * @Target( ElementType.METHOD )
 * @Documented
 * @Inherited
 * public @interface Sessional
 * {
 * }
 *
 * public class MyStateMixin
 *     implements SessionState
 * {
 *     private State state;
 *
 *     @Sessional
 *     public void setSomeState( State state )
 *     {
 *         this.state = state;
 *     }
 *
 *     @Sessional
 *     public State getSomeState()
 *     {
 *         return this.state;
 *     }
 *
 *     public void setStateService( StateService service )
 *     {
 *         this.service = service;
 *     }
 *
 *     public StateService getStateService()
 *     {
 *         return this.service;
 *     }
 * }
 *
 * public interface SessionState
 * {
 *     State getSomeState();
 *     void setSomeState( State state );
 * }
 *
 * @ModifiedBy( SessionModifier.class )
 * @ImplementedBy( MyStateMixin.class )
 * public interface MyComposite extends Composite, SessionState
 * {}
 * </code></pre>
 * The setStateService and getStateService methods does not have the @Sessional annotation,
 * and will therefor the SessionModifier will not be placed into the call sequence of these
 * methods, and the other way around.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
@Documented
@Inherited
public @interface AppliesTo
{
    Class value();
}
