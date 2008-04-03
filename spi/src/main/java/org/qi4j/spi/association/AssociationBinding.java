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

package org.qi4j.spi.association;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;
import org.qi4j.association.AssociationInfo;
import org.qi4j.spi.composite.AssociationResolution;

/**
 * TODO
 */
public final class AssociationBinding
    implements AssociationInfo
{
    private AssociationResolution associationResolution;
    private Map<Class, Serializable> associationInfo;

    public AssociationBinding( AssociationResolution associationResolution, Map<Class, Serializable> associationInfo )
    {
        this.associationInfo = associationInfo;
        this.associationResolution = associationResolution;
    }

    public AssociationResolution getAssociationResolution()
    {
        return associationResolution;
    }

    public <T> T metaInfo( Class<T> infoClass )
    {
        return infoClass.cast( associationInfo.get( infoClass ) );
    }

    public String name()
    {
        return associationResolution.getAssociationModel().getName();
    }

    public String qualifiedName()
    {
        return associationResolution.getAssociationModel().getQualifiedName();
    }

    public Type type()
    {
        return associationResolution.getAssociationModel().getType();
    }

    @Override public String toString()
    {
        return associationResolution.toString();
    }
}
