/*
 * Copyright (c) 2007-2011, Rickard Öberg. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.api.property;

/**
 * Properties are declared in Composite interfaces by using this interface.
 * <p>
 * It creates a first-class object for the property from which you can get and set the value, and access any
 * metadata about it.
 * </p>
 * <p>The type of the Property can be one of the following:</p>
 * <ul>
 * <li> A boxed primitive (Long,Integer,Boolean, etc.)</li>
 * <li> String</li>
 * <li> BigInteger</li>
 * <li> BigDecimal</li>
 * <li> Date</li>
 * <li> DateTime (Joda Time)</li>
 * <li> LocalDateTime (Joda Time)</li>
 * <li> Money (Joda Money)</li>
 * <li> BigMoney (Joda Money)</li>
 * <li> A serializable</li>
 * <li> A ValueComposite</li>
 * <li> A List, Set or Collection of any of the above</li>
 * </ul>
 *
 * @param <T> Parameterized type of the Property
 */
public interface Property<T>
{
    /**
     * Get the value of the property.
     *
     * @return the value
     */
    T get();

    /**
     * Set the value of the property
     *
     * @param newValue the new value
     *
     * @throws IllegalArgumentException if the value has an invalid value
     * @throws IllegalStateException    if the property is immutable
     */
    void set( T newValue )
        throws IllegalArgumentException, IllegalStateException;
}
