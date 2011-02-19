package org.qi4j.samples.cargo.app1;

import com.pathfinder.assembly.PathFinderModuleAssembler;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.samples.cargo.app1.assembly.CargoModuleAssembler;
import org.qi4j.samples.cargo.app1.assembly.ConfigurationModule;
import org.qi4j.samples.cargo.app1.assembly.DomainSupportModuleAssembler;
import org.qi4j.samples.cargo.app1.assembly.HandlingModuleAssembler;
import org.qi4j.samples.cargo.app1.assembly.InterfaceModuleAssembler;
import org.qi4j.samples.cargo.app1.assembly.LocationModuleAssembler;
import org.qi4j.samples.cargo.app1.assembly.PersistenceModule;
import org.qi4j.samples.cargo.app1.assembly.RoutingServiceModuleAssembler;
import org.qi4j.samples.cargo.app1.assembly.VoyageModuleAssembler;
import org.qi4j.spi.structure.ApplicationSPI;

public class Main
{
    private static ApplicationSPI application;

    public static void main( String[] args )
        throws Exception
    {
        Energy4Java qi4j = new Energy4Java();
        ApplicationAssembler applicationAssembler = new ApplicationAssembler()
        {

            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
                assembly.setName( "Shipping Sample" );
                LayerAssembly interfaceLayer = assembly.layerAssembly( "Interface Layer" );
                LayerAssembly applicationLayer = assembly.layerAssembly( "Application Layer" );
                LayerAssembly domainLayer = assembly.layerAssembly( "Domain Layer" );
                LayerAssembly infraLayer = assembly.layerAssembly( "Infrastructure Layer" );
                LayerAssembly configurationLayer = assembly.layerAssembly( "Configuration Layer" );
                interfaceLayer.uses( applicationLayer );
                applicationLayer.uses( domainLayer );
                domainLayer.uses( infraLayer );
                infraLayer.uses( configurationLayer );

                ModuleAssembly cxfModule = applicationLayer.moduleAssembly( "Web Service Module" );
                new InterfaceModuleAssembler().assemble( cxfModule );
                ModuleAssembly supportModule = applicationLayer.moduleAssembly( "Domain Support Module" );
                new DomainSupportModuleAssembler().assemble( supportModule );

                ModuleAssembly cargoModule = domainLayer.moduleAssembly( "Cargo Module" );
                new CargoModuleAssembler().assemble( cargoModule );
                ModuleAssembly handlingModule = domainLayer.moduleAssembly( "Handling Module" );
                new HandlingModuleAssembler().assemble( handlingModule );
                ModuleAssembly locationModule = domainLayer.moduleAssembly( "Location Module" );
                new LocationModuleAssembler().assemble( locationModule );
                ModuleAssembly voyageModule = domainLayer.moduleAssembly( "Voyage Module" );
                new VoyageModuleAssembler().assemble( voyageModule );

                ModuleAssembly externalRoutingModule = domainLayer.moduleAssembly( "External Routing Service Module" );
                new RoutingServiceModuleAssembler().assemble( externalRoutingModule );
                new PathFinderModuleAssembler().assemble( externalRoutingModule );

                ModuleAssembly persistenceModule = infraLayer.moduleAssembly( "Persistence Module" );
                new PersistenceModule().assemble( persistenceModule );

                ModuleAssembly configurationModule = domainLayer.moduleAssembly( "Configuration Module" );
                new ConfigurationModule().assemble( configurationModule );
                return assembly;
            }
        };
        application = qi4j.newApplication( applicationAssembler );
        application.activate();

    }
}
