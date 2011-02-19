/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.envisage.school;

import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.envisage.school.config.persistence.PersistenceConfigAssembler;
import org.qi4j.envisage.school.domain.person.assembly.PersonModelAssembler;
import org.qi4j.envisage.school.domain.school.assembly.SchoolModelAssembler;
import org.qi4j.envisage.school.infrastructure.mail.assembly.MailServiceAssembler;
import org.qi4j.envisage.school.infrastructure.persistence.PersistenceAssembler;
import org.qi4j.envisage.school.ui.admin.AdminAssembler;

/**
 * Application assembler for the School sample
 */
public class SchoolAssembler
    implements ApplicationAssembler
{
    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
        throws AssemblyException
    {
        final ApplicationAssembly appAssembly = applicationFactory.newApplicationAssembly();
        appAssembly.setName( "School" );

        // Create layers
        LayerAssembly layerUI = createUILayer( appAssembly );
        LayerAssembly layerDomain = createDomainLayer( appAssembly );
        LayerAssembly layerInfra = createInfrastructureLayer( appAssembly );
        LayerAssembly layerConfig = createConfigLayer( appAssembly );

        layerUI.uses( layerDomain );
        layerDomain.uses( layerInfra );
        layerDomain.uses( layerConfig );

        return appAssembly;
    }

    private LayerAssembly createInfrastructureLayer( ApplicationAssembly appAssembly )
        throws AssemblyException
    {
        LayerAssembly layerInfrastructure = appAssembly.layerAssembly( "Infrastructure" );

        ModuleAssembly moduleMail = layerInfrastructure.moduleAssembly( "Mail" );
        new MailServiceAssembler().assemble( moduleMail );

        ModuleAssembly modulePersistence = layerInfrastructure.moduleAssembly( "Persistence" );
        new PersistenceAssembler().assemble( modulePersistence );

        return layerInfrastructure;
    }

    private LayerAssembly createConfigLayer( ApplicationAssembly appAssembly )
        throws AssemblyException
    {
        LayerAssembly layerConfig = appAssembly.layerAssembly( "configuration" );

        ModuleAssembly persistenceConfig = layerConfig.moduleAssembly( "persistence" );
        new PersistenceConfigAssembler().assemble( persistenceConfig );
        return layerConfig;
    }

    private LayerAssembly createDomainLayer( ApplicationAssembly appAssembly )
        throws AssemblyException
    {
        LayerAssembly layerDomain = appAssembly.layerAssembly( "domain" );

        ModuleAssembly modulePerson = layerDomain.moduleAssembly( "person" );
        new PersonModelAssembler().assemble( modulePerson );

        ModuleAssembly moduleSchool = layerDomain.moduleAssembly( "school" );
        new SchoolModelAssembler().assemble( moduleSchool );

        return layerDomain;
    }

    private LayerAssembly createUILayer( ApplicationAssembly appAssembly )
        throws AssemblyException
    {
        LayerAssembly layerUI = appAssembly.layerAssembly( "UI" );

        // Add admin
        ModuleAssembly moduleAdmin = layerUI.moduleAssembly( "admin" );
        new AdminAssembler().assemble( moduleAdmin );

        return layerUI;
    }
}
