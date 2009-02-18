/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.library.jini.importer.org.qi4j.library.jini.tests;

import java.util.Stack;
import java.io.IOException;
import org.wonderly.jini2.PersistentJiniService;
import net.jini.config.ConfigurationException;

public class InterpreterServiceImpl extends PersistentJiniService
    implements InterpreterService
{
    private Stack<Entry> stack = new Stack<Entry>();

    public InterpreterServiceImpl( String[] arguments )
        throws IOException, ConfigurationException
    {
        super( arguments );
    }

    public void push( long value )
    {
        stack.push( new Entry( value ) );
    }

    public void push( double value )
    {
        stack.push( new Entry( value ) );
    }

    public long popLong()
    {
        Entry entry = stack.pop();
        return entry.value.longValue();
    }

    public double popDouble()
    {
        Entry entry = stack.pop();
        return entry.value.doubleValue();
    }

    public void addition()
    {
        Entry entry1 = stack.pop();
        Entry entry2 = stack.pop();
        if( entry1.type == EntryType.llong )
        {
            if( entry2.type != EntryType.llong )
                throw new IllegalExpressionException();
            final long result = entry2.value.longValue() + entry1.value.longValue();
            stack.push( new Entry( result ) );
        }
        else
        {
            if( entry2.type != EntryType.ddouble )
                throw new IllegalExpressionException();
            final double result = entry2.value.doubleValue() + entry1.value.doubleValue();
            stack.push( new Entry( result ) );
        }
    }

    public void subtraction()
    {
        Entry entry1 = stack.pop();
        Entry entry2 = stack.pop();
        if( entry1.type == EntryType.llong )
        {
            if( entry2.type != EntryType.llong )
                throw new IllegalExpressionException();
            final long result = entry2.value.longValue() - entry1.value.longValue();
            stack.push( new Entry( result ) );
        }
        else
        {
            if( entry2.type != EntryType.ddouble )
                throw new IllegalExpressionException();
            final double result = entry2.value.doubleValue() - entry1.value.doubleValue();
            stack.push( new Entry( result ) );
        }
    }

    public void multiplication()
    {
        Entry entry1 = stack.pop();
        Entry entry2 = stack.pop();
        if( entry1.type == EntryType.llong )
        {
            if( entry2.type != EntryType.llong )
                throw new IllegalExpressionException();
            final long result = entry2.value.longValue() * entry1.value.longValue();
            stack.push( new Entry( result ) );
        }
        else
        {
            if( entry2.type != EntryType.ddouble )
                throw new IllegalExpressionException();
            final double result = entry2.value.doubleValue() * entry1.value.doubleValue();
            stack.push( new Entry( result ) );
        }
    }

    public void division()
    {
        Entry entry1 = stack.pop();
        Entry entry2 = stack.pop();
        if( entry1.type == EntryType.llong )
        {
            if( entry2.type != EntryType.llong )
                throw new IllegalExpressionException();
            final long result = entry2.value.longValue() / entry1.value.longValue();
            stack.push( new Entry( result ) );
        }
        else
        {
            if( entry2.type != EntryType.ddouble )
                throw new IllegalExpressionException();
            final double result = entry2.value.doubleValue() / entry1.value.doubleValue();
            stack.push( new Entry( result ) );
        }
    }

    public void modulo()
    {
        Entry entry1 = stack.pop();
        Entry entry2 = stack.pop();
        if( entry1.type == EntryType.llong )
        {
            if( entry2.type != EntryType.llong )
                throw new IllegalExpressionException();
            final long result = entry2.value.longValue() % entry1.value.longValue();
            stack.push( new Entry( result ) );
        }
        else
        {
            throw new IllegalExpressionException();
        }
    }

    private class Entry
    {
        EntryType type;
        Number value;

        public Entry( double value )
        {
            type = EntryType.ddouble;
            this.value = value;
        }

        public Entry( long value )
        {
            type = EntryType.llong;
            this.value = value;
        }
    }

    private enum EntryType
    {
        ddouble, llong
    }
}
