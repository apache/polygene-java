/*
 * Copyright 2009 Lukasz Zielinski.
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

package org.apache.zest.runtime.mixin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

public class Qi228Test
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( SomeLogicService.class );
    }

    @Test
    public void test1()
        throws Exception
    {
        SomeLogic service = serviceFinder.findService( SomeLogic.class ).get();
        try
        {
            service.getNumbers();
            // Either should succeed.
        }
        catch( Exception e )
        {
            // Or fail with a decent Excpetion.
        }
    }

    public interface SomeLogic
    {
        Collection<Integer> getNumbers();
    }

    public static class SomeLogicMixin
        implements SomeLogic
    {
        public List<Integer> getNumbers()
        {
            return Collections.emptyList();
        }
    }

    @Mixins( { SomeLogicMixin.class } )
    public interface SomeLogicService
        extends SomeLogic, ServiceComposite
    {
    }
}
