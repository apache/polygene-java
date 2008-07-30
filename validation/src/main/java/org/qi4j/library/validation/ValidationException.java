/*
 * Copyright (c) 2007, Lan Boon Ping. All Rights Reserved.
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
package org.qi4j.library.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Default exception for validation error
 */
public class ValidationException
    extends RuntimeException
{
    List<ValidationMessage> messages;

    public ValidationException( String string )
    {
        messages = new ArrayList<ValidationMessage>();
        messages.add( new ValidationMessage( string, ValidationMessage.Severity.ERROR ) );
    }

    public ValidationException( List<ValidationMessage> messages )
    {
        this.messages = messages;
    }

    public List<ValidationMessage> getMessages()
    {
        return messages;
    }

    @Override public String getMessage()
    {
        return getLocalizedMessage();
    }


    @Override public String getLocalizedMessage()
    {
        StringBuffer buf = new StringBuffer();
        for( ValidationMessage message : messages )
        {
            if( buf.length() != 0 )
            {
                buf.append( "\n" );
            }
            buf.append( message.getMessage() );
        }

        return buf.toString();
    }
}
