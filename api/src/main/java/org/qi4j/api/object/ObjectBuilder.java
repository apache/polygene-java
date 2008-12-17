/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.api.object;

import org.qi4j.api.common.ConstructionException;

/**
 * TODO for Rickard; Explanation needed on how to use for Templating, Strategy and Builder patterns.
 */
public interface ObjectBuilder<T>
    extends Iterable<T>
{
    /**
     * Make the given objects available for @Uses injection.
     * <p>
     * These objects will be injected to the @Uses fields and arguments, when the
     * object is created (constructor injection) or just has been created (field injection).
     * </p>
     * <p>
     * It is possible to provide more objects in the <code>use()</code> method than is actually
     * required. Any additional objects will be discarded before the Application.activate() method
     * is called, and eventually garbage collected.
     * </p>
     *
     * @param objects the objects to be used
     * @return builder for objects
     */
    ObjectBuilder<T> use( Object... objects );

    /**
     * Creates a new instance from this ObjectBuilder.
     *
     * @return An object of type <code><i>T</i></code>.
     * @throws org.qi4j.api.common.ConstructionException
     *          If the object class is not compatible with the
     */
    T newInstance()
        throws ConstructionException;

    /**
     * Inject an existing instance. Only fields and methods will be called.
     *
     * @param instance
     * @throws ConstructionException
     */
    void injectTo( T instance )
        throws ConstructionException;
}