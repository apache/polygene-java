package org.qi4j.api.annotation.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
 *     @SideEffectFor MyStuff next;
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
@DependencyScope
public @interface SideEffectFor
{
}
