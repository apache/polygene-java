/*  Copyright 2008 Jan Kronquist.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.javaspaces;

import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.structure.Visibility;
import org.qi4j.test.entity.AbstractEntityStoreTest;

/**
 * JavaSpaces EntityStore test
 */
public class JavaSpacesEntityStoreTest extends AbstractEntityStoreTest
{
    @SuppressWarnings("unchecked")
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        super.assemble( module );

        module.addServices(UuidIdentityGeneratorService.class, JavaSpacesEntityStoreService.class);
        ModuleAssembly config = module.layerAssembly().newModuleAssembly("Config");
        config.setName("config");
        config.addEntities(JavaSpacesConfiguration.class).visibleIn(Visibility.layer);
        config.addServices(MemoryEntityStoreService.class);
    }

    protected TestEntity createEntity(UnitOfWork unitOfWork) throws UnitOfWorkCompletionException
    {
        // Create entity
        TestEntity instance = unitOfWork.newEntity(TestEntity.class);
        instance.identity().get();

        instance.name().set("Test");
        instance.association().set(instance);

        instance.manyAssociation().add(instance);

        instance.listAssociation().add(instance);
        instance.listAssociation().add(instance);
        instance.listAssociation().add(instance);

        instance.setAssociation().add(instance);
        instance.setAssociation().add(instance);
        return instance;
    }
    
    @Test
    public void enableTests()
    {
    }

    @Override @Test
    public void whenNewEntityThenCanFindEntity() throws Exception
    {
        super.whenNewEntityThenCanFindEntity();
    }

    @Override @Test
    public void whenRemovedEntityThenCannotFindEntity() throws Exception
    {
        super.whenRemovedEntityThenCannotFindEntity();
    }
}