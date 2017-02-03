package org.apache.polygene.api.type;

import java.time.LocalDate;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HasTypesPredicatesTest
{
    @Test
    public void hasEqualTypePredicate()
    {
        assertTrue( new HasEqualType<>( Integer.class )
                        .test( ValueType.of( Integer.class ) ) );
        assertTrue( new HasEqualType<>( Integer.class )
                        .test( ValueType.of( String.class, Integer.class ) ) );
        assertFalse( new HasEqualType<>( Number.class )
                         .test( ValueType.of( String.class, Integer.class ) ) );
        assertFalse( new HasEqualType<>( String.class )
                         .test( ValueType.of( LocalDate.class, Integer.class ) ) );

        assertTrue( new HasEqualType<>( ValueType.of( Integer.class ) )
                        .test( ValueType.of( Integer.class ) ) );
        assertTrue( new HasEqualType<>( ValueType.of( Integer.class ) )
                        .test( ValueType.of( String.class, Integer.class ) ) );
        assertFalse( new HasEqualType<>( ValueType.of( Number.class ) )
                         .test( ValueType.of( String.class, Integer.class ) ) );
        assertFalse( new HasEqualType<>( ValueType.of( String.class ) )
                         .test( ValueType.of( LocalDate.class, Integer.class ) ) );
    }

    @Test
    public void hasAssignableTypePredicate()
    {
        assertTrue( new HasAssignableFromType<>( Number.class )
                        .test( ValueType.of( String.class, Integer.class ) ) );
        assertFalse( new HasAssignableFromType<>( Integer.class )
                         .test( ValueType.of( Integer.class ) ) );
        assertFalse( new HasAssignableFromType<>( String.class )
                         .test( ValueType.of( LocalDate.class, Integer.class ) ) );
    }

    @Test
    public void hasEqualOrAssignablePredicate()
    {
        assertTrue( new HasEqualOrAssignableFromType<>( Number.class )
                        .test( ValueType.of( String.class, Integer.class ) ) );
        assertTrue( new HasEqualOrAssignableFromType<>( Integer.class )
                        .test( ValueType.of( Integer.class ) ) );
        assertFalse( new HasEqualOrAssignableFromType<>( String.class )
                         .test( ValueType.of( LocalDate.class, Integer.class ) ) );
    }
}
