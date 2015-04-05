package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;

// START SNIPPET: carFactoryMixin2
// START SNIPPET: carFactoryMixin1
public class CarEntityFactoryMixin
        implements CarEntityFactory
{

// END SNIPPET: carFactoryMixin1
    @Structure
    Module module;
// END SNIPPET: carFactoryMixin2
// START SNIPPET: carFactoryMixin3
    public CarEntityFactoryMixin( @Structure Module module )
    {
    }

// END SNIPPET: carFactoryMixin3
// START SNIPPET: createCar
    public Car create(Manufacturer manufacturer, String model)
    {
        UnitOfWork uow = module.currentUnitOfWork();
        EntityBuilder<Car> builder = uow.newEntityBuilder( Car.class );

        Car prototype = builder.instance();
        prototype.manufacturer().set( manufacturer );
        prototype.model().set( model );

        return builder.newInstance();
    }
// END SNIPPET: createCar
}

