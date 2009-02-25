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

package org.qi4j.runtime.entity.association;

import java.lang.reflect.Type;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.common.QualifiedName;

/**
 * JAVADOC
 */
public final class EntityBuilderAssociation<T>
    implements Association<T>
{
    private final AssociationModel model;
    private T associated;

    public EntityBuilderAssociation( AssociationModel aModel )
    {
        model = aModel;
    }

    public T get()
    {
        return associated;
    }

    public void set( T associated ) throws IllegalArgumentException
    {
        this.associated = associated;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return model.metaInfo( infoType );
    }

    public QualifiedName qualifiedName()
    {
        return model.qualifiedName();
    }

    public Type type()
    {
        return model.type();
    }

    public boolean isImmutable()
    {
        return false;
    }

    public boolean isAggregated()
    {
        return false;
    }
}