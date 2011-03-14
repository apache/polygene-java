/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.entitystore.prefs.assembly;

import java.util.prefs.Preferences;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreInfo;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class PreferenceEntityStoreAssembler
    implements Assembler
{
    private Visibility visibility;

    public PreferenceEntityStoreAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        String applicationName = module.layer().application().name();

        Preferences root = Preferences.userRoot();
        Preferences node = root.node( applicationName );
        PreferencesEntityStoreInfo info = new PreferencesEntityStoreInfo( node );
        module.services( PreferencesEntityStoreService.class )
            .setMetaInfo( info )
            .visibleIn( visibility )
            .instantiateOnStartup();
        module.services( UuidIdentityGeneratorService.class ).visibleIn( visibility );
    }
}
