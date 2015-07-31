/*
 * Copyright (c) 2015, Paul Merlin. All Rights Reserved.
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
package org.qi4j.tools.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qi4j.tools.model.descriptor.ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor;

/**
 * Visitable Detail Test.
 */
public class VisitableDetailTest
{
    @Test
    public void visit()
        throws AssemblyException, ActivationException
    {
        ApplicationDescriptor application = new Energy4Java().newApplicationModel(
            new ApplicationAssembler()
            {
                @Override
                public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
                {
                    ApplicationAssembly app = applicationFactory.newApplicationAssembly();
                    app.setName( "UnderTestApp" );
                    app.withActivators( ApplicationActivator.class );

                    LayerAssembly layer = app.layer( "LayerName" );
                    layer.withActivators( LayerActivator.class );

                    ModuleAssembly module = layer.module( "ModuleName" );
                    module.withActivators( ModuleActivator.class );

                    return app;
                }
            }
        );
        ApplicationDetailDescriptor detail = createApplicationDetailDescriptor( application );
        Visitor visitor = new Visitor();
        detail.accept( visitor );
        assertThat(
            visitor.events,
            equalTo( Arrays.asList(
                    // Application
                    "visitEnter( UnderTestApp )",
                    "visit( " + ApplicationActivator.class.getName() + " )",
                    // Layer
                    "visitEnter( LayerName )",
                    "visit( " + LayerActivator.class.getName() + " )",
                    // Module
                    "visitEnter( ModuleName )",
                    "visit( " + ModuleActivator.class.getName() + " )",
                    // Leaving Structure
                    "visitLeave( ModuleName )",
                    "visitLeave( LayerName )",
                    "visitLeave( UnderTestApp )"
                )
            )
        );
    }

    private static final class Visitor
        implements HierarchicalVisitor<Object, Object, RuntimeException>
    {
        private final List<String> events = new ArrayList<>();

        @Override
        public boolean visitEnter( Object visited )
            throws RuntimeException
        {
            String event = "visitEnter( " + visited + " )";
            events.add( event );
            System.out.println( event );
            return true;
        }

        @Override
        public boolean visitLeave( Object visited )
            throws RuntimeException
        {
            String event = "visitLeave( " + visited + " )";
            events.add( event );
            System.out.println( event );
            return true;
        }

        @Override
        public boolean visit( Object visited )
            throws RuntimeException
        {
            String event = "visit( " + visited + " )";
            events.add( event );
            System.out.println( event );
            return true;
        }
    }

    static class ApplicationActivator
        extends ActivatorAdapter<Application>
    {
    }

    static class LayerActivator
        extends ActivatorAdapter<Layer>
    {
    }

    static class ModuleActivator
        extends ActivatorAdapter<Module>
    {
    }
}
