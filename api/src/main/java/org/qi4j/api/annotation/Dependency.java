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
 * Annotation to denote the injection of a dependency into a Fragment (a Modifier or Mixin).
 * <p/>
 * Dependency resolution is handled through the DependencyResolver, which allows any kind
 * of injection. The org.qi4j.spi.DefaultDependencyResolver handles CompositeBuilderFactory and
 * CompositeModelFactory. The @Dependency annotation tells the runtime to find an provider for
 * the type of that field.
 * <p/>
 * Example;
 * <pre><code>
 * public class MyBeerOrderMixin
 *     implements BeerOrder
 * {
 *     @Dependency CompositeBuilderFactory factory;
 * <p/>
 *     public Beer moreBeer()
 *     {
 *         return factory.newInstance( BeerComposite.class );
 *     }
 * }
 * </code></pre>
 * <p/>
 * <p/>
 * Dependencies can also be optional;
 * <pre><code>
 * public class MyBeerOrderModifier
 *     implements BeerOrder
 * {
 *     @Dependency( optional=true ) Auditor auditor;
 *     @Modifier BeerOrder next;
 * <p/>
 *     public Beer moreBeer()
 *     {
 *         Beer beer = next.moreBeer();
 *         if( auditor != null )
 *         {
 *             auditor.purchased( beer.getDescription(), beer.getPrice() );
 *         }
 *     }
 * }
 * </code></pre>
 * <p/>
 * If the @Dependency needs to be more specific than the type of the field,
 * it is possible to provide a value that is DependencyResolver implementation
 * specific. For instance;
 * <pre><code>
 * public class MyBeerOrder
 *     implements BeerOrder
 * {
 *     @Dependency( "id=1123" ) Fridge fridge;
 * <p/>
 *    :
 * }
 * </code></pre>
 *
 * @see org.qi4j.api.DependencyResolver
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.PARAMETER } )
@Documented
public @interface Dependency
{
    String value() default ""; // This name can be used for lookups

    boolean optional() default false; // If the dependency cannot be resolved, only fail if this is false
}
