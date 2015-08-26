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

package org.apache.zest.library.rest.server.assembler;

import java.lang.reflect.Modifier;
import java.util.Properties;
import java.util.function.Predicate;
import org.apache.velocity.app.VelocityEngine;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.service.importer.NewObjectImporter;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ClassScanner;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.rest.server.restlet.InteractionConstraintsService;
import org.apache.zest.library.rest.server.restlet.RequestReaderDelegator;
import org.apache.zest.library.rest.server.restlet.ResponseWriterDelegator;
import org.apache.zest.library.rest.server.restlet.freemarker.ValueCompositeObjectWrapper;
import org.apache.zest.library.rest.server.restlet.requestreader.DefaultRequestReader;
import org.apache.zest.library.rest.server.restlet.responsewriter.AbstractResponseWriter;
import org.apache.zest.library.rest.server.restlet.responsewriter.DefaultResponseWriter;
import org.apache.zest.library.rest.server.spi.ResponseWriter;
import org.restlet.service.MetadataService;

import static org.apache.zest.api.util.Classes.hasModifier;
import static org.apache.zest.api.util.Classes.isAssignableFrom;
import static org.apache.zest.bootstrap.ImportedServiceDeclaration.INSTANCE;
import static org.apache.zest.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;
import static org.apache.zest.functional.Iterables.filter;

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
        Predicate<Class<?>> responseWriterClass = isAssignableFrom( ResponseWriter.class );
        Predicate<Class<?>> isNotAnAbstract = hasModifier( Modifier.ABSTRACT ).negate();
        Iterable<Class<?>> candidates = filter( isNotAnAbstract.and( responseWriterClass ), writers );
        for( Class<?> responseWriter : candidates )
        {
            module.objects( responseWriter );
        }

        // Standard request readers
        module.objects( DefaultRequestReader.class );
    }
}
