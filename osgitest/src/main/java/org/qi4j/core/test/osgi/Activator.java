/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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

package org.qi4j.core.test.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.structure.Application;
import org.qi4j.structure.Module;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.property.Property;
import org.qi4j.injection.scope.This;

public class Activator
    implements BundleActivator
{
    private static final String MODULE_NAME = "Single Module.";
    private static final String LAYER_NAME = "Single Layer.";
    private Application application;

    public void start( BundleContext bundleContext ) throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler assembler = new Assembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.layerAssembly().setName( LAYER_NAME );
                module.setName( MODULE_NAME );
                module.addComposites( SimpleComposite.class );
            }
        };
        application = boot.newApplication( assembler );
        application.activate();

        Module module = application.findModule( LAYER_NAME, MODULE_NAME );
        CompositeBuilderFactory builderFactory = module.compositeBuilderFactory();
        CompositeBuilder<Simple> builder = builderFactory.newCompositeBuilder( Simple.class );
        builder.stateOfComposite().someValue().set( "Habba" );
        Simple composite = builder.newInstance();
        System.out.println( composite.sayValue() );
    }

    public void stop( BundleContext bundleContext ) throws Exception
    {
        application.passivate();
    }

    @Mixins( SimpleMixin.class )
    private static interface SimpleComposite extends Simple, Composite
    {}

    private static interface Simple
    {
        Property<String> someValue();
        String sayValue();
    }

    public static abstract class SimpleMixin
        implements Simple
    {
        @This private Simple me;

        public String sayValue()
        {
            return "Saying: " + me.someValue().get();
        }
    }
}
