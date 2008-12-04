/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.library.spaces;

public class ConcurrentEntryModificationException extends SpaceException
{
    private String identity;

    public ConcurrentEntryModificationException( String id )
    {
        super( "Entry currently being modified: " + id, null );
        identity = id;
    }

    public String getIdentity()
    {
        return identity;
    }
}
