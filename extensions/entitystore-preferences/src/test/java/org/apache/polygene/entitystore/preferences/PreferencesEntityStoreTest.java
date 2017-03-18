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
package org.apache.polygene.entitystore.preferences;

import java.util.prefs.Preferences;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.preferences.PreferencesEntityStoreInfo;
import org.apache.polygene.entitystore.preferences.PreferencesEntityStoreService;
import org.apache.polygene.test.entity.AbstractEntityStoreTest;
import org.apache.polygene.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class PreferencesEntityStoreTest
    extends AbstractEntityStoreTest
{
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.layer().application().setName( "PreferencesTest" );

        super.assemble( module );
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( null );
        Preferences node = Preferences.userNodeForPackage( getClass() )
                                      .node( "integtest" )
                                      .node( tmpDir.getRoot().getName() )
                                      .node( "PreferencesEntityStoreTest" );
        PreferencesEntityStoreInfo metaInfo = new PreferencesEntityStoreInfo( node );
        Thread.currentThread().setContextClassLoader( cl );
        module.services( PreferencesEntityStoreService.class ).setMetaInfo( metaInfo ).instantiateOnStartup();
        new OrgJsonValueSerializationAssembler().assemble( module );
    }
}
