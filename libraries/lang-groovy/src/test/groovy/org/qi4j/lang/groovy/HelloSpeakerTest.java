/*
 * Copyright (c) 2015 the original author or authors
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
package org.qi4j.lang.groovy;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.bootstrap.SingletonAssembler;

public class HelloSpeakerTest extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( HelloSpeaker.class ).withMixins( GroovyMixin.class );
    }

    @Test
    public void testHello()
    {
        HelloSpeaker speaker = module.newTransient( HelloSpeaker.class );
        Assert.assertEquals( "Hello World!", speaker.sayHello( "World" ) );
    }

    @Test
    public void testGroovyScriptResourceMixin()
        throws Exception
    {
        // START SNIPPET: script
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly assembly )
                throws AssemblyException
            {
                assembly.transients( HelloSpeaker.class ).withMixins( GroovyMixin.class );
            }
        };
        HelloSpeaker speaker = assembler.module().newTransient( HelloSpeaker.class );
        Assert.assertEquals( "Hello World!", speaker.sayHello( "World" ) );
        // END SNIPPET: script
    }

    @Test
    public void testGroovyClassMixin()
        throws Exception
    {
        // START SNIPPET: direct
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( HelloSpeaker.class ).withMixins( HelloSpeakerMixin.class );
            }
        };
        HelloSpeaker speaker = assembler.module().newTransient( HelloSpeaker.class );
        Assert.assertEquals( "Hello World!", speaker.sayHello( "World" ) );
        // END SNIPPET: direct
    }
}
