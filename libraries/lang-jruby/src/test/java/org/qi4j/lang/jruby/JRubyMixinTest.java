package org.qi4j.lang.jruby;
/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/

import org.jruby.Ruby;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.service.importer.InstanceImporter;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

public class JRubyMixinTest extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( MyDomainType.class ).withMixins( JRubyMixin.class );

        Ruby ruby = Ruby.newInstance();
        module.importedServices( Ruby.class ).importedBy( InstanceImporter.class ).setMetaInfo( ruby );
    }

    @Test
    public void testInvoke()
        throws Throwable
    {
        MyDomainType domain = module.newTransientBuilder( MyDomainType.class ).newInstance();

        Assert.assertEquals( "do1() in Ruby mixin.", domain.do1() );
    }
}