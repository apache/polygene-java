/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.serialization;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.spi.entity.QualifiedIdentity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Serializable state for a single entity. This includes the version
 * of the state and the version of the type.
 */
public final class SerializableState
    implements Serializable
{
    private static final long serialVersionUID = 3L;

    private final QualifiedIdentity identity;
    private final long entityVersion;
    private final long lastModified;
    private final Map<QualifiedName, Object> properties;
    private final Map<QualifiedName, QualifiedIdentity> associations;
    private final Map<QualifiedName, Collection<QualifiedIdentity>> manyAssociations;

    public SerializableState( QualifiedIdentity identity,
                              long entityVersion, long lastModified,
                              Map<QualifiedName, Object> properties,
                              Map<QualifiedName, QualifiedIdentity> associations,
                              Map<QualifiedName, Collection<QualifiedIdentity>> manyAssociations )
    {
        this.identity = identity;
        this.entityVersion = entityVersion;
        this.lastModified = lastModified;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
    }

    public QualifiedIdentity qualifiedIdentity()
    {
        return identity;
    }

    public long version()
    {
        return entityVersion;
    }

    public long lastModified()
    {
        return lastModified;
    }

    public Map<QualifiedName, Object> properties()
    {
        return properties;
    }

    public Map<QualifiedName, QualifiedIdentity> associations()
    {
        return associations;
    }

    public Map<QualifiedName, Collection<QualifiedIdentity>> manyAssociations()
    {
        return manyAssociations;
    }
}
