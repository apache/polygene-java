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
package org.apache.polygene.tools.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.api.structure.Layer;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.Energy4Java;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.tools.model.descriptor.ApplicationDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.ServiceDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.TransientDetailDescriptor;
import org.junit.jupiter.api.Test;

import static org.apache.polygene.tools.model.descriptor.ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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
            applicationFactory -> {
                ApplicationAssembly app = applicationFactory.newApplicationAssembly();
                app.setName( "UnderTestApp" );
                app.withActivators( ApplicationActivator.class );

                LayerAssembly layer = app.layer( "LayerName" );
                layer.withActivators( LayerActivator.class );

                ModuleAssembly module = layer.module( "ModuleName" );
                module.withActivators( ModuleActivator.class );

                return app;
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
            if( visited instanceof ServiceDetailDescriptor)
            {
                return true;
            }
            String event = "visitEnter( " + visited + " )";
            events.add( event );
            System.out.println( event );
            return true;
        }

        @Override
        public boolean visitLeave( Object visited )
            throws RuntimeException
        {
            if( visited instanceof ServiceDetailDescriptor)
            {
                return true;
            }
            String event = "visitLeave( " + visited + " )";
            events.add( event );
            System.out.println( event );
            return true;
        }

        @Override
        public boolean visit( Object visited )
            throws RuntimeException
        {
            if( visited instanceof TransientDetailDescriptor)
            {
                return true;
            }
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
