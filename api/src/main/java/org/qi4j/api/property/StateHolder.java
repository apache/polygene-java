/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.property;

import java.lang.reflect.Method;
import org.qi4j.api.common.QualifiedName;

/**
 * This represents the state of a composite (properties).
 */
public interface StateHolder
{
    /**
     * Get a property for a specific accessor method
     *
     * @param propertyMethod of the property
     *
     * @return the property
     */
    <T> Property<T> getProperty( Method propertyMethod );

    /**
     * Get a property for a specific accessor method
     *
     * @param name The qualified name of the property
     *
     * @return the property
     */
    <T> Property<T> getProperty( QualifiedName name );

    void visitProperties( StateVisitor visitor );

    public interface StateVisitor
    {
        void visitProperty( QualifiedName name, Object value );
    }
}
