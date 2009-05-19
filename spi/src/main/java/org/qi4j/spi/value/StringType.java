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
import org.qi4j.spi.util.PeekableStringTokenizer;

import java.lang.reflect.Type;

/**
 * String type
 */
public class StringType
        extends AbstractStringType
{
    public static boolean isString(Type type)
    {
        if (type instanceof Class)
        {
            Class typeClass = (Class) type;
            return (typeClass.equals(String.class));
        }
        return false;
    }

    public StringType()
    {
        super(TypeName.nameOf(String.class));
    }

    public void toJSON(Object value, StringBuilder json)
    {
        json.append('"');
        String stringValue = value.toString();
        int len = stringValue.length();
        for (int i = 0; i < len; i++)
        {
            char ch = stringValue.charAt(i);
            // Escape characters properly
            switch (ch)
            {
                case '"':
                    json.append('\\').append('"');
                    break;
                case '\\':
                    json.append('\\').append('\\');
                    break;
                case '/':
                    json.append('\\').append('/');
                    break;
                case '\b':
                    json.append('\\').append('b');
                    break;
                case '\f':
                    json.append('\\').append('f');
                    break;
                case '\n':
                    json.append('\\').append('n');
                    break;
                case '\r':
                    json.append('\\').append('r');
                    break;
                case '\t':
                    json.append('\\').append('t');
                    break;

                default:
                    json.append(ch);
            }
        }
        json.append('"');
    }

    public Object fromJSON(PeekableStringTokenizer json, Module module)
    {
        String token = json.nextToken("\"");

        StringBuilder builder = new StringBuilder();

        String result = json.peekNextToken("\\\"");

        if (result.equals("\""))
        {
            json.nextToken();
            return "";
        }

        while (!(token = json.nextToken()).equals("\""))
        {
            if (token.charAt(0) == '\\')
            {
                result = json.nextToken();

                char controlChar = result.charAt(0);
                // Unescape characters properly
                switch (controlChar)
                {
                    case '"':
                        builder.append('"');
                        break;
                    case '\\':
                        builder.append('\\');
                        break;
                    case '/':
                        builder.append('/');
                        break;
                    case 'b':
                        builder.append('\b');
                        break;
                    case 'f':
                        builder.append('\f');
                        break;
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;

                    default:
                        throw new IllegalStateException("Illegal control character in string:"+controlChar);
                }

                if (result.length() > 1)
                    builder.append(result.substring(1));
            } else
            {
                builder.append(token);
            }
        }

        return builder.toString();
    }

    @Override
    public String toQueryParameter(Object value)
    {
        return value == null ? null : value.toString();
    }

    @Override
    public Object fromQueryParameter(String parameter, Module module)
    {
        return parameter;
    }
}