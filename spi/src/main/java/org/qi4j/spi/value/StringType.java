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
import org.qi4j.spi.util.PeekableStringTokenizer;

import java.lang.reflect.Type;

/**
 * Boolean type
 */
public class StringType
    extends ValueType
{
    public static boolean isString( Type type )
    {
        if( type instanceof Class )
        {
            Class typeClass = (Class) type;
            return ( typeClass.equals( String.class ) );
        }
        return false;
    }

    private final TypeName type;

    public StringType( TypeName type )
    {
        this.type = type;
    }

    public void versionize( SchemaVersion schemaVersion )
    {
        schemaVersion.versionize( type );
    }

    public TypeName type()
    {
        return type;
    }

    public void toJSON( Object value, StringBuilder json, Qi4jSPI spi )
    {
        json.append( '"' );
        String stringValue = value.toString();
        int len = stringValue.length();
        for( int i = 0; i < len; i++ )
        {
            char ch = stringValue.charAt( i );
            // Escape characters properly
            switch( ch )
            {
            case '"':
                json.append( '\\' ).append( '"' );
                break;
            case '\\':
                json.append( '\\' ).append( '\\' );
                break;

            // TODO Control characters

            default:
                json.append( ch );
            }
        }
        json.append( '"' );
    }

    public Object fromJSON( PeekableStringTokenizer json, Module module )
    {
        String token = json.nextToken( "\"" );
        String result = json.nextToken();

        // Empty String
        if( result.equals( "\"" ) )
        {
            return "";
        }

        // TODO unescaping

        token = json.nextToken();
        return result;
    }

    @Override public String toString()
    {
        return type.toString();
    }
}