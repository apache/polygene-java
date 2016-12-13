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
 *
 *
 */

package org.apache.polygene.runtime.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.polygene.api.activation.Activator;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.service.ServiceImporter;
import org.apache.polygene.api.structure.Layer;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.AssemblyVisitor;
import org.apache.polygene.bootstrap.EntityAssembly;
import org.apache.polygene.bootstrap.EntityDeclaration;
import org.apache.polygene.bootstrap.ImportedServiceAssembly;
import org.apache.polygene.bootstrap.ImportedServiceDeclaration;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.ObjectAssembly;
import org.apache.polygene.bootstrap.ObjectDeclaration;
import org.apache.polygene.bootstrap.ServiceAssembly;
import org.apache.polygene.bootstrap.ServiceDeclaration;
import org.apache.polygene.bootstrap.TransientAssembly;
import org.apache.polygene.bootstrap.TransientDeclaration;
import org.apache.polygene.bootstrap.ValueAssembly;
import org.apache.polygene.bootstrap.ValueDeclaration;

/**
 * Assembly of a Layer. From here you can create more ModuleAssemblies for
 * the Layer that is being assembled. It is also here that you define
 * what other Layers this Layer is using by calling {@link org.apache.polygene.runtime.bootstrap.LayerAssemblyImpl#uses()}.
 */
public final class LayerAssemblyImpl
    implements LayerAssembly
{
    private final ApplicationAssembly applicationAssembly;
    private final HashMap<String, ModuleAssemblyImpl> moduleAssemblies;
    private final Set<LayerAssembly> uses;

    private String name;
    private final MetaInfo metaInfo = new MetaInfo();
    private final List<Class<? extends Activator<Layer>>> activators = new ArrayList<>();

    public LayerAssemblyImpl( ApplicationAssembly applicationAssembly, String name )
    {
        this.applicationAssembly = applicationAssembly;
        this.name = name;

        moduleAssemblies = new LinkedHashMap<>();
        uses = new LinkedHashSet<>();
    }

    @Override
    public ModuleAssembly module( String name )
    {
        if( name != null )
        {
            ModuleAssemblyImpl existing = moduleAssemblies.get( name );
            if( existing != null )
            {
                return existing;
            }
        }
        ModuleAssemblyImpl moduleAssembly = new ModuleAssemblyImpl( this, name );
        moduleAssemblies.put( name, moduleAssembly );
        return moduleAssembly;
    }

    @Override
    public ApplicationAssembly application()
    {
        return applicationAssembly;
    }

    @Override
    public LayerAssembly setName( String name )
    {
        this.name = name;
        return this;
    }

    @Override
    public LayerAssembly setMetaInfo( Object info )
    {
        metaInfo.set( info );
        return this;
    }

    @Override
    public LayerAssembly uses( LayerAssembly... layerAssembly )
        throws IllegalArgumentException
    {
        uses.addAll( Arrays.asList( layerAssembly ) );
        return this;
    }

    @Override
    @SafeVarargs
    public final LayerAssembly withActivators( Class<? extends Activator<Layer>>... activators )
    {
        this.activators.addAll( Arrays.asList( activators ) );
        return this;
    }

    @Override
    public <ThrowableType extends Throwable> void visit( AssemblyVisitor<ThrowableType> visitor )
        throws ThrowableType
    {
        visitor.visitLayer( this );
        for( ModuleAssemblyImpl moduleAssembly : moduleAssemblies.values() )
        {
            moduleAssembly.visit( visitor );
        }
    }

    @Override
    public EntityDeclaration entities( Predicate<? super EntityAssembly> specification )
    {
        final List<EntityDeclaration> declarations = new ArrayList<>();

        for( ModuleAssemblyImpl moduleAssembly : moduleAssemblies.values() )
        {
            declarations.add( moduleAssembly.entities( specification ) );
        }

        return new EntityDeclaration()
        {
            @Override
            public EntityDeclaration setMetaInfo( Object info )
            {
                for( EntityDeclaration declaration : declarations )
                {
                    declaration.setMetaInfo( info );
                }
                return this;
            }

            @Override
            public EntityDeclaration visibleIn( Visibility visibility )
            {
                for( EntityDeclaration declaration : declarations )
                {
                    declaration.visibleIn( visibility );
                }
                return this;
            }

            @Override
            public EntityDeclaration withConcerns( Class<?>... concerns )
            {
                for( EntityDeclaration declaration : declarations )
                {
                    declaration.withConcerns( concerns );
                }
                return this;
            }

            @Override
            public EntityDeclaration withSideEffects( Class<?>... sideEffects )
            {
                for( EntityDeclaration declaration : declarations )
                {
                    declaration.withSideEffects( sideEffects );
                }
                return this;
            }

            @Override
            public EntityDeclaration withMixins( Class<?>... mixins )
            {
                for( EntityDeclaration declaration : declarations )
                {
                    declaration.withMixins( mixins );
                }
                return this;
            }

            @Override
            public EntityDeclaration withTypes( Class<?>... types )
            {
                for( EntityDeclaration declaration : declarations )
                {
                    declaration.withTypes( types );
                }
                return this;
            }
        };
    }

    @Override
    public ServiceDeclaration services( Predicate<? super ServiceAssembly> specification )
    {
        final List<ServiceDeclaration> declarations = new ArrayList<>();

        for( ModuleAssemblyImpl moduleAssembly : moduleAssemblies.values() )
        {
            declarations.add( moduleAssembly.services( specification ) );
        }

        return new ServiceDeclaration()
        {
            @Override
            public ServiceDeclaration setMetaInfo( Object serviceAttribute )
            {
                for( ServiceDeclaration declaration : declarations )
                {
                    declaration.setMetaInfo( serviceAttribute );
                }
                return this;
            }

            @Override
            public ServiceDeclaration visibleIn( Visibility visibility )
            {
                for( ServiceDeclaration declaration : declarations )
                {
                    declaration.visibleIn( visibility );
                }
                return this;
            }

            @Override
            public ServiceDeclaration withConcerns( Class<?>... concerns )
            {
                for( ServiceDeclaration declaration : declarations )
                {
                    declaration.withConcerns( concerns );
                }
                return this;
            }

            @Override
            public ServiceDeclaration withSideEffects( Class<?>... sideEffects )
            {
                for( ServiceDeclaration declaration : declarations )
                {
                    declaration.withSideEffects( sideEffects );
                }
                return this;
            }

            @Override
            public ServiceDeclaration withMixins( Class<?>... mixins )
            {
                for( ServiceDeclaration declaration : declarations )
                {
                    declaration.withMixins( mixins );
                }
                return this;
            }

            @Override
            public ServiceDeclaration withTypes( Class<?>... types )
            {
                for( ServiceDeclaration declaration : declarations )
                {
                    declaration.withTypes( types );
                }
                return this;
            }

            @Override
            @SafeVarargs
            public final ServiceDeclaration withActivators( Class<? extends Activator<?>>... activators )
            {
                for( ServiceDeclaration declaration : declarations )
                {
                    declaration.withActivators( activators );
                }
                return this;
            }

            @Override
            public ServiceDeclaration identifiedBy( String identity )
            {
                for( ServiceDeclaration declaration : declarations )
                {
                    declaration.identifiedBy( identity );
                }
                return this;
            }

            @Override
            public ServiceDeclaration taggedWith( String... tags )
            {
                for( ServiceDeclaration declaration : declarations )
                {
                    declaration.taggedWith( tags );
                }
                return this;
            }

            @Override
            public ServiceDeclaration instantiateOnStartup()
            {
                for( ServiceDeclaration declaration : declarations )
                {
                    declaration.instantiateOnStartup();
                }

                return this;
            }
        };
    }

    @Override
    public TransientDeclaration transients( Predicate<? super TransientAssembly> specification )
    {
        final List<TransientDeclaration> declarations = new ArrayList<>();

        for( ModuleAssemblyImpl moduleAssembly : moduleAssemblies.values() )
        {
            declarations.add( moduleAssembly.transients( specification ) );
        }

        return new TransientDeclaration()
        {
            @Override
            public TransientDeclaration setMetaInfo( Object info )
            {
                for( TransientDeclaration declaration : declarations )
                {
                    declaration.setMetaInfo( info );
                }
                return this;
            }

            @Override
            public TransientDeclaration visibleIn( Visibility visibility )
            {
                for( TransientDeclaration declaration : declarations )
                {
                    declaration.visibleIn( visibility );
                }
                return this;
            }

            @Override
            public TransientDeclaration withConcerns( Class<?>... concerns )
            {
                for( TransientDeclaration declaration : declarations )
                {
                    declaration.withConcerns( concerns );
                }
                return this;
            }

            @Override
            public TransientDeclaration withSideEffects( Class<?>... sideEffects )
            {
                for( TransientDeclaration declaration : declarations )
                {
                    declaration.withSideEffects( sideEffects );
                }
                return this;
            }

            @Override
            public TransientDeclaration withMixins( Class<?>... mixins )
            {
                for( TransientDeclaration declaration : declarations )
                {
                    declaration.withMixins( mixins );
                }
                return this;
            }

            @Override
            public TransientDeclaration withTypes( Class<?>... types )
            {
                for( TransientDeclaration declaration : declarations )
                {
                    declaration.withTypes( types );
                }
                return this;
            }
        };
    }

    @Override
    public ValueDeclaration values( Predicate<? super ValueAssembly> specification )
    {
        final List<ValueDeclaration> declarations = new ArrayList<>();

        for( ModuleAssemblyImpl moduleAssembly : moduleAssemblies.values() )
        {
            declarations.add( moduleAssembly.values( specification ) );
        }
        return new ValueDeclaration()
        {
            @Override
            public ValueDeclaration setMetaInfo( Object info )
            {
                for( ValueDeclaration declaration : declarations )
                {
                    declaration.setMetaInfo( info );
                }
                return this;
            }

            @Override
            public ValueDeclaration visibleIn( Visibility visibility )
            {
                for( ValueDeclaration declaration : declarations )
                {
                    declaration.visibleIn( visibility );
                }
                return this;
            }

            @Override
            public ValueDeclaration withConcerns( Class<?>... concerns )
            {
                for( ValueDeclaration declaration : declarations )
                {
                    declaration.withConcerns( concerns );
                }
                return this;
            }

            @Override
            public ValueDeclaration withSideEffects( Class<?>... sideEffects )
            {
                for( ValueDeclaration declaration : declarations )
                {
                    declaration.withSideEffects( sideEffects );
                }
                return this;
            }

            @Override
            public ValueDeclaration withMixins( Class<?>... mixins )
            {
                for( ValueDeclaration declaration : declarations )
                {
                    declaration.withMixins( mixins );
                }
                return this;
            }

            @Override
            public ValueDeclaration withTypes( Class<?>... types )
            {
                for( ValueDeclaration declaration : declarations )
                {
                    declaration.withTypes( types );
                }
                return this;
            }
        };
    }

    @Override
    public ObjectDeclaration objects( Predicate<? super ObjectAssembly> specification )
    {
        final List<ObjectDeclaration> declarations = new ArrayList<>();

        for( ModuleAssemblyImpl moduleAssembly : moduleAssemblies.values() )
        {
            declarations.add( moduleAssembly.objects( specification ) );
        }
        return new ObjectDeclaration()
        {
            @Override
            public ObjectDeclaration setMetaInfo( Object info )
            {
                for( ObjectDeclaration declaration : declarations )
                {
                    declaration.setMetaInfo( info );
                }
                return this;
            }

            @Override
            public ObjectDeclaration visibleIn( Visibility visibility )
                throws IllegalStateException
            {
                for( ObjectDeclaration declaration : declarations )
                {
                    declaration.visibleIn( visibility );
                }
                return this;
            }
        };
    }

    @Override
    public ImportedServiceDeclaration importedServices( Predicate<? super ImportedServiceAssembly> specification )
    {
        final List<ImportedServiceDeclaration> declarations = new ArrayList<>();

        for( ModuleAssemblyImpl moduleAssembly : moduleAssemblies.values() )
        {
            declarations.add( moduleAssembly.importedServices( specification ) );
        }
        return new ImportedServiceDeclaration()
        {

            @Override
            public ImportedServiceDeclaration importOnStartup()
            {
                for( ImportedServiceDeclaration declaration : declarations )
                {
                    declaration.importOnStartup();
                }
                return this;
            }

            @Override
            public ImportedServiceDeclaration visibleIn( Visibility visibility )
            {
                for( ImportedServiceDeclaration declaration : declarations )
                {
                    declaration.visibleIn( visibility );
                }
                return this;
            }

            @Override
            public ImportedServiceDeclaration importedBy( Class<? extends ServiceImporter> serviceImporterClass )
            {
                for( ImportedServiceDeclaration declaration : declarations )
                {
                    declaration.importedBy( serviceImporterClass );
                }
                return this;
            }

            @Override
            public ImportedServiceDeclaration identifiedBy( String identity )
            {
                for( ImportedServiceDeclaration declaration : declarations )
                {
                    declaration.identifiedBy( identity );
                }
                return this;
            }

            @Override
            public ImportedServiceDeclaration taggedWith( String... tags )
            {
                for( ImportedServiceDeclaration declaration : declarations )
                {
                    declaration.taggedWith( tags );
                }
                return this;
            }

            @Override
            public ImportedServiceDeclaration setMetaInfo( Object serviceAttribute )
            {
                for( ImportedServiceDeclaration declaration : declarations )
                {
                    declaration.setMetaInfo( serviceAttribute );
                }
                return this;
            }

            @Override
            @SafeVarargs
            public final ImportedServiceDeclaration withActivators( Class<? extends Activator<?>>... activators )
            {
                for( ImportedServiceDeclaration declaration : declarations )
                {
                    declaration.withActivators( activators );
                }
                return this;
            }

        };
    }

    Collection<ModuleAssemblyImpl> moduleAssemblies()
    {
        return moduleAssemblies.values();
    }

    Set<LayerAssembly> uses()
    {
        return uses;
    }

    public MetaInfo metaInfo()
    {
        return metaInfo;
    }

    @Override
    public String name()
    {
        return name;
    }

    public List<Class<? extends Activator<Layer>>> activators()
    {
        return activators;
    }

    @Override
    public final String toString()
    {
        return "LayerAssembly [" + name + "]";
    }
}
