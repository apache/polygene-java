/*
 * Copyright (c) 2009, Tony Kohar. All Rights Reserved.
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
package org.qi4j.envisage.event;

import java.awt.Cursor;
import java.util.EventObject;
import javax.swing.text.html.HTMLEditorKit;

public final class LinkEvent
    extends EventObject
{
    public static final Cursor LINK_CURSOR = new HTMLEditorKit().getLinkCursor();

    protected final Object object;

    public LinkEvent( Object source, Object object )
    {
        super( source );
        this.object = object;
    }

    public Object getObject()
    {
        return object;
    }
}
