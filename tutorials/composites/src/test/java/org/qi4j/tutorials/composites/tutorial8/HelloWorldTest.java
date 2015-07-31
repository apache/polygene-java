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
package org.qi4j.tutorials.composites.tutorial8;

import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class HelloWorldTest
{
    HelloWorldComposite helloWorld;

    @Before
    public void setUp()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( HelloWorldComposite.class );
            }
        };
        TransientBuilderFactory builderFactory = assembly.module();
        TransientBuilder<HelloWorldComposite> builder = builderFactory.newTransientBuilder( HelloWorldComposite.class );
        builder.prototype().name().set( "Hello" );
        builder.prototype().phrase().set( "World" );
        helloWorld = builder.newInstance();
    }

    @Test
    public void givenHelloWorldWhenSetPropertiesAndSayThenReturnCorrectResult()
    {
        {
            helloWorld.phrase().set( "Hey" );
            helloWorld.name().set( "Universe" );
            String result = helloWorld.say();
            assertThat( result, equalTo( "Hey Universe" ) );
        }
    }

    @Test
    public void givenHelloWorldWhenSetInvalidPhraseThenThrowException()
    {
        try
        {
            helloWorld.phrase().set( null );
            fail( "Should not be allowed to set phrase to null" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }

        try
        {
            helloWorld.phrase().set( "" );
            fail( "Should not be allowed to set phrase to empty string" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }
    }

    @Test
    public void givenHelloWorldWhenSetInvalidNameThenThrowException()
    {
        try
        {
            helloWorld.name().set( null );
            fail( "Should not be allowed to set name to null" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }

        try
        {
            helloWorld.name().set( "" );
            fail( "Should not be allowed to set name to empty string" );
        }
        catch( IllegalArgumentException e )
        {
            // Ok
        }
    }
}
