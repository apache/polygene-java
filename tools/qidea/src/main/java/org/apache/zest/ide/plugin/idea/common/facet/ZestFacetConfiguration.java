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
*/

package org.apache.zest.ide.plugin.idea.common.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.apache.zest.ide.plugin.idea.common.facet.ui.PolygeneFacetEditorTab;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class PolygeneFacetConfiguration
    implements FacetConfiguration
{
    public FacetEditorTab[] createEditorTabs( FacetEditorContext editorContext,
                                              FacetValidatorsManager validatorsManager )
    {
        return new FacetEditorTab[]{
            new PolygeneFacetEditorTab( editorContext )
        };
    }

    public final void readExternal( Element element )
        throws InvalidDataException
    {
        // Do nothing
    }

    public final void writeExternal( Element element )
        throws WriteExternalException
    {
        // Do nothing
    }
}
