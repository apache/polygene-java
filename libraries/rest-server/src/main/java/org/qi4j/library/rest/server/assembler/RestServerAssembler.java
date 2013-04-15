/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.server.assembler;

import java.lang.reflect.Modifier;
import java.util.Properties;
import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.importer.NewObjectImporter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ClassScanner;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Specification;
import org.qi4j.library.rest.server.restlet.InteractionConstraintsService;
import org.qi4j.library.rest.server.restlet.RequestReaderDelegator;
import org.qi4j.library.rest.server.restlet.ResponseWriterDelegator;
import org.qi4j.library.rest.server.restlet.freemarker.ValueCompositeObjectWrapper;
import org.qi4j.library.rest.server.restlet.requestreader.DefaultRequestReader;
import org.qi4j.library.rest.server.restlet.responsewriter.AbstractResponseWriter;
import org.qi4j.library.rest.server.restlet.responsewriter.DefaultResponseWriter;
import org.qi4j.library.rest.server.spi.ResponseWriter;
import org.restlet.service.MetadataService;

import static org.qi4j.api.util.Classes.hasModifier;
import static org.qi4j.api.util.Classes.isAssignableFrom;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.INSTANCE;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Specifications.and;
import static org.qi4j.functional.Specifications.not;

/**
 * JAVADOC
 */
public class RestServerAssembler
    implements Assembler
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        Properties props = new Properties();
        try
        {
            props.load( getClass().getResourceAsStream( "/velocity.properties" ) );

            VelocityEngine velocity = new VelocityEngine( props );

            module.importedServices( VelocityEngine.class )
                .importedBy( INSTANCE ).setMetaInfo( velocity );
        }
        catch( Exception e )
        {
            throw new AssemblyException( "Could not load velocity properties", e );
        }

        freemarker.template.Configuration cfg = new freemarker.template.Configuration();
        cfg.setClassForTemplateLoading( AbstractResponseWriter.class, "" );
        cfg.setObjectWrapper( new ValueCompositeObjectWrapper() );

        module.importedServices( freemarker.template.Configuration.class ).setMetaInfo( cfg );

        module.importedServices( MetadataService.class );

        module.importedServices( ResponseWriterDelegator.class )
            .identifiedBy( "responsewriterdelegator" )
            .importedBy( NEW_OBJECT )
            .visibleIn( Visibility.layer );
        module.objects( ResponseWriterDelegator.class );

        module.importedServices( RequestReaderDelegator.class )
            .identifiedBy( "requestreaderdelegator" )
            .importedBy( NEW_OBJECT )
            .visibleIn( Visibility.layer );
        module.objects( RequestReaderDelegator.class );

        module.importedServices( InteractionConstraintsService.class ).
            importedBy( NewObjectImporter.class ).
            visibleIn( Visibility.application );
        module.objects( InteractionConstraintsService.class );

        // Standard response writers
        Iterable<Class<?>> writers = ClassScanner.findClasses( DefaultResponseWriter.class );
        Specification<Class<?>> responseWriterClass = isAssignableFrom( ResponseWriter.class );
        Specification<Class<?>> isNotAnAbstract = not( hasModifier( Modifier.ABSTRACT ) );
        Iterable<Class<?>> candidates = filter( and( isNotAnAbstract, responseWriterClass ), writers );
        for( Class<?> responseWriter : candidates )
        {
            module.objects( responseWriter );
        }

        // Standard request readers
        module.objects( DefaultRequestReader.class );
    }
}
