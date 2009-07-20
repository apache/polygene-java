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

package org.qi4j.spi.entity.association;

import java.io.Serializable;
import org.qi4j.api.common.QualifiedName;
import static org.qi4j.api.common.TypeName.*;
import org.qi4j.spi.entity.SchemaVersion;
import org.qi4j.spi.entity.StateName;

/**
 * JAVADOC
 */
public final class ManyAssociationType
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final QualifiedName qualifiedName;
    private final String type;
    private final String rdf;
    private final boolean queryable;
    private StateName stateName;

    public ManyAssociationType( final QualifiedName qualifiedName,
                                final String type,
                                final String rdf,
                                final boolean queryable )
    {
        this.qualifiedName = qualifiedName;
        this.type = type;
        this.rdf = rdf;
        this.queryable = queryable;

        SchemaVersion schemaVersion = new SchemaVersion();
        schemaVersion.versionize( type );
        schemaVersion.versionize( qualifiedName );
        stateName = new StateName( qualifiedName, rdf, schemaVersion.base64() );
    }

    public QualifiedName qualifiedName()
    {
        return qualifiedName;
    }

    public String type()
    {
        return type;
    }

    public String rdf()
    {
        return rdf;
    }

    public boolean queryable()
    {
        return queryable;
    }


    public StateName stateName()
    {
        return stateName;
    }

    @Override public String toString()
    {
        return qualifiedName + "(" + type + ")";
    }

    public void versionize( SchemaVersion schemaVersion )
    {
        schemaVersion.versionize( qualifiedName );
        schemaVersion.versionize( nameOf( type ) );
    }
}
