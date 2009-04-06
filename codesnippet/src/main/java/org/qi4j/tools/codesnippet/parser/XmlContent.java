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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instances of this class are responsible for converting the lexical tokens provided via the {@link TokenHandler}
 * interface into a xml document. The xml can be read as a stream of characters with the {@link #read(char[], int, int)}
 * method.
 * 
 * @author Georg Ragaller
 */
class XmlContent
    implements TokenHandler, Closeable
{
    // --- NOTE: the prefix and suffix patterns MUST match the appropriate expressions in the
    // javacc lexer spec.
    private static final Pattern ML_PREFIX = Pattern.compile( "^/\\*[ \t]*\\$" );
    private static final Pattern ML_SUFFIX = Pattern.compile( "\\$[ \t]*\\*/$" );
    private static final Pattern SL_PREFIX = Pattern.compile( "^//[ \t]*\\$" );
    private static final Pattern SL_SUFFIX = Pattern.compile( "\\$[ \t]*$" );
    private static final String CDATA_START = "<![CDATA[";
    private static final String CDATA_END = "]]>";
    private static final String CDATA_START_QUOTED = "]]>&lt;![CDATA[<![CDATA[";
    private static final String CDATA_END_QUOTED = "]]>]]&gt;<![CDATA[";

    // stylesheet for testing only
    private static final String STYLESHEET = (false) ? "<?xml-stylesheet type=\"text/xml\" href=\"style.xsl\"?>\n" : "";
    private StringBuilder xml = new StringBuilder( "<?xml version=\"1.0\"?>\n" + STYLESHEET + "<java>\n" );

    /**
     * Flag, which indicates if a CDATA section is opened in the xml output or not.
     */
    private boolean isCDATA;

    /**
     * The current read position for the {@link #read(char[], int, int)} method.
     */
    private int readPos;

    /*
     * (non-Javadoc)
     * 
     * @see de.classloader.jsnippy.parser.TokenHandler#appendToken(de.classloader.jsnippy.parser.Token)
     */
    public void appendToken( Token t )
    {
        if( !isCDATA )
        {
            xml.append( CDATA_START );
            isCDATA = true;
        }
        final String quoted = quoteCDATA( t.image );

        xml.append( quoted );

        if( Lexer.EOF == t.kind )
        {
            xml.append( CDATA_END );
            xml.append( "\n</java>\n" );
            isCDATA = false;
        }
    }

    /**
     * If a CDATA section start and/or end delimiter is found in the argument string, then it's quoted to appear 'as is'
     * in the result.
     * 
     * @param image the string to be quoted
     * @return the quoted argument string
     */
    private String quoteCDATA( String image )
    {
        Pattern pattern = Pattern.compile( "(" + Pattern.quote( CDATA_START ) + "|" + Pattern.quote( CDATA_END ) + ")" );

        StringBuilder result = new StringBuilder();

        Matcher matcher = pattern.matcher( image );

        int pos = 0;

        while( matcher.find( pos ) )
        {
            result.append( image.substring( pos, matcher.start() ) );
            String match = matcher.group();

            if( CDATA_START.equals( match ) )
            {
                result.append( CDATA_START_QUOTED );
            }
            else if( CDATA_END.equals( match ) )
            {
                result.append( CDATA_END_QUOTED );
            }
            else
            {
                throw new IllegalStateException();
            }
            pos = matcher.end();

        }
        result.append( image.substring( pos ) );

        return result.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.classloader.jsnippy.parser.TokenHandler#appendSpecialToken(de.classloader.jsnippy.parser.Token,
     * de.classloader.jsnippy.parser.JavaCharStream)
     */
    public void appendSpecialToken( Token t, JavaCharStream input_stream )
    {
        switch( t.kind ) {
        case Lexer.WS_SPACE:
        case Lexer.WS_HT:
        case Lexer.WS_LF:
        case Lexer.WS_CR:
        case Lexer.WS_FF:
        case Lexer.SINGLE_LINE_COMMENT:
        case Lexer.MULTI_LINE_COMMENT_XML_ERR:
        case Lexer.FORMAL_COMMENT:
        case Lexer.MULTI_LINE_COMMENT:
            appendToken( t );
            break;
        case Lexer.SINGLE_LINE_COMMENT_XML:
            appendSingleLineCommentXml( t );
            break;
        case Lexer.MULTI_LINE_COMMENT_XML:
            appendMultiLineCommentXml( t );
            break;
        default:
            // --- must never happen, until someone changes the lexer spec.
            throw new IllegalArgumentException( "unknown SPECIAL_TOKEN: " + t.kind );
        }
    }

    /**
     * Handle the java multiline comment with embedded xml.
     */
    private void appendMultiLineCommentXml( Token t )
    {
        assert (t.kind == Lexer.MULTI_LINE_COMMENT_XML);

        if( isCDATA )
        {
            xml.append( CDATA_END );
            isCDATA = false;
        }

        String stripped = stripComment( t.image, ML_PREFIX, ML_SUFFIX );

        xml.append( stripped );
    }

    /**
     * Handle the java singleline comment with embedded xml.
     */
    private void appendSingleLineCommentXml( Token t )
    {
        assert (t.kind == Lexer.SINGLE_LINE_COMMENT_XML);

        if( isCDATA )
        {
            xml.append( CDATA_END );
            isCDATA = false;
        }

        String stripped = stripComment( t.image, SL_PREFIX, SL_SUFFIX );

        xml.append( stripped );
    }

    /**
     * Strip the prefix and suffix given as regex patterns from the comment string.
     * 
     * @param comment the coment to be stripped
     * @param prefix the prefix pattern
     * @param suffix the suffix pattern
     * @return the stripped comment, i.e. the embedded xml
     */
    private String stripComment( String comment, Pattern prefix, Pattern suffix )
    {
        Matcher mlp = prefix.matcher( comment );

        if( !mlp.find() )
        {
            throw new IllegalStateException( "unexpected pattern" );
        }

        int start = mlp.end();

        Matcher mls = suffix.matcher( comment );

        if( !mls.find() )
        {
            throw new IllegalStateException( "unexpected pattern" );
        }
        int end = mls.start();

        return comment.substring( start, end );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Closeable#close()
     */
    public void close()
    {
        xml = new StringBuilder( 0 );
    }

    /**
     * Read characters into a portion of an array. This method will block until some input is available, an I/O error
     * occurs, or the end of the stream is reached.
     * 
     * @param dest destination buffer
     * @param off offset at which to start storing characters
     * @param len maximum number of characters to read
     * 
     * @return the number of characters read, or -1 if the end of the stream has been reached
     * 
     */
    public int read( char[] dest, int off, int len )
    {
        int available = xml.length() - readPos;

        if( 0 < available )
        {
            int read = Math.min( available, len );

            xml.getChars( readPos, readPos + read, dest, off );
            readPos += read;
            return read;
        }
        else
        {
            return -1;
        }
    }

    /**
     * The currently available xml content.
     */
    @Override
    public String toString()
    {
        return xml.substring( readPos );
    }

}
