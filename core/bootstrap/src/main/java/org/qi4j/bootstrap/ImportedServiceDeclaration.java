/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 * Copyright 2012, Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.qi4j.bootstrap;

import org.qi4j.api.activation.Activator;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.importer.InstanceImporter;
import org.qi4j.api.service.importer.NewObjectImporter;
import org.qi4j.api.service.importer.ServiceInstanceImporter;
import org.qi4j.api.service.importer.ServiceSelectorImporter;

/**
 * Fluent API for declaring imported services. Instances
 * of this API are acquired by calling {@link ModuleAssembly#importedServices(Class[])}.
 */
public interface ImportedServiceDeclaration
{
    // Convenience constants for common service importers
    public static final Class<? extends ServiceImporter> INSTANCE = InstanceImporter.class;
    public static final Class<? extends ServiceImporter> NEW_OBJECT = NewObjectImporter.class;
    public static final Class<? extends ServiceImporter> SERVICE_SELECTOR = ServiceSelectorImporter.class;
    public static final Class<? extends ServiceImporter> SERVICE_IMPORTER = ServiceInstanceImporter.class;

    ImportedServiceDeclaration visibleIn( Visibility visibility );

    ImportedServiceDeclaration importedBy( Class<? extends ServiceImporter> serviceImporterClass );

    ImportedServiceDeclaration identifiedBy( String identity );

    ImportedServiceDeclaration taggedWith( String... tags );

    ImportedServiceDeclaration setMetaInfo( Object serviceAttribute );
    
    ImportedServiceDeclaration importOnStartup();

    /**
     * Set the imported service activators. Activators are executed in order around
     * the ServiceReference activation and passivation.
     *
     * @param activators the imported service activators
     * @return the assembly
     */    
    ImportedServiceDeclaration withActivators( Class<? extends Activator<?>>... activators );
}