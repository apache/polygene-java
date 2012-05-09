/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.http;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.junit.Before;
import org.junit.BeforeClass;

import org.qi4j.test.AbstractQi4jTest;

public abstract class AbstractJettyTest
        extends AbstractQi4jTest
{

    protected static final int HTTP_PORT = 8041;
    protected HttpClient defaultHttpClient;
    protected ResponseHandler<String> stringResponseHandler = new ResponseHandler<String>()
    {

        public String handleResponse( HttpResponse hr )
                throws ClientProtocolException, IOException
        {
            return EntityUtils.toString( hr.getEntity(), "UTF-8" );
        }

    };

    @BeforeClass
    public static void beforeJettyTestClass()
    {
        // Be sure that no test trigger a DNS cache, needed by VirtualHosts test plumbing
        Security.setProperty( "networkaddress.cache.ttl", "0" );
    }

    @Before
    public void before()
            throws GeneralSecurityException, IOException
    {
        // Default HTTP Client
        defaultHttpClient = new DefaultHttpClient();
    }

}
