package org.qi4j.runtime.types;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.json.JSONDeserializer;
import org.qi4j.api.json.JSONObjectSerializer;
import org.qi4j.api.type.ValueType;

import static org.junit.Assert.assertEquals;

public class JodaLocalDateTimeTypeTest
{

    private ValueType underTest;

    @Before
    public void setup()
    {
        underTest = new ValueType( LocalDateTime.class );
    }

    @Test
    public void givenLocalDateTypeWhenConvertingToJsonExpectValidString()
        throws Exception
    {
        JSONObjectSerializer serializer = new JSONObjectSerializer();
        serializer.serialize( new LocalDateTime( "2020-03-04T13:23:00", DateTimeZone.UTC ), underTest );
        Object value = serializer.getRoot();
        assertEquals( "2020-03-04T13:23:00.000", value.toString());
    }

    @Test
    public void givenLocalDateTypeWhenConvertingFromJsonExpectValidLocalDate()
        throws Exception
    {
        Object value = new JSONDeserializer( null ).deserialize( "2020-03-04T12:23:09", underTest );
        assertEquals( new LocalDateTime("2020-03-04T12:23:09", DateTimeZone.UTC ), value);
    }
}
