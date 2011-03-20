package org.qi4j.runtime.types;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JodaLocalDateTimeTypeTest
{

    private JodaLocalDateTimeType underTest;

    @Before
    public void setup()
    {
        underTest = new JodaLocalDateTimeType();
    }

    @Test
    public void givenLocalDateTypeWhenConvertingToJsonExpectValidString()
        throws Exception
    {
        Object value = underTest.toJSON( new LocalDateTime( "2020-03-04T13:23:00", DateTimeZone.UTC ) );
        assertEquals( "2020-03-04T13:23:00.000", value.toString());
    }

    @Test
    public void givenLocalDateTypeWhenConvertingFromJsonExpectValidLocalDate()
        throws Exception
    {
        Object value = underTest.fromJSON( "2020-03-04T12:23:09", null );
        assertEquals( new LocalDateTime("2020-03-04T12:23:09", DateTimeZone.UTC ), value);
    }


    @Test
    public void givenLocalDateTypeWhenCheckingLocalDateExpectIsDate()
        throws Exception
    {
        assertTrue( JodaLocalDateTimeType.isDate( LocalDateTime.class ) );
    }

    @Test
    public void givenLocalDateTypeWhenCheckingOtherDateTypesExpectIsFalse()
        throws Exception
    {
        assertFalse( JodaLocalDateTimeType.isDate( LocalDate.class ) );
        assertFalse( JodaLocalDateTimeType.isDate( DateTime.class ) );
        assertFalse( JodaLocalDateTimeType.isDate( java.util.Date.class ) );
        assertFalse( JodaLocalDateTimeType.isDate( java.sql.Date.class ) );
    }
}
