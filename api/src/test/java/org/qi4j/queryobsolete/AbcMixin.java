/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.queryobsolete;

import java.util.Date;
import java.util.List;

public class AbcMixin
    implements Abc, MutableAbc
{
    private String name;
    private String city;
    private Date created;
    private List<String> defs;

    public String getName()
    {
        return name;
    }

    public String getCity()
    {
        return city;
    }

    public Date getCreated()
    {
        return created;
    }

    public List<String> getDefs()
    {
        return defs;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setCity( String city )
    {
        this.city = city;
    }

    public void setCreated( Date created )
    {
        this.created = created;
    }

    public void addDef( String def )
    {
        defs.add( def );
    }

    public void removeDef( String def )
    {
        defs.remove( def );
    }
}
