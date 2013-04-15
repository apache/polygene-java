package org.qi4j.manual.recipes.properties;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

public class BookFactory
{
// START SNIPPET: create
    @Structure
    Module module;
// END SNIPPET: create

    public Book create()
    {
// START SNIPPET: create
        TransientBuilder<Book> builder = module.newTransientBuilder( Book.class );
        Book prototype = builder.prototype();
        prototype.title().set( "The Death of POJOs" );
        prototype.author().set( "Niclas Hedhman" );
        Book book = builder.newInstance();
        String title = book.title().get();     // Retrieves the title.
        book.title().set( "Long Live POJOs" ); // throws an IllegalStateException
// END SNIPPET: create
        return book;
    }
}
