/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.model.properties;

/**
 * Enum for genders
 */
public enum GenderType
{
    MALE( "Male" ), FEMALE( "Female" );

    private String description;

    private GenderType( String description )
    {
        this.description = description;
    }

    @Override public String toString()
    {
        return this.description;
    }

    public static final GenderType toEnum( String text )
    {
        try
        {
            return valueOf( text.toUpperCase() );
        }
        catch( IllegalArgumentException iae )
        {
            // TODO
        }

        return null;
    }

    public static GenderType parse( String genderType )
    {
        if( MALE.toString().equalsIgnoreCase( genderType ) )
        {
            return MALE;
        }
        else if( FEMALE.toString().equalsIgnoreCase( genderType ) )
        {
            return FEMALE;
        }
        else
        {
            throw new IllegalArgumentException( "Unknown genderType " + genderType );
        }
    }
}
