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
package org.qi4j.ide.plugin.idea.common.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class Qi4jFacet extends Facet<Qi4jFacetConfiguration>
{
    public Qi4jFacet( @NotNull FacetType facetType,
                      @NotNull Module module,
                      String name,
                      @NotNull Qi4jFacetConfiguration configuration,
                      Facet underlyingFacet )
    {
        super( facetType, module, name, configuration, underlyingFacet );
    }

    @Nullable
    public static Qi4jFacet getInstance( @NotNull Module module )
    {
        return FacetManager.getInstance( module ).getFacetByType( Qi4jFacetType.ID );
    }

}
