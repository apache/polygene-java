/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.sample.rental.web;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface QuikitContext
{

    Page page();

    String methodName();

    Document dom();

    Element element();

    Element parentElement();

    /**
     * Returns the request path, with the mountpoint removed.
     *
     * @return The request path.
     */
    String path();

    /**
     * Returns the queryString from the URL.
     *
     * @return the query string of the URL, i.e. the content after the question mark "?".
     */
    String queryString();

    /**
     * Returns the header from the request.
     *
     * @param headerKey the name of the header.
     *
     * @return the Header value of the named header.
     */
    String getHeader( String headerKey );

    /**
     * Returns the data of the request.
     *
     * @return the data part of the request.
     */
    byte[] data()
        throws RenderException;

    void setDynamic( String method, Element element, Element parent );
}
