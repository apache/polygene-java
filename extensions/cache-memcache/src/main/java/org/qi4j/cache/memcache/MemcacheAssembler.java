/*
 * Copyright 2014 Paul Merlin.
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
package org.qi4j.cache.memcache;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

/**
 * Memcache CachePool Assembler.
 */
public class MemcacheAssembler
    implements Assembler
{
    private Visibility visibility = Visibility.module;
    private ModuleAssembly configModule = null;
    private Visibility configVisibility = Visibility.module;

    public MemcacheAssembler()
    {
    }

    public MemcacheAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    public MemcacheAssembler visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public MemcacheAssembler withConfig( ModuleAssembly configModule, Visibility configVisibility )
    {
        this.configModule = configModule;
        this.configVisibility = configVisibility;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( MemcachePoolService.class ).visibleIn( visibility );
        if( configModule != null )
        {
            configModule.entities( MemcacheConfiguration.class ).visibleIn( configVisibility );
        }
    }
}
