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

package org.apache.polygene.ide.plugin.idea.common.facet;

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
 * @since 0.1
 */
public final class PolygeneFacetType extends FacetType<PolygeneFacet, PolygeneFacetConfiguration>
{
    public static final FacetTypeId<PolygeneFacet> ID = new FacetTypeId<PolygeneFacet>();

    public PolygeneFacetType()
    {
        super( ID, "PolygeneFacet", "Polygene Facet" );
    }

    public final PolygeneFacetConfiguration createDefaultConfiguration()
    {
        return new PolygeneFacetConfiguration();
    }

    public final PolygeneFacet createFacet( @NotNull Module module,
                                        String name,
                                        @NotNull PolygeneFacetConfiguration configuration,
                                        @Nullable Facet underlyingFacet )
    {
        return new PolygeneFacet( this, module, name, configuration, underlyingFacet );
    }

    public final boolean isSuitableModuleType( ModuleType moduleType )
    {
        return moduleType instanceof JavaModuleType;
    }

    @Override
    public final void registerDetectors( FacetDetectorRegistry<PolygeneFacetConfiguration> registry )
    {
        registry.registerOnTheFlyDetector(
            StdFileTypes.JAVA, VirtualFileFilter.ALL, new HasPolygeneImportPackageCondition(),
            new FacetDetector<PsiFile, PolygeneFacetConfiguration>( "PolygeneFacetDetector" )
            {
                @Override
                public PolygeneFacetConfiguration detectFacet( PsiFile source,
                                                           Collection<PolygeneFacetConfiguration> existingConfigurations )
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

    private static class HasPolygeneImportPackageCondition
        implements Condition<PsiFile>
    {
        public final boolean value( PsiFile psiFile )
        {
            final boolean[] hasPolygeneImportPackage = new boolean[]{ false };

            psiFile.accept( new JavaElementVisitor()
            {
                @Override
                public final void visitImportStatement( PsiImportStatement statement )
                {
                    String packageName = statement.getQualifiedName();
                    if( packageName != null && packageName.startsWith( "org.apache.polygene" ) )
                    {
                        hasPolygeneImportPackage[ 0 ] = true;
                    }
                }

                @Override
                public void visitReferenceExpression( PsiReferenceExpression expression )
                {
                    // Ignore
                }
            } );
            return hasPolygeneImportPackage[ 0 ];
        }
    }
}
