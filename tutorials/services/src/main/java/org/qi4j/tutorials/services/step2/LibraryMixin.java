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
package org.qi4j.tutorials.services.step2;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

public class LibraryMixin
    implements Library
{
    @Structure
    ValueBuilderFactory factory;

    @Override
    public Book borrowBook( String author, String title )
    {
        ValueBuilder<Book> builder = factory.newValueBuilder( Book.class );
        Book prototype = builder.prototype();
        prototype.author().set( author );
        prototype.title().set( title );
        Book book = builder.newInstance();
        System.out.println( "Book borrowed: " + book.title().get() + " by " + book.author().get() );
        return book;
    }

    @Override
    public void returnBook( Book book )
    {
        System.out.println( "Book returned: " + book.title().get() + " by " + book.author().get() );
    }
}