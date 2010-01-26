/*
 * Copyright (c) 2008, Michael Hunger. All Rights Reserved.
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

package org.qi4j.entitystore.qrm.internal;

import static java.lang.String.format;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.entitystore.qrm.IdentifierConverter;

public class CapitalizingIdentifierConverter
    implements IdentifierConverter
{
    public String convertIdentifier( final QualifiedName qualifiedIdentifier )
    {
        final String name = qualifiedIdentifier.name();
        if( name.equalsIgnoreCase( "identity" ) )
        {
            return "ID";
        }
        return name.toUpperCase();
    }

    public Object getValueFromData( final Map<String, Object> rawData, final QualifiedName qualifiedName )
    {
        String convertedIdentifier = convertIdentifier( qualifiedName );
        if( rawData.containsKey( convertedIdentifier ) )
        {
            return rawData.remove( convertedIdentifier );
        }
        return null;
    }

    public Map<String, Object> convertKeys( Map<QualifiedName, Object> rawData )
    {
        Map<String, Object> result = new HashMap<String, Object>( rawData.size() );
        for( Map.Entry<QualifiedName, Object> entry : rawData.entrySet() )
        {
            final String convertedIdentifier = convertIdentifier( entry.getKey() );
            if( result.containsKey( convertedIdentifier ) )
            {
                throw new IllegalArgumentException( format( "Duplicate Key: %s -> %s", entry.getKey(), convertedIdentifier ) );
            }
            result.put( convertedIdentifier, entry.getValue() );
        }
        return result;
    }
}
