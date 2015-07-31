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
package org.qi4j.ide.plugin.idea.common.facet.ui;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class Qi4jFacetEditorTab extends FacetEditorTab
{
    private final FacetEditorContext editorContext;

    public Qi4jFacetEditorTab( FacetEditorContext aContext )
    {
        editorContext = aContext;
    }

    @Nls
    public final String getDisplayName()
    {
        return "Zest";
    }

    public JComponent createComponent()
    {
        return new JPanel();
    }

    public final boolean isModified()
    {
        return false;
    }

    public final void apply()
        throws ConfigurationException
    {
        // From UI to configuration
    }

    public final void reset()
    {
        // From Configuration to UI
    }

    public final void disposeUIResources()
    {
        // Do nothing for now
    }
}
