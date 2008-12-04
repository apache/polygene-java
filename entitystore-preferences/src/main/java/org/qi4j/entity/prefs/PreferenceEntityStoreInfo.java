/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.entity.prefs;

import java.io.Serializable;
import java.util.prefs.Preferences;

/**
 * @author edward.yakop@gmail.com
 */
public final class PreferenceEntityStoreInfo
    implements Serializable
{
    public static enum PreferenceNode
    {
        /**
         * @see Preferences#systemRoot()
         */
        SYSTEM_ROOT( Preferences.systemRoot() ),

        /**
         * @see Preferences#userRoot()
         */
        USER_ROOT( Preferences.userRoot() );

        private final Preferences preferences;

        private PreferenceNode( Preferences preferences )
        {
            this.preferences = preferences;
        }

        final Preferences getNode()
        {
            return preferences;
        }
    }

    private final PreferenceNode rootNode;

    public PreferenceEntityStoreInfo( PreferenceNode aRootNode )
    {
        rootNode = aRootNode;
    }

    /**
     * @return root preference node to use.
     */
    public PreferenceNode getRootNode()
    {
        return rootNode;
    }
}