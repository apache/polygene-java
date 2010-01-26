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

/**
 * JAVADOC
 */
public final class ManyAssociationType
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final QualifiedName qualifiedName;
    private final String type;
    private final boolean queryable;

    public ManyAssociationType( final QualifiedName qualifiedName,
                                final String type,
                                final boolean queryable
    )
    {
        this.qualifiedName = qualifiedName;
        this.type = type;
        this.queryable = queryable;
    }

    public QualifiedName qualifiedName()
    {
        return qualifiedName;
    }

    public String type()
    {
        return type;
    }

    public boolean queryable()
    {
        return queryable;
    }

    @Override
    public String toString()
    {
        return qualifiedName + "(" + type + ")";
    }
}
