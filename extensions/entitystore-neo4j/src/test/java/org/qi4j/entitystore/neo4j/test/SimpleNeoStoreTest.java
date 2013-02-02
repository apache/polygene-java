package org.qi4j.entitystore.neo4j.test;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.neo4j.NeoConfiguration;
import org.qi4j.entitystore.neo4j.NeoEntityStoreService;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

public class SimpleNeoStoreTest
        extends AbstractEntityStoreTest
{
   public void assemble(ModuleAssembly module)
           throws AssemblyException
   {
      module.layer().application().setName("SimpleNeoTest");

      super.assemble(module);
      module.services(FileConfigurationService.class);
      module.services(NeoEntityStoreService.class);
      module.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );

      ModuleAssembly configModule = module.layer().module("config");
      configModule.entities(NeoConfiguration.class).visibleIn(Visibility.layer);
      configModule.services(MemoryEntityStoreService.class);
      configModule.services(UuidIdentityGeneratorService.class);
   }

   @Override
   public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
   {
   }
}
