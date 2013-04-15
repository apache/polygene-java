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

import java.util.HashMap;
import java.util.Map;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.junit.AfterClass;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

public class JCloudsFilesystemTest
        extends AbstractEntityStoreTest
{

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
        new OrgJsonValueSerializationAssembler().assemble( module );
        new JCloudsMapEntityStoreAssembler().withConfigIn( config, Visibility.layer ).assemble( module );
        JCloudsMapEntityStoreConfiguration defaults = config.forMixin( JCloudsMapEntityStoreConfiguration.class ).declareDefaults();
        defaults.provider().set( "filesystem" );
        Map<String, String> props = new HashMap<String, String>();
        props.put( FilesystemConstants.PROPERTY_BASEDIR, "build/tmp/" + getClass().getPackage().getName() + "/es-jclouds-" + System.currentTimeMillis() );
        defaults.properties().set( props );
    }

    @AfterClass
    public static void filesystemCleanup()
    {
        // TODO recursively delete "build/tmp/" + getClass().getPackage().getName()
    }

}
