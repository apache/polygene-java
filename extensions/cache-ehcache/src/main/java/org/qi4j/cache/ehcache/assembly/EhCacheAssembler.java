/*
 * Copyright 2010 Niclas Hedhman.
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
package org.qi4j.cache.ehcache.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.cache.ehcache.EhCacheConfiguration;
import org.qi4j.cache.ehcache.EhCachePoolService;

public class EhCacheAssembler
    implements Assembler
{

    private Visibility visibility = Visibility.module;
    private Visibility configVisibility = Visibility.module;
    private ModuleAssembly configModule = null;

    public EhCacheAssembler()
    {
    }

    public EhCacheAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    public EhCacheAssembler visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public EhCacheAssembler withConfig( ModuleAssembly configModule, Visibility configVisibility )
    {
        this.configModule = configModule;
        this.configVisibility = configVisibility;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( EhCachePoolService.class ).visibleIn( visibility );
        if( configModule != null )
        {
            configModule.entities( EhCacheConfiguration.class ).visibleIn( configVisibility );
        }
    }

}
