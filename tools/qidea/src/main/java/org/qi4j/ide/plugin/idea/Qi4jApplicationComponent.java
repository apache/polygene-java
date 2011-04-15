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
package org.qi4j.ide.plugin.idea;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.FileTypeManager;
import javax.swing.Icon;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.qi4j.ide.plugin.idea.appliesTo.inspections.AppliesToAnnotationDeclaredCorrectlyInspection;
import org.qi4j.ide.plugin.idea.common.facet.Qi4jFacetType;
import static org.qi4j.ide.plugin.idea.common.resource.Qi4jResourceBundle.message;
import org.qi4j.ide.plugin.idea.concerns.inspections.ConcernsAnnotationDeclaredCorrectlyInspection;
import org.qi4j.ide.plugin.idea.injections.invocation.inspections.InvocationAnnotationDeclaredCorrectlyInspection;
import org.qi4j.ide.plugin.idea.injections.service.inspections.ServiceAnnotationDeclaredCorrectlyInspection;
import org.qi4j.ide.plugin.idea.injections.structure.inspections.StructureAnnotationDeclaredCorrectlyInspection;
import org.qi4j.ide.plugin.idea.mixins.inspections.MixinImplementsMixinType;
import org.qi4j.ide.plugin.idea.mixins.inspections.MixinsAnnotationDeclaredOnMixinType;
import org.qi4j.ide.plugin.idea.sideEffects.inspections.SideEffectsAnnotationDeclaredCorrectlyInspection;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class Qi4jApplicationComponent
    implements ApplicationComponent, InspectionToolProvider, FileTemplateGroupDescriptorFactory
{
    @NonNls
    private static String[] FILE_TEMPLATES = {
        "GenericConcernOf.java"
    };

    private final Qi4jFacetType qi4jFacetType;

    public Qi4jApplicationComponent()
    {
        qi4jFacetType = new Qi4jFacetType();
    }

    @NotNull
    public final String getComponentName()
    {
        return "Qi4jApplicationComponent";
    }

    public final void initComponent()
    {
        registerFacet();
        registerIntentions();
    }

    private void registerFacet()
    {
        FacetTypeRegistry facetTypeRegistry = FacetTypeRegistry.getInstance();
        facetTypeRegistry.registerFacetType( qi4jFacetType );
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
        facetTypeRegistry.unregisterFacetType( qi4jFacetType );
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
            message( "qi4j.file.template.group.title" ), null
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
