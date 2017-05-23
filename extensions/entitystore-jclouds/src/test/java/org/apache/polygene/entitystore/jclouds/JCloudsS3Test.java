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

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.jclouds.assembly.JCloudsEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.docker.DockerRule;
import org.apache.polygene.test.entity.AbstractEntityStoreTest;
import org.junit.ClassRule;

public class JCloudsS3Test extends AbstractEntityStoreTest
{
    @ClassRule
    public static final DockerRule DOCKER = new DockerRule( "s3server", "server started" );

    @Override
    public void assemble( ModuleAssembly module )
    {
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
        new JCloudsEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
        JCloudsMapEntityStoreConfiguration defaults =
            config.forMixin( JCloudsMapEntityStoreConfiguration.class ).declareDefaults();

        String host = DOCKER.getDockerHost();
        int port = DOCKER.getExposedContainerPort( "8000/tcp" );
        defaults.provider().set( "s3" );
        defaults.endpoint().set( "http://" + host + ':' + port );
        defaults.identifier().set( "dummyIdentifier" );
        defaults.credential().set( "dummyCredential" );
    }
}
