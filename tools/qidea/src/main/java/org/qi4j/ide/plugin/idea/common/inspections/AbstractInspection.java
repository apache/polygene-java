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
package org.qi4j.ide.plugin.idea.common.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.qi4j.ide.plugin.idea.common.resource.Qi4jResourceBundle;

import static com.intellij.codeHighlighting.HighlightDisplayLevel.ERROR;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public abstract class AbstractInspection extends BaseJavaLocalInspectionTool
{
    private static final String QI4J_IDEA_INSPECTIONS_NAME = "qi4j.inspections.name";

    @Nls @NotNull public String getGroupDisplayName()
    {
        return Qi4jResourceBundle.message( QI4J_IDEA_INSPECTIONS_NAME );
    }

    @NotNull
    protected abstract String resourceBundlePrefixId();

    @Nls @NotNull
    public final String getDisplayName()
    {
        return Qi4jResourceBundle.message( resourceBundlePrefixId() + ".name.display" );
    }

    @NotNull @Override
    public HighlightDisplayLevel getDefaultLevel()
    {
        return ERROR;
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return true;
    }
}
