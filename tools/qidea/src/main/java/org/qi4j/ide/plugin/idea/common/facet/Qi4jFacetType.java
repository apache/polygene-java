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
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiReferenceExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class Qi4jFacetType extends FacetType<Qi4jFacet, Qi4jFacetConfiguration>
{
    public static final FacetTypeId<Qi4jFacet> ID = new FacetTypeId<Qi4jFacet>();

    public Qi4jFacetType()
    {
        super( ID, "Qi4jFacet", "Qi4j Facet" );
    }

    public final Qi4jFacetConfiguration createDefaultConfiguration()
    {
        return new Qi4jFacetConfiguration();
    }

    public final Qi4jFacet createFacet( @NotNull Module module,
                                        String name,
                                        @NotNull Qi4jFacetConfiguration configuration,
                                        @Nullable Facet underlyingFacet )
    {
        return new Qi4jFacet( this, module, name, configuration, underlyingFacet );
    }

    public final boolean isSuitableModuleType( ModuleType moduleType )
    {
        return moduleType instanceof JavaModuleType;
    }

    @Override
    public final void registerDetectors( FacetDetectorRegistry<Qi4jFacetConfiguration> registry )
    {
        registry.registerOnTheFlyDetector(
            StdFileTypes.JAVA, VirtualFileFilter.ALL, new HasQi4jImportPackageCondition(),
            new FacetDetector<PsiFile, Qi4jFacetConfiguration>( "Qi4jFacetDetector" )
            {
                @Override
                public Qi4jFacetConfiguration detectFacet( PsiFile source,
                                                           Collection<Qi4jFacetConfiguration> existingConfigurations )
                {
                    if( !existingConfigurations.isEmpty() )
                    {
                        return existingConfigurations.iterator().next();
                    }

                    return createDefaultConfiguration();
                }
            }
        );
    }

    private static class HasQi4jImportPackageCondition
        implements Condition<PsiFile>
    {
        public final boolean value( PsiFile psiFile )
        {
            final boolean[] hasQi4jImportPackage = new boolean[]{ false };

            psiFile.accept( new JavaElementVisitor()
            {
                @Override
                public final void visitImportStatement( PsiImportStatement statement )
                {
                    String packageName = statement.getQualifiedName();
                    if( packageName != null && packageName.startsWith( "org.qi4j" ) )
                    {
                        hasQi4jImportPackage[ 0 ] = true;
                    }
                }

                @Override
                public void visitReferenceExpression( PsiReferenceExpression expression )
                {
                    // Ignore
                }
            } );
            return hasQi4jImportPackage[ 0 ];
        }
    }
}
