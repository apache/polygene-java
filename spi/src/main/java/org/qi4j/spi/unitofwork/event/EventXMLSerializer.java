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

package org.qi4j.spi.unitofwork.event;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Serialize entity events to XML using SAX
 */
public final class EventXMLSerializer
{
    AttributesImpl atts = new AttributesImpl();

    public void toXML( UnitOfWorkEvent event, ContentHandler hd ) throws SAXException
    {

        if( event instanceof EntityEvent )
        {
            EntityEvent entityEvent = (EntityEvent) event;

            atts.addAttribute( "", "", "on", "CDATA", entityEvent.identity().identity() );

            if( entityEvent instanceof EntityStateEvent )
            {

                EntityStateEvent entityStateEvent = (EntityStateEvent) entityEvent;
                atts.addAttribute( "", "", "for", "CDATA", entityStateEvent.stateName().qualifiedName().toURI() );

                if( entityStateEvent instanceof SetPropertyEvent )
                {
                    SetPropertyEvent setPropertyEvent = (SetPropertyEvent) entityStateEvent;
                    String value = setPropertyEvent.value();
                    hd.startElement( "", "", "event", atts );
                    hd.characters( value.toCharArray(), 0, value.length() );
                    hd.endElement( "", "", "event" );
                }
            }
        }
    }
}