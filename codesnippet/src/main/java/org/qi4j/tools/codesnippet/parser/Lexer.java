/*
 * Copyright 2009 Georg Ragaller. All rights reserved.
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
package org.qi4j.tools.codesnippet.parser;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

/**
 * @author Georg Ragaller
 */
class Lexer extends JavaXmlCommentsTokenManager
    implements Closeable
{
    final TokenHandler out;

    public Lexer( Reader stream, TokenHandler tokenHandler )
    {
        super( new JavaCharStream( stream ) );
        out = tokenHandler;
    }

    @Override
    void CommonTokenAction( Token t )
    {
        if( null != t.specialToken )
        {
            // Walk back the special token chain until the first
            // special token after the previous regular token is reached.
            Token tmp = t.specialToken;
            while( null != tmp.specialToken )
                tmp = tmp.specialToken;
            // Walk the special token chain in the forward
            // direction and append them to the token handler.
            while( null != tmp )
            {
                out.appendSpecialToken( tmp, input_stream );
                tmp = tmp.next;
            }
        }
        out.appendToken( t );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Closeable#close()
     */
    public void close()
        throws IOException
    {
        if( null != input_stream && null != input_stream.inputStream )
        {
            input_stream.inputStream.close();
            input_stream = null;
        }
    }

}
