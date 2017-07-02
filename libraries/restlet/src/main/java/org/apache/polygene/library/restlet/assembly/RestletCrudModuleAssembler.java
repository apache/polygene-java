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

package org.apache.polygene.library.restlet.assembly;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.ServiceDeclaration;
import org.apache.polygene.library.restlet.repository.CrudRepository;
import org.apache.polygene.library.restlet.repository.EntityTypeDescriptor;
import org.apache.polygene.library.restlet.repository.SmallCrudRepositoryMixin;

/**
 * This assembler should be used for each module that has CRUD types, reachable by the REST API.
 */
public class RestletCrudModuleAssembler extends Assemblers.VisibilityIdentity<RestletCrudModuleAssembler>
{
    private final Class type;
    private final Class repositoryType;

    public RestletCrudModuleAssembler( Class type, Class repositoryType )
    {
        this.type = type;
        this.repositoryType = repositoryType;
    }

    public RestletCrudModuleAssembler( Class type )
    {
        this.type = type;
        repositoryType = CrudRepository.class;
    }

    @Override
    public void assemble( ModuleAssembly module )
    {
        ServiceDeclaration declaration = module
            .addServices( repositoryType )
            .visibleIn( Visibility.application )
            .withMixins( SmallCrudRepositoryMixin.class )
            .withConcerns( UnitOfWorkConcern.class )
            .taggedWith( type.getSimpleName() )
            .setMetaInfo( new EntityTypeDescriptor( type ) );
        if( hasIdentity() )
        {
            declaration.identifiedBy( identity() );
        }
        else
        {
            declaration.identifiedBy( "repository_" + type.getSimpleName() );
        }
        module.entities( type ).visibleIn( Visibility.layer );
        module.values( type ).visibleIn( Visibility.layer );
    }
}
