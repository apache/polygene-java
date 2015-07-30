/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.apache.zest.library.uid.sequence;

import org.junit.Test;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.uid.sequence.assembly.PersistingSequencingAssembler;
import org.apache.zest.test.AbstractQi4jTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.junit.Assert.*;
    
public class PersistingSequencingTest extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        new PersistingSequencingAssembler().assemble( module );
        new EntityTestAssembler().assemble( module );
        module.transients( UnderTestComposite.class );
    }

    @Test
    public void whenTransientSequencingThenNumbersStartAtZero()
        throws Exception
    {
        UnderTest underTest = module.newTransient( UnderTest.class );
        assertEquals( 0, underTest.currentValue() );
    }

    @Test
    public void whenTransientSequencingThenFirstNextValueIsOne()
        throws Exception
    {
        UnderTest underTest = module.newTransient( UnderTest.class );
        assertEquals( 1, underTest.nextValue() );
        assertEquals( 1, underTest.currentValue() );
    }

    @Test
    public void whenTransientSequencingThenFirst100ValuesAreInSequence()
        throws Exception
    {
        UnderTest underTest = module.newTransient( UnderTest.class );
        for( int i = 1; i <= 100; i++ )
        {
            assertEquals( i, underTest.nextValue() );
            assertEquals( i, underTest.currentValue() );
            assertEquals( i, underTest.currentValue() );
            assertEquals( i, underTest.currentValue() );
        }
    }

    public interface UnderTest
    {
        long nextValue();

        long currentValue();
    }

    @Mixins( UnderTestMixin.class )
    public interface UnderTestComposite extends UnderTest, TransientComposite
    {
    }

    public static class UnderTestMixin
        implements UnderTest
    {
        @Service private Sequencing service;

        public long nextValue()
        {
            return service.newSequenceValue();
        }

        public long currentValue()
        {
            return service.currentSequenceValue();
        }
    }
}