/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.entitystore;

import java.util.prefs.Preferences;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreInfo;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

/**
 * JAVADOC
 */
public class PreferencesEntityStoreTest
    extends AbstractEntityStoreTest
{
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
    }
}
