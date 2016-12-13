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

package org.apache.polygene.ide.plugin.idea;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.FileTypeManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.apache.polygene.ide.plugin.idea.appliesTo.inspections.AppliesToAnnotationDeclaredCorrectlyInspection;
import org.apache.polygene.ide.plugin.idea.common.facet.PolygeneFacetType;
import org.apache.polygene.ide.plugin.idea.concerns.inspections.ConcernsAnnotationDeclaredCorrectlyInspection;
import org.apache.polygene.ide.plugin.idea.injections.invocation.inspections.InvocationAnnotationDeclaredCorrectlyInspection;
import org.apache.polygene.ide.plugin.idea.injections.service.inspections.ServiceAnnotationDeclaredCorrectlyInspection;
import org.apache.polygene.ide.plugin.idea.injections.structure.inspections.StructureAnnotationDeclaredCorrectlyInspection;
import org.apache.polygene.ide.plugin.idea.mixins.inspections.MixinImplementsMixinType;
import org.apache.polygene.ide.plugin.idea.mixins.inspections.MixinsAnnotationDeclaredOnMixinType;
import org.apache.polygene.ide.plugin.idea.sideEffects.inspections.SideEffectsAnnotationDeclaredCorrectlyInspection;

import javax.swing.*;

import static org.apache.polygene.ide.plugin.idea.common.resource.PolygeneResourceBundle.message;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class PolygeneApplicationComponent
    implements ApplicationComponent, InspectionToolProvider, FileTemplateGroupDescriptorFactory
{
    @NonNls
    private static String[] FILE_TEMPLATES = {
        "GenericConcernOf.java"
    };

    private final PolygeneFacetType zestFacetType;

    public PolygeneApplicationComponent()
    {
        zestFacetType = new PolygeneFacetType();
    }

    @NotNull
    public final String getComponentName()
    {
        return "PolygeneApplicationComponent";
    }

    public final void initComponent()
    {
        registerFacet();
        registerIntentions();
    }

    private void registerFacet()
    {
        FacetTypeRegistry facetTypeRegistry = FacetTypeRegistry.getInstance();
        facetTypeRegistry.registerFacetType( zestFacetType );
    }

    private void registerIntentions()
    {
//        IntentionManager intentionManager = IntentionManager.getInstance();
//        intentionManager.registerIntentionAndMetaData( new AddConcernOnType(), "intention.category.control.flow" );
    }

    public final void disposeComponent()
    {
        unregisterFacet();
    }

    private void unregisterFacet()
    {
        FacetTypeRegistry facetTypeRegistry = FacetTypeRegistry.getInstance();
        facetTypeRegistry.unregisterFacetType( zestFacetType );
    }

    public final Class[] getInspectionClasses()
    {
        return new Class[]{
            // Concerns
            ConcernsAnnotationDeclaredCorrectlyInspection.class,
            // Mixins
            MixinImplementsMixinType.class,
            MixinsAnnotationDeclaredOnMixinType.class,
            // Side effects
            SideEffectsAnnotationDeclaredCorrectlyInspection.class,
            // Injections
            InvocationAnnotationDeclaredCorrectlyInspection.class,
            ServiceAnnotationDeclaredCorrectlyInspection.class,
            StructureAnnotationDeclaredCorrectlyInspection.class,
            // AppliesTo
            AppliesToAnnotationDeclaredCorrectlyInspection.class
        };
    }

    public final FileTemplateGroupDescriptor getFileTemplatesDescriptor()
    {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(
            message( "zest.file.template.group.title" ), null
        );

        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        for( @NonNls String template : FILE_TEMPLATES )
        {
            Icon icon = fileTypeManager.getFileTypeByFileName( template ).getIcon();
            group.addTemplate( new FileTemplateDescriptor( template, icon ) );
        }

        return group;
    }

}
