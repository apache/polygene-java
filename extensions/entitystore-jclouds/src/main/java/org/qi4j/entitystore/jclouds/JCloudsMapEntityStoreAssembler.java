/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.entitystore.jclouds;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class JCloudsMapEntityStoreAssembler
    implements Assembler
{

    private final Visibility visibility;
    private String identity;
    private ModuleAssembly config;
    private Visibility configVisibility;

    public JCloudsMapEntityStoreAssembler()
    {
        this( Visibility.layer );
    }

    public JCloudsMapEntityStoreAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    public JCloudsMapEntityStoreAssembler identifiedBy( String identity )
    {
        this.identity = identity;
        return this;
    }

    public JCloudsMapEntityStoreAssembler withConfigIn( ModuleAssembly config, Visibility configVisibility )
    {
        this.config = config;
        this.configVisibility = configVisibility;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( UuidIdentityGeneratorService.class );
        module.services( JCloudsMapEntityStoreService.class ).visibleIn( visibility ).instantiateOnStartup();
        if( identity != null && identity.length() > 0 )
        {
            module.services( JCloudsMapEntityStoreService.class ).identifiedBy( identity );
        }
        if( config != null )
        {
            config.entities( JCloudsMapEntityStoreConfiguration.class ).visibleIn( configVisibility );
        }
    }
}
