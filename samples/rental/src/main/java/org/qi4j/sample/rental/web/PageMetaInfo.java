/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.sample.rental.web;

import java.io.Serializable;

public class PageMetaInfo
    implements Serializable
{
    final static long serialVersionUID = 1L;

    private String mountPoint;

    public PageMetaInfo( String mountPoint )
    {
        while( mountPoint.startsWith( "/" ) )
        {
            mountPoint = mountPoint.substring( 1 );
        }
        while( mountPoint.endsWith( "/" ) )
        {
            mountPoint = mountPoint.substring( 0, mountPoint().length() - 1 );
        }
        this.mountPoint = "/" + mountPoint;
    }

    public String mountPoint()
    {
        return mountPoint;
    }
}
