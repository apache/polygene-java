package org.qi4j.runtime.composite;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.spi.composite.ConstructorResolution;
import org.qi4j.spi.composite.FieldResolution;
import org.qi4j.spi.composite.MethodResolution;
import org.qi4j.spi.composite.ObjectModel;
import org.qi4j.spi.composite.ObjectResolution;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.dependency.ResolutionContext;

/**
 * TODO
 */
public class ObjectResolver
    extends AbstractResolver
{
    public ObjectResolution resolveObjectModel( ResolutionContext resolutionContext )
    {
        ObjectModel objectModel = (ObjectModel) resolutionContext.getAbstractModel();
        List<ConstructorResolution> constructors = new ArrayList<ConstructorResolution>();
        resolveConstructorModel( objectModel.getConstructorModels(), constructors, resolutionContext );
        List<FieldResolution> fields = new ArrayList<FieldResolution>();
        resolveFieldModels( objectModel.getFieldModels(), fields, resolutionContext );
        List<MethodResolution> methods = new ArrayList<MethodResolution>();
        resolveMethodModels( objectModel.getMethodModels(), methods, resolutionContext );

        // Resolve properties
        List<PropertyResolution> propertyResolutions = new ArrayList<PropertyResolution>(); // TODO

        ObjectResolution objectResolution = new ObjectResolution( objectModel, constructors, fields, methods, propertyResolutions );
        return objectResolution;
    }
}