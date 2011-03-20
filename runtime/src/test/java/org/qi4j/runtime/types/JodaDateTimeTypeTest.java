package org.qi4j.runtime.types;

import org.apache.log4j.helpers.ISO8601DateFormat;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JodaDateTimeTypeTest
{

    private JodaDateTimeType underTest;

    @Before
    public void setup()
    {
        underTest = new JodaDateTimeType();
    }

    @Test
    public void givenLocalDateTypeWhenConvertingToJsonExpectValidString()
        throws Exception
    {
        Object value = underTest.toJSON( new DateTime("2020-03-04T13:24:35", DateTimeZone.UTC ) );
        assertEquals( "2020-03-04T13:24:35.000Z", value.toString());
    }

    @Test
    public void givenLocalDateTypeWhenConvertingFromJsonExpectValidLocalDate()
        throws Exception
    {
        Object value = underTest.fromJSON( "2020-03-04T12:23:33Z", null );
        assertEquals( new DateTime("2020-03-04T12:23:33Z", DateTimeZone.UTC ), value);
    }


    @Test
    public void givenLocalDateTypeWhenCheckingLocalDateExpectIsDate()
        throws Exception
    {
        assertTrue( JodaDateTimeType.isDate( DateTime.class ) );
    }

    @Test
    public void givenLocalDateTypeWhenCheckingOtherDateTypesExpectIsFalse()
        throws Exception
    {
        assertFalse( JodaDateTimeType.isDate( LocalDateTime.class ) );
        assertFalse( JodaDateTimeType.isDate( LocalDate.class ) );
        assertFalse( JodaDateTimeType.isDate( java.util.Date.class ) );
        assertFalse( JodaDateTimeType.isDate( java.sql.Date.class ) );
    }
}
