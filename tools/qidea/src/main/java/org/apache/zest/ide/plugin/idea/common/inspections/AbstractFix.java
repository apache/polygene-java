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
package org.apache.zest.ide.plugin.idea.common.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import org.jetbrains.annotations.NotNull;

import static org.apache.zest.ide.plugin.idea.common.resource.Qi4jResourceBundle.message;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public abstract class AbstractFix
    implements LocalQuickFix
{
    private String fixName;

    protected AbstractFix( @NotNull String name )
    {
        fixName = name;
    }

    @NotNull
    public final String getName()
    {
        return fixName;
    }

    @NotNull
    public final String getFamilyName()
    {
        return message( "qi4j.quick.fixes.family.name" );
    }
}