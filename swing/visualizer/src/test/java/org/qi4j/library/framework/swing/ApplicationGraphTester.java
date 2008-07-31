/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.framework.swing;

import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.runtime.structure.ApplicationInstance;
import org.qi4j.structure.Application;

/**
 * TODO
 */
public class ApplicationGraphTester
{
    public static void main( String[] args )
        throws Exception
    {
        Energy4Java qi4j = new Energy4Java();

        ApplicationAssembly assembly = qi4j.newApplicationAssembly();

        LayerAssembly infrastructureLayer = assembly.newLayerAssembly();
        infrastructureLayer.setName( "Infrastructure" );
        ModuleAssembly database = infrastructureLayer.newModuleAssembly();
        database.setName( "Database" );

        LayerAssembly domainLayer = assembly.newLayerAssembly();
        domainLayer.setName( "Domain" );

        ModuleAssembly someDomain = domainLayer.newModuleAssembly();
        someDomain.setName( "Some domain" );
        someDomain.addComposites( ADomainComposite.class, BDomainComposite.class );

        LayerAssembly guiLayer = assembly.newLayerAssembly();
        guiLayer.setName( "UI" );

        ModuleAssembly swingModule = guiLayer.newModuleAssembly();
        swingModule.setName( "Swing" );

        ModuleAssembly plugin1 = guiLayer.newModuleAssembly();
        plugin1.setName( "Plugin 1" );

        ModuleAssembly plugin2 = guiLayer.newModuleAssembly();
        plugin2.setName( "Plugin 2" );
        plugin2.addComposites( UIComposite.class );

        guiLayer.uses( infrastructureLayer );
        guiLayer.uses( domainLayer );

        Application app1 = qi4j.newApplication( assembly );
        new ApplicationGraph().show( ( (ApplicationInstance) app1 ).model() );
    }

    private static interface ADomainComposite extends Composite
    {

    }

    private static interface BDomainComposite extends Composite
    {

    }

    private static interface UIComposite extends Composite
    {

    }
}
