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
package org.apache.zest.entitystore;

import java.util.prefs.Preferences;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.prefs.PreferencesEntityStoreInfo;
import org.apache.zest.entitystore.prefs.PreferencesEntityStoreService;
import org.apache.zest.test.entity.AbstractEntityStoreTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

public class PreferencesEntityStoreTest
    extends AbstractEntityStoreTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.layer().application().setName( "PreferencesTest" );

        super.assemble( module );
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( null );
        PreferencesEntityStoreInfo metaInfo = new PreferencesEntityStoreInfo( Preferences.userNodeForPackage( getClass() ) );
        Thread.currentThread().setContextClassLoader( cl );
        module.services( PreferencesEntityStoreService.class ).setMetaInfo( metaInfo ).instantiateOnStartup();
        new OrgJsonValueSerializationAssembler().assemble( module );
    }
}
