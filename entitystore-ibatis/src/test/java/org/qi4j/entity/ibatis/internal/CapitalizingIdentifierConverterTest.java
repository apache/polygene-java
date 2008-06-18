package org.qi4j.entity.ibatis.internal;

import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import java.util.Map;
import java.util.HashMap;

/**
 * @author mh14 @ jexp.de
 * @since 11.06.2008 03:18:24 (c) 2008 jexp.de
 */
public class CapitalizingIdentifierConverterTest
{
    private final CapitalizingIdentifierConverter converter = new CapitalizingIdentifierConverter();

    @Test public void convertToUpperCase() {
        assertEquals("identity -> ID","ID", converter.convertIdentifier( "identity" ));
        assertEquals("uppercase ABC","ABC", converter.convertIdentifier( "abc" ));
        assertEquals("removed qualified prefix","ABC", converter.convertIdentifier( "aaa:abc" ));
        assertEquals("removed qualified prefixes","ABC", converter.convertIdentifier( "aaa:bbb:abc" ));
    }
    @Test public void removeFromMap() {
        Map<String,Object> rawData = new HashMap<String,Object>();
        rawData.put("ABC","test");
        assertEquals("converted key and found value","test", converter.getValueFromData( rawData, "abc" ));
        assertEquals( "entry removed" , 0, rawData.size());
    }
    @Test public void nullIfNotFound() {
        Map<String,Object> rawData = new HashMap<String,Object>();
        assertEquals("converted key and found value",null, converter.getValueFromData( rawData, "abc" ));
    }

    @Test public void convertMapKeys() {
        Map<String,Object> rawData = new HashMap<String,Object>();
        rawData.put("abc","test1");
        rawData.put("DEF","test2");
        rawData.put("aaa:GHI","test3");
        rawData.put("aaa:bbb:JKL","test4");
        final Map<String, Object> convertedData = converter.convertKeys( rawData );
        assertEquals( "all entries remained" ,4,convertedData.size());
        assertEquals("converted key and found value ABC","test1",convertedData.get("ABC"));
        assertEquals("converted key and found value DEF","test2",convertedData.get("DEF"));
        assertEquals("converted key and found value GHI","test3",convertedData.get("GHI"));
        assertEquals("converted key and found value JKL","test4",convertedData.get("JKL"));
    }
    @Test(expected = IllegalArgumentException.class)
    public void failDuplicateKeys() {
        Map<String,Object> rawData = new HashMap<String,Object>();
        rawData.put("abc","test1");
        rawData.put("ABC","test2");
        converter.convertKeys( rawData );
    }
    @Test(expected = IllegalArgumentException.class)
    public void failDuplicateQualfiedKeys() {
        Map<String,Object> rawData = new HashMap<String,Object>();
        rawData.put("abc","test1");
        rawData.put("aaa:abc","test2");
        converter.convertKeys( rawData );
    }
}
