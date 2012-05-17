/*
 * Copyright 2010 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.cxf.divs;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.custommonkey.xmlunit.XMLAssert;

public class DividendsTest
{
    private static final String EXPECTED =
        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        "<soap:Body><ns1:DSDataResponse xmlns:ns1=\"http://divs.cxf.library.qi4j.org/\">" +
        "<ns1:Snapshot>" +
        "<ns1:entry>" +
        "<ns1:key>bt.l/PRIVATE_niclas</ns1:key>" +
        "<ns1:value xmlns:ns2=\"urn:qi4j:type:value:org.qi4j.library.cxf.divs\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns2:DivStream\">" +
        "<issueId>PC10YZNZC100</issueId>" +
        "<divPoints>" +
        "<ns3:anyType xmlns:ns3=\"http://cxf.apache.org/arrays\" xsi:type=\"ns2:DivPoint\">" +
        "<updateTS>" + new Date( 2010, 10, 10 ).toString() + "</updateTS>" +
        "<divTypeCD></divTypeCD>" +
        "<valCcy>USD</valCcy>" +
        "<recType>A</recType>" +
        "<val>0.708</val>" +
        "<dt>2010-02-12</dt>" +
        "<net>0.637200003862381</net>" +
        "<netCcy>USD</netCcy>" +
        "<paydate>2017-09-19</paydate>" +
        "<recDate>2014-06-16</recDate>" +
        "<divType>REG</divType>" +
        "<comment>Silly comment</comment>" +
        "<lastUpdater>niclas</lastUpdater>" +
        "</ns3:anyType>" +
        "<ns3:anyType xmlns:ns3=\"http://cxf.apache.org/arrays\" xsi:type=\"ns2:DivPoint\">" +
        "<updateTS>" + new Date( 2010, 10, 10 ).toString() + "</updateTS>" +
        "<divTypeCD></divTypeCD>" +
        "<valCcy>USD</valCcy>" +
        "<recType>A</recType>" +
        "<val>3.068</val>" +
        "<dt>2020-02-22</dt>" +
        "<net>2.7612000703811646</net>" +
        "<netCcy>USD</netCcy>" +
        "<paydate>2027-09-10</paydate>" +
        "<recDate>2024-06-26</recDate>" +
        "<divType>REG</divType>" +
        "<comment>Silly comment</comment>" +
        "<lastUpdater>niclas</lastUpdater>" +
        "</ns3:anyType>" +
        "</divPoints>" +
        "<systemUpdateTS>" + new Date( 2010, 10, 10 ).toString() + "</systemUpdateTS>" +
        "<streamName>PRIVATE_niclas</streamName>" +
        "<userUpdateTS>" + new Date( 2010, 10, 10 ).toString() + "</userUpdateTS>" +
        "<consolidate>N</consolidate>" +
        "<mdSymbol>bt.l</mdSymbol>" +
        "</ns1:value>" +
        "</ns1:entry>" +
        "</ns1:Snapshot>" +
        "</ns1:DSDataResponse>" +
        "</soap:Body>" +
        "</soap:Envelope>";

    @Test
    public void whenRequestQi4jValueExpectCorrectResult()
        throws Exception
    {
        DividendsMain.main( new String[ 0 ] );  // Start server;

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost( "http://localhost:9300/ProjectedDividendsService" );
        httpPost.setHeader( "SOAPAction", "\"DSDataRequest\"" );
        httpPost.setHeader( "Content-Type", "text/xml;charset=UTF-8" );
        HttpEntity data = new StringEntity(
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:divs=\"http://divs.cxf.library.qi4j.org/\" " +
            "xmlns:cxf=\"http://cxf.library.qi4j.org\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <divs:DSData>\n" +
            "         <!--Optional:-->\n" +
            "         <divs:RequestType>?</divs:RequestType>\n" +
            "         <!--Optional:-->\n" +
            "         <divs:DataService>?</divs:DataService>\n" +
            "         <!--Optional:-->\n" +
            "         <divs:Subscription>\n" +
            "            <!--Optional:-->\n" +
            "            <cxf:key>?</cxf:key>\n" +
            "         </divs:Subscription>\n" +
            "      </divs:DSData>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>" );
        httpPost.setEntity( data );
        HttpResponse response = httpclient.execute( httpPost );
        HttpEntity entity = response.getEntity();
        String result = "";
        if( entity != null )
        {
            InputStream instream = entity.getContent();
            InputStreamReader isr = new InputStreamReader( instream );
            BufferedReader br = new BufferedReader( isr );
            String line = br.readLine();
            while( line != null )
            {
                result = result + line;
                line = br.readLine();
            }
        }
        XMLAssert.assertXMLEqual( EXPECTED, result );
    }
}
