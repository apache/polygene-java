/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.entitystore.prefs;

import java.util.prefs.Preferences;
import org.junit.After;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import static org.qi4j.entitystore.prefs.PreferenceEntityStoreInfo.PreferenceNode.USER_ROOT;
import org.qi4j.entitystore.prefs.PreferenceEntityStoreInfo;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

/**
 * TODO
 */
public class PreferencesEntityStoreTest extends AbstractEntityStoreTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );

        module.addServices( PreferencesEntityStoreService.class )
            .setMetaInfo( new PreferenceEntityStoreInfo( USER_ROOT ) )
            .instantiateOnStartup();
    }

    @Override
    @After 
    public void tearDown()
        throws Exception
    {
        super.tearDown();
        Preferences preferences = Preferences.userRoot();
        preferences = preferences.node( "/" );
        preferences = preferences.node( "Application" );
        preferences.removeNode();
    }
}
