/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.api.association;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.structure.MetaInfoHolder;

/**
 * Association Descriptor.
 */
public interface AssociationDescriptor extends MetaInfoHolder
{
    /**
     * Get the qualified name of the association. This is constructed by
     * concatenating the name of the declaring interface with the name
     * of the method, using ":" as separator.
     * <p>
     * Example:
     * </p>
     * <p>
     * com.somecompany.MyInterface with association method
     * </p>
     * <pre><code>
     * Association&lt;String&gt; someAssociation();
     * </code></pre>
     * will have the qualified name:
     * <pre><code>
     * com.somecompany.MyInterface:someAssociation
     * </code></pre>
     *
     * @return the qualified name of the association
     */
    QualifiedName qualifiedName();

    /**
     * Get the type of the associated Entities
     *
     * @return the type of the associated Entities
     */
    Type type();

    AccessibleObject accessor();

    boolean queryable();

    boolean isImmutable();

    boolean isAggregated();
}
