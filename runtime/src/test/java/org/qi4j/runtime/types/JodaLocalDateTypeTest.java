package org.qi4j.runtime.types;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JodaLocalDateTypeTest
{

    private JodaLocalDateType underTest;

    @Before
    public void setup()
    {
        underTest = new JodaLocalDateType();
    }

    @Test
    public void givenLocalDateTypeWhenConvertingToJsonExpectValidString()
        throws Exception
    {
        Object value = underTest.toJSON( new LocalDate( "2020-03-04" ) );
        assertEquals( "2020-03-04", value.toString());
    }

    @Test
    public void givenLocalDateTypeWhenConvertingFromJsonExpectValidLocalDate()
        throws Exception
    {
        Object value = underTest.fromJSON( "2020-03-04", null );
        assertEquals( new LocalDate("2020-03-04"), value);
    }


    @Test
    public void givenLocalDateTypeWhenCheckingLocalDateExpectIsDate()
        throws Exception
    {
        assertTrue( JodaLocalDateType.isDate( LocalDate.class ) );
    }

    @Test
    public void givenLocalDateTypeWhenCheckingOtherDateTypesExpectIsFalse()
        throws Exception
    {
        assertFalse( JodaLocalDateType.isDate( LocalDateTime.class ) );
        assertFalse( JodaLocalDateType.isDate( DateTime.class ) );
        assertFalse( JodaLocalDateType.isDate( java.util.Date.class ) );
        assertFalse( JodaLocalDateType.isDate( java.sql.Date.class ) );
    }
}
