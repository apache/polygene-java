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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A xml filter to strip of whitespaces in character data at the start of each line, i.e. shorten the indent.
 * 
 * @author Georg Ragaller
 */
public class IndentStripFilter extends XMLFilterImpl
{
    private static final String WS_START = "^[ \t]";
    private static final Pattern INDENT = Pattern.compile( WS_START + "*" );

    private final String element;
    private int nesting;
    private Pattern indent;
    private StringBuilder pending;

    /**
     * @param element the name of the element, whose nested character data should be stripped of its indent.
     */
    public IndentStripFilter( String element )
    {
        this(element,null);
    }

    /**
     * @param element the name of the element, whose nested character data should be stripped of its indent.
     * @param parent the parent XML reader.
     */
    public IndentStripFilter( String element, XMLReader parent)
    {
        super(parent);
        this.element = element;
    }

    @Override
    public void startElement( String uri, String localName, String name, Attributes atts )
        throws SAXException
    {
        if( 0 < nesting )
        {
            flushPendingCharacters();
        }
        super.startElement( uri, localName, name, atts );

        if( element.equals( localName ) )
        {
            ++nesting;
        }

    }

    @Override
    public void endElement( String uri, String localName, String name )
        throws SAXException
    {
        if( 0 < nesting )
        {
            flushPendingCharacters();
        }
        if( element.equals( localName ) )
        {
            --nesting;
        }

        super.endElement( uri, localName, name );
    }

    @Override
    public void characters( char[] ch, int start, int length )
        throws SAXException
    {
        if( 0 < nesting )
        {
            if( null == pending )
            {
                pending = new StringBuilder( length * 2 );
            }
            pending.append( ch, start, length );
        }
        else
        {
            super.characters( ch, start, length );
        }
    }

    @Override
    public void ignorableWhitespace( char[] ch, int start, int length )
        throws SAXException
    {
        if( 0 < nesting )
        {
            flushPendingCharacters();
            String stripped = strip( new String( ch, start, length ) );
            super.characters( stripped.toCharArray(), 0, stripped.length() );
        }
        else
        {
            super.ignorableWhitespace( ch, start, length );
        }
    }

    /**
     * Generate a <tt>characters</tt> event with the buffered contiguous character data if necessary.
     * 
     * @throws SAXException
     */
    private void flushPendingCharacters()
        throws SAXException
    {
        if( null != pending )
        {
            String stripped = strip( pending );
            super.characters( stripped.toCharArray(), 0, stripped.length() );
            pending = null;
        }
    }

    /**
     * Strips off the indent of the given character sequence.
     * <p>
     * The indent is calculated by with {@link #guessIndent(char[], int, int)}.
     * </p>
     * 
     * @param chars The character sequence to be examined
     * @return the examined character date with the indent removed
     */
    private String strip( CharSequence chars )
    {
        if( null == indent )
        {
            indent = guessIndent( chars );
        }
        String stripped = stripIndent( chars );
        return stripped;
    }

    /**
     * Strips off the indent of the given character sequence.
     * <p>
     * The indent must already be calculated and stored in {@link #indent}.
     * </p>
     * 
     * @param chars The character sequence to be examined
     * @return the examined character date with the indent removed
     */
    private String stripIndent( CharSequence chars )
    {
        assert (null != indent);

        Matcher matcher = indent.matcher( chars );
        return matcher.replaceAll( "" );
    }

    /**
     * Guess the indent from the given character data.
     * <p>
     * The indent is defined as the number of preceding whitespace characters in the first line inside the considered
     * character data.
     * </p>
     * <p>
     * <b>Note:</b> A more exact way of determining the "best" indent, would require a 2-pass logic, where all the lines
     * are observed, and the minimum indent of all lines is used.
     * </p>
     * 
     * @param chars The character sequence to be examined
     * @return a pattern describing the indent
     */
    private Pattern guessIndent( CharSequence chars )
    {
        Matcher matcher = INDENT.matcher( chars );
        int len = (matcher.find() ? matcher.group().length() : 0);

        return Pattern.compile( WS_START + "{" + len + "}", Pattern.MULTILINE );
    }
}
