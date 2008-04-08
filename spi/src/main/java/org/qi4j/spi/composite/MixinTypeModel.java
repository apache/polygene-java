/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.composite;

/**
 * TODO Add JavaDoc.
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, April 07, 2008
 */
public final class MixinTypeModel
    implements Comparable<MixinTypeModel>
{

    /**
     * Get URI for a mixin type class.
     *
     * @param mixinTypeClass mixin type class
     * @return mixin type URI
     */
    public static String toURI( final Class mixinTypeClass )
    {
        if( mixinTypeClass == null )
        {
            return null;
        }
        String className = mixinTypeClass.getName();
        className = className.replace( '$', '&' );
        return "urn:qi4j:" + className;
    }

    /**
     * Mixin type class.
     */
    private final Class mixinTypeClass;

    /**
     * Constructor.
     *
     * @param mixinTypeClass mixin type class; cannot be null
     */
    MixinTypeModel( final Class mixinTypeClass )
    {
        this.mixinTypeClass = mixinTypeClass;
    }

    /**
     * Get mixin type class.
     *
     * @return mixin type class
     */
    public Class getMixinType()
    {
        return mixinTypeClass;
    }

    /**
     * Get mixin type URI.
     *
     * @return mixin type URI
     */
    public String toURI()
    {
        return toURI( mixinTypeClass );
    }

    @Override public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        MixinTypeModel that = (MixinTypeModel) o;

        if( mixinTypeClass != null )
        {
            return mixinTypeClass.equals( that.mixinTypeClass );
        }
        else
        {
            return that.mixinTypeClass == null;
        }
    }

    @Override public int hashCode()
    {
        return ( mixinTypeClass != null ? mixinTypeClass.hashCode() : 0 );
    }

    @Override public String toString()
    {
        return mixinTypeClass.toString();
    }

    public int compareTo( final MixinTypeModel mixinTypeModel )
    {
        return this.mixinTypeClass.getName().compareTo( mixinTypeModel.mixinTypeClass.getName() );
    }
}