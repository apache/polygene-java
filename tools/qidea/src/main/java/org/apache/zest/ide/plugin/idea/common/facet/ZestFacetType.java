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
public final class ZestFacetType extends FacetType<ZestFacet, ZestFacetConfiguration>
{
    public static final FacetTypeId<ZestFacet> ID = new FacetTypeId<ZestFacet>();

    public ZestFacetType()
    {
        super( ID, "ZestFacet", "Zest Facet" );
    }

    public final ZestFacetConfiguration createDefaultConfiguration()
    {
        return new ZestFacetConfiguration();
    }

    public final ZestFacet createFacet( @NotNull Module module,
                                        String name,
                                        @NotNull ZestFacetConfiguration configuration,
                                        @Nullable Facet underlyingFacet )
    {
        return new ZestFacet( this, module, name, configuration, underlyingFacet );
    }

    public final boolean isSuitableModuleType( ModuleType moduleType )
    {
        return moduleType instanceof JavaModuleType;
    }

    @Override
    public final void registerDetectors( FacetDetectorRegistry<ZestFacetConfiguration> registry )
    {
        registry.registerOnTheFlyDetector(
            StdFileTypes.JAVA, VirtualFileFilter.ALL, new HasZestImportPackageCondition(),
            new FacetDetector<PsiFile, ZestFacetConfiguration>( "ZestFacetDetector" )
            {
                @Override
                public ZestFacetConfiguration detectFacet( PsiFile source,
                                                           Collection<ZestFacetConfiguration> existingConfigurations )
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

    private static class HasZestImportPackageCondition
        implements Condition<PsiFile>
    {
        public final boolean value( PsiFile psiFile )
        {
            final boolean[] hasZestImportPackage = new boolean[]{ false };

            psiFile.accept( new JavaElementVisitor()
            {
                @Override
                public final void visitImportStatement( PsiImportStatement statement )
                {
                    String packageName = statement.getQualifiedName();
                    if( packageName != null && packageName.startsWith( "org.apache.zest" ) )
                    {
                        hasZestImportPackage[ 0 ] = true;
                    }
                }

                @Override
                public void visitReferenceExpression( PsiReferenceExpression expression )
                {
                    // Ignore
                }
            } );
            return hasZestImportPackage[ 0 ];
        }
    }
}
