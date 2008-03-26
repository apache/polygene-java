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
package org.qi4j.queryobsolete.value;

import java.util.Date;
import java.util.Map;
import org.qi4j.queryobsolete.Expression;

public final class DateValueExpression
    implements Expression, ValueExpression
{
    private Date value;

    public DateValueExpression( Date value )
    {
        this.value = value;
    }

    public Date getValue( Object candidate, Map<String, Object> variables )
    {
        return value;
    }

    public String toString()
    {
        return "Date[" + value + "]";
    }
}
