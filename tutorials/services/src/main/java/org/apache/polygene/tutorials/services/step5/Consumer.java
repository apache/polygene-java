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

package org.apache.polygene.tutorials.services.step5;

import org.apache.polygene.api.injection.scope.Service;

/**
 * Simple service consumer. The service is injected using the @Service annotation.
 */
public class Consumer
{
    @Service
    Library library;

    public void run()
    {
        Book book = library.borrowBook( "Eric Evans", "Domain Driven Design" );
        System.out.println( "Consumer got book: " + book.title().get() + " by " + book.author().get() );
        library.returnBook( book );
    }
}