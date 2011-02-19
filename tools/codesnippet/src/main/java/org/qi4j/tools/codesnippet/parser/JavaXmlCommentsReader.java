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

import java.io.IOException;
import java.io.Reader;

/**
 * <p>
 * A {@link Reader} implementation, which reads a java source file with XML embedded in special comments and delivers a
 * XML stream with the java source embedded.
 * </p>
 * <p>
 * See the package documentation for an <a href="package-summary.html#example">example</a>.
 * </p>
 */
public class JavaXmlCommentsReader
    extends Reader
{
    private final Lexer lexer;
    private final XmlContent tokenBuffer = new XmlContent();
    // --- NOTE: works only because javac's lexical token (kind) constants are >= 0
    private int kind = -1;

    public JavaXmlCommentsReader( Reader javaSource )
    {
        this.lexer = new Lexer( javaSource, tokenBuffer );
    }

    @Override
    public void close()
        throws IOException
    {
        lexer.close();
        tokenBuffer.close();
    }

    @Override
    public int read( char[] cbuf, int off, int len )
        throws IOException
    {
        if( -1 == kind || kind != Lexer.EOF )
        {
            Token tok = lexer.getNextToken();
            kind = tok.kind;
        }

        return tokenBuffer.read( cbuf, off, len );
    }
}
