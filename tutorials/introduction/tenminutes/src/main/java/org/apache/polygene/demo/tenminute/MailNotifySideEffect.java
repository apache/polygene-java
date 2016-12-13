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

package org.apache.polygene.demo.tenminute;

import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.sideeffect.SideEffectOf;
import org.apache.polygene.elsewhere.mail.MailService;

// START SNIPPET: allClass
public abstract class MailNotifySideEffect extends SideEffectOf<Confirmable>
    implements Confirmable
{
    @Service
    private MailService mailer;

    @This
    private HasLineItems hasItems;

    @This
    private HasCustomer hasCustomer;

    @Override
    public void confirm()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "An Order has been made.\n\n\n" );
        builder.append( "Customer:" );
        builder.append( hasCustomer.name().get() );
        builder.append( "\n\nItems ordered:\n" );
        for( LineItem item : hasItems.lineItems().get() )
        {
            builder.append( item.name().get() );
            builder.append( " : " );
            builder.append( item.quantity().get() );
            builder.append( "\n" );
        }
        mailer.send( "sales@mycompany.com", builder.toString() );
    }
}
// END SNIPPET: allClass
