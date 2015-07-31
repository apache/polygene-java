/*
 * Copyright 2008 Sonny Gill. All Rights Reserved.
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
package org.qi4j.regression.qi78;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.functional.HierarchicalVisitorAdapter;

public class IssueTest
{
    @Test
    public void testLayersCanBeCreatedInOrderDifferentFromTheirDependency()
        throws AssemblyException
    {
        Energy4Java qi4j = new Energy4Java();

        Application app = qi4j.newApplication( new ApplicationAssembler()
        {
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();

                LayerAssembly domainLayer = assembly.layer( null );
                domainLayer.setName( "Domain" );

                LayerAssembly infrastructureLayer = assembly.layer( null );
                infrastructureLayer.setName( "Infrastructure" );

                domainLayer.uses( infrastructureLayer );

                return assembly;
            }
        } );
        ApplicationDescriptor model = app.descriptor();
        model.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws RuntimeException
            {
                return visited instanceof ApplicationDescriptor;
            }

            @Override
            public boolean visitLeave( Object visited )
                throws RuntimeException
            {
                return visited instanceof LayerDescriptor;
            }

            @Override
            public boolean visit( Object visited )
                throws RuntimeException
            {
                if( visited instanceof LayerDescriptor )
                {
                    Iterable<? extends LayerDescriptor> usedLayers = ( (LayerDescriptor) visited ).usedLayers()
                        .layers();
                    for( LayerDescriptor usedLayerModel : usedLayers )
                    {
                        Assert.assertNotNull( "Used layer model is null", usedLayerModel );
                    }
                }

                return false;
            }
        } );
    }
}
