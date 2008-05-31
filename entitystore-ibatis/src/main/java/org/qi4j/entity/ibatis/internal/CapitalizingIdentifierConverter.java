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

package org.qi4j.entity.ibatis.internal;

import org.qi4j.entity.ibatis.IdentifierConverter;

/**
 * @autor Michael Hunger
 * @since 19.05.2008
 */
public class CapitalizingIdentifierConverter implements IdentifierConverter
{
    public String convertIdentifier( final String qualifiedIdentifier )
    {
        final String name = unqualify( qualifiedIdentifier );
        if (name.equalsIgnoreCase( "identity" )) return "ID";
        return name.toUpperCase();
    }

    private String unqualify( final String qualifiedIdentifier )
    {
        final int colonIndex = qualifiedIdentifier.lastIndexOf( ":" );
        if( colonIndex == -1 )
        {
            return qualifiedIdentifier;
        }
        else
        {
            return qualifiedIdentifier.substring( colonIndex + 1 );
        }
    }

}
