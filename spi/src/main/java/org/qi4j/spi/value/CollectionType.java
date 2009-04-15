/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.value;

import java.lang.reflect.Type;
import java.util.*;

import org.qi4j.api.util.Classes;
import org.qi4j.api.common.TypeName;
import static org.qi4j.api.common.TypeName.nameOf;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.entity.SchemaVersion;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.util.PeekableStringTokenizer;

/**
 * JAVADOC
 */
public class CollectionType
    implements ValueType
{
    public static boolean isCollection( Type type)
    {
        Class cl = Classes.getRawClass( type );
        return cl.equals( Collection.class ) || cl.equals( List.class ) || cl.equals( Set.class );
    }

    private TypeName type;
    private ValueType collectedType;

    public CollectionType( TypeName type, ValueType collectedType )
    {
        this.type = type;
        this.collectedType = collectedType;
    }

    public TypeName type()
    {
        return type;
    }

    public ValueType collectedType()
    {
        return collectedType;
    }

    public void versionize( SchemaVersion schemaVersion )
    {
        schemaVersion.versionize( type );
        collectedType.versionize( schemaVersion );
    }

    @Override public String toString()
    {
        return type +"<"+ collectedType +">";
    }

    public void toJSON(Object value, StringBuilder json, Qi4jSPI spi)
    {
        json.append('[');

        Collection collection = (Collection) value;
        String comma = "";
        for (Object collectionValue : collection)
        {
            json.append(comma);
            collectedType.toJSON(collectionValue, json, spi);
            comma=",";
        }

        json.append("]");
    }

    public Object fromJSON(PeekableStringTokenizer json, Module module)
    {
        String token = json.nextToken("[");

        Collection coll;
        if (type.isClass(List.class))
            coll = new ArrayList();
        else
            coll = new LinkedHashSet();

        token = json.peekNextToken("]{\"");
        while (!token.equals("]"))
        {
            coll.add(collectedType.fromJSON(json, module));
            token = json.nextToken(",]");
        }

        return coll;
    }
}
