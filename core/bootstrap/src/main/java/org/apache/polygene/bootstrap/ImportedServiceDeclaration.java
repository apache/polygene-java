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

package org.apache.polygene.bootstrap;

import org.apache.polygene.api.activation.Activator;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.service.ServiceImporter;
import org.apache.polygene.api.service.importer.InstanceImporter;
import org.apache.polygene.api.service.importer.NewObjectImporter;
import org.apache.polygene.api.service.importer.ServiceInstanceImporter;
import org.apache.polygene.api.service.importer.ServiceSelectorImporter;

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
    @SuppressWarnings( { "unchecked","varargs" } )
    ImportedServiceDeclaration withActivators( Class<? extends Activator<?>>... activators );
}