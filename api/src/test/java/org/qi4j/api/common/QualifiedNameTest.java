package org.qi4j.api.common;

import org.junit.Test;
import org.qi4j.api.util.NullArgumentException;

import static org.junit.Assert.*;

public class QualifiedNameTest
{
    @Test
    public void testQualifiedNameWithDollar()
    {
        assertEquals( "Name containing dollar is modified", "Test-Test", new QualifiedName( TypeName.nameOf( "Test$Test" ), "test" ).type() );
    }

    @Test
    public void testQualifiedNameFromQNWithDollar()
    {
        assertEquals( "Name containing dollar is cleaned up", "Test-Test", QualifiedName.fromQN( "Test$Test:test" ).type() );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments1()
    {
        new QualifiedName( TypeName.nameOf( "Test" ), null );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments2()
    {
        new QualifiedName( null, "test" );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments3()
    {
        new QualifiedName( null, null );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments4()
    {
        QualifiedName.fromQN( null );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments5()
    {
        QualifiedName.fromMethod( null );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments6()
    {
        QualifiedName.fromClass( null, "test" );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments7()
    {
        QualifiedName.fromClass( null, null );
    }
}
