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
package org.apache.polygene.runtime.concerns;

import java.util.ArrayList;
import java.util.List;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.AssemblyReportException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

//TODO 2.0 removed this possibility when simplifying the Property handling. So, we are now checking that a decent
// exception is thrown, but should be changed to supported instead.
public class PropertyInheritanceTest extends AbstractPolygeneTest
{

    private boolean failed;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( Audit.class );
    }

    @Override
    protected void assemblyException( AssemblyException exception )
        throws AssemblyException
    {
        if( exception instanceof AssemblyReportException )
        {
            failed = true;
            return;
        }
        super.assemblyException( exception );
    }

    @Test
    public void givenConcernOnInheritedPropertyWhenAccessingPropertyExpectConcernToBeCalled()
    {
        assertThat( failed, equalTo( true ) );

// TODO: The following test code is testing the feature once it has been implemented. The @Test is needed to ensure check for the right Exception.
//        List<String> data = new ArrayList<String>();
//        data.add( "First" );
//        data.add( "Second" );
//        data.add( "Third" );
//        Audit audit = module.newTransient( Audit.class );
//        AuditTrail trail = audit.trail();
//        trail.set( data );
//        assertThat( audit.trail().get().get( 0 ), equalTo( "1: First" ) );
//        assertThat( audit.trail().get().get( 1 ), equalTo( "2: Second" ) );
//        assertThat( audit.trail().get().get( 2 ), equalTo("3: Third"));
    }

    public static abstract class AuditTrailMarkupConcern extends ConcernOf<AuditTrail>
        implements AuditTrail
    {

        @Override
        public void set( List<String> newValue )
            throws IllegalArgumentException, IllegalStateException
        {
            List<String> markedUp = new ArrayList<String>();
            int counter = 0;
            for( String value : newValue )
            {
                markedUp.add( counter++ + ": " + value );
            }
            next.set( markedUp );
        }
    }

    @Concerns( AuditTrailMarkupConcern.class )
    public interface AuditTrail extends Property<List<String>>
    {
    }

    public interface Audit
    {
        @UseDefaults
        AuditTrail trail();
    }
}
