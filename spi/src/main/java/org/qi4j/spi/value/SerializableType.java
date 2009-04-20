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

import org.qi4j.api.common.TypeName;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.SchemaVersion;
import org.qi4j.spi.util.Base64Encoder;
import org.qi4j.spi.util.PeekableStringTokenizer;

import java.io.*;

/**
 * JAVADOC
 */
public class SerializableType
    implements ValueType
{
    private final TypeName type;

    public SerializableType( TypeName type )
    {
        this.type = type;
    }

    public TypeName type()
    {
        return type;
    }

    public void versionize( SchemaVersion schemaVersion )
    {
        schemaVersion.versionize( type );
    }

    public void toJSON( Object value, StringBuilder json, Qi4jSPI spi )
    {
        // Serialize value
        try
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream( bout );
            out.writeUnshared( value );
            out.close();
            String stringValue = new String( Base64Encoder.encode( bout.toByteArray(), true ), "UTF-8" );
            json.append( stringValue );
        }
        catch( IOException e )
        {
            throw new IllegalArgumentException( "Could not serialize value", e );
        }

    }

    public Object fromJSON( PeekableStringTokenizer json, Module module )
    {
        try
        {
            byte[] bytes = json.nextToken().getBytes( "UTF-8" );
            bytes = Base64Encoder.decode( bytes );
            ByteArrayInputStream bin = new ByteArrayInputStream( bytes );
            ObjectInputStream oin = new ObjectInputStream( bin );
            Object result = oin.readObject();
            oin.close();
            return result;
        }
        catch( IOException e )
        {
            throw new IllegalStateException( "Could not deserialize value", e );
        }
        catch( ClassNotFoundException e )
        {
            throw new IllegalStateException( "Could not find class for serialized value", e );
        }
    }

    @Override public String toString()
    {
        return type.toString();
    }
}
