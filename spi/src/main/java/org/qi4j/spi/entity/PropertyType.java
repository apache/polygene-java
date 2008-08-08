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

package org.qi4j.spi.entity;

import java.io.Serializable;
import org.qi4j.property.GenericPropertyInfo;

/**
 * TODO
 */
public class PropertyType
    implements Serializable
{
    private String qualifiedName;
    private String type;

    public PropertyType( String qualifiedName, String type )
    {
        this.qualifiedName = qualifiedName;
        this.type = type;
    }

    public String qualifiedName()
    {
        return qualifiedName;
    }

    public String type()
    {
        return type;
    }

    public String toURI()
    {
        return GenericPropertyInfo.toURI( qualifiedName );
    }

    @Override public String toString()
    {
        return qualifiedName + "(" + type + ")";
    }
}
