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
package org.apache.polygene.library.uid.uuid;

import org.junit.Test;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.uid.uuid.assembly.UuidServiceAssembler;
import org.apache.polygene.test.AbstractPolygeneTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UuidServiceTest extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        new UuidServiceAssembler().assemble(module);
        module.transients( UnderTestComposite.class );
    }

    @Test
    public void whenGeneratingUuidGivenHashLengthLargerThanZeroThenValidateTheSizeAndContent()
        throws Exception
    {
        UnderTest ut = transientBuilderFactory.newTransient( UnderTest.class );
        for( int hashLength = 1; hashLength < 50; hashLength++ )
        {
            String uid = ut.generateUuid( hashLength );
            assertEquals( hashLength * 2, uid.length() );
            for( int i = 0; i < uid.length(); i++ )
            {
                char ch = uid.charAt( i );
                assertTrue( ( ch >= '0' && ch <= '9' ) || ( ch >= 'A' && ch <= 'F' ) );
            }
        }
    }

    @Test
    public void whenGeneratingUuidGivenZeroHashLengthThenValidateFormat()
        throws Exception
    {
        UnderTest ut = transientBuilderFactory.newTransient( UnderTest.class );
        String uid = ut.generateUuid( 0 );
        int dashCounter = 0;
        for( int i = 0; i < uid.length(); i++ )
        {
            char ch = uid.charAt( i );
            if( ch == '-' )
            {
                dashCounter++;
            }
            else
            {
                assertTrue( ( ch >= '0' && ch <= '9' ) || ( ch >= 'A' && ch <= 'F' ) );
            }
        }
        assertEquals( 5, dashCounter );
    }

    @Mixins( UnderTestMixin.class )
    public interface UnderTestComposite extends UnderTest, TransientComposite
    {
    }

    public interface UnderTest
    {
        String generateUuid( int len );
    }

    public static class UnderTestMixin
        implements UnderTest
    {
        @Service private UuidService service;

        public String generateUuid( int len )
        {
            return service.generateUuid( len );
        }
    }
}
