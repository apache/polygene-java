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
package org.apache.polygene.entitystore.jclouds;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Environment;
import com.github.junit5docker.Port;
import com.github.junit5docker.WaitFor;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.jclouds.assembly.JCloudsEntityStoreAssembler;
import org.apache.polygene.test.entity.model.EntityStoreTestSuite;

@Docker( image = "s3server",
         ports = @Port( exposed = 8801, inner = 8000 ),
         waitFor = @WaitFor( value = "server started", timeoutInMillis = 30000 ),
         environments = {
             @Environment( key = "SCALITY_ACCESS_KEY_ID", value = "dummyIdentifier" ),
             @Environment( key = "SCALITY_SECRET_ACCESS_KEY", value = "dummyCredential" )
         },
         newForEachCase = false
)
public class JCloudsS3TestSuite extends EntityStoreTestSuite
{
    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        new JCloudsEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        JCloudsEntityStoreConfiguration defaults =
            configModule.forMixin( JCloudsEntityStoreConfiguration.class ).declareDefaults();

        String host = "localhost";
        int port = 8801;
        defaults.provider().set( "s3" );
        defaults.endpoint().set( "http://" + host + ':' + port );
        defaults.identifier().set( "dummyIdentifier" );
        defaults.credential().set( "dummyCredential" );
    }
}
