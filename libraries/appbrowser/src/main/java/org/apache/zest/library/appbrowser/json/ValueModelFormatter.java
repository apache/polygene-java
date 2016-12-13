/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.polygene.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.library.appbrowser.Formatter;

public class ValueModelFormatter extends AbstractJsonFormatter<ValueDescriptor,Void>
{
    public ValueModelFormatter( JSONWriter writer )
    {
        super(writer);
    }

    @Override
    public void enter( ValueDescriptor visited )
        throws JSONException
    {
        object();
        field( "type", visited.valueType().mainType().getName() );
        field( "visibility", visited.visibility().toString() );
    }

    @Override
    public void leave( ValueDescriptor visited )
        throws JSONException
    {
        endObject();
    }

    @Override
    public void visit( Void visited )
        throws JSONException
    {

    }
}
