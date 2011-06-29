package org.qi4j.runtime.types;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.common.TypeName;
import org.qi4j.spi.property.JSONDeserializer;
import org.qi4j.spi.property.JSONObjectSerializer;
import org.qi4j.spi.property.ValueType;

import static org.junit.Assert.assertEquals;

public class JodaDateTimeTypeTest
{

    private ValueType underTest;

    @Before
    public void setup()
    {
        underTest = new ValueType( DateTime.class );
    }

    @Test
    public void givenLocalDateTypeWhenConvertingToJsonExpectValidString()
        throws Exception
    {
        JSONObjectSerializer serializer = new JSONObjectSerializer();
        serializer.serialize( new DateTime( "2020-03-04T13:24:35", DateTimeZone.UTC ), underTest );
        Object value = serializer.getRoot();
        assertEquals( "2020-03-04T13:24:35.000Z", value.toString());
    }

    @Test
    public void givenLocalDateTypeWhenConvertingFromJsonExpectValidLocalDate()
        throws Exception
    {
        Object value = new JSONDeserializer( null ).deserialize( "2020-03-04T12:23:33Z", underTest );
        assertEquals( new DateTime("2020-03-04T12:23:33Z", DateTimeZone.UTC ), value);
    }
}
