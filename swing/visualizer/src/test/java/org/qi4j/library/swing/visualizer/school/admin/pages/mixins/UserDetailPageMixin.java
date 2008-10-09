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
package org.qi4j.library.swing.visualizer.school.admin.pages.mixins;

import org.qi4j.injection.scope.Structure;
import org.qi4j.library.swing.visualizer.school.admin.pages.UserDetailPage;
import org.qi4j.structure.Layer;
import org.qi4j.structure.Module;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class UserDetailPageMixin
    implements UserDetailPage
{
    // Note: Don't remove
    private @Structure Layer layer;

    // Note: Don't remove
    public UserDetailPageMixin( @Structure Module module )
    {
    }

    public final String generateHtml()
    {
        return "UserDetailPage";
    }
}
