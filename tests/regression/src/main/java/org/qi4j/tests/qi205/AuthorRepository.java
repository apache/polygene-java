package org.qi4j.tests.qi205;

import java.util.Iterator;

public interface AuthorRepository
{
    Author findBySurname( String string );

    Iterator<Author> findAllBySurname( String string );
}
