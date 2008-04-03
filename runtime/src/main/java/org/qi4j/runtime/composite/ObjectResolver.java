package org.qi4j.runtime.composite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.spi.composite.ConstructorResolution;
import org.qi4j.spi.composite.FieldResolution;
import org.qi4j.spi.composite.MethodResolution;
import org.qi4j.spi.composite.ObjectModel;
import org.qi4j.spi.composite.ObjectResolution;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.entity.association.AssociationResolution;
import org.qi4j.spi.injection.ResolutionContext;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.structure.ObjectDescriptor;

/**
 * TODO
 */
public final class ObjectResolver
    extends AbstractResolver
{
    public ObjectResolution resolveObjectModel( ObjectDescriptor objectDescriptor, ResolutionContext resolutionContext )
    {
        ObjectModel objectModel = objectDescriptor.getObjectModel();
        List<ConstructorResolution> constructors = new ArrayList<ConstructorResolution>();
        resolveConstructorModel( objectModel.getConstructorModels(), constructors, resolutionContext );
        List<FieldResolution> fields = new ArrayList<FieldResolution>();
        resolveFieldModels( objectModel.getFieldModels(), fields, resolutionContext );
        List<MethodResolution> methods = new ArrayList<MethodResolution>();
        resolveMethodModels( objectModel.getMethodModels(), methods, resolutionContext );

        // Compute set of implemented properties and associations in this mixin
        Map<String, PropertyResolution> propertyResolutions = new HashMap<String, PropertyResolution>();
        for( PropertyModel propertyModel : objectModel.getPropertyModels() )
        {
            String propertyModelName = propertyModel.getName();
            propertyResolutions.put( propertyModelName, new PropertyResolution( propertyModel ) );
        }

        Map<String, AssociationResolution> associationResolutions = new HashMap<String, AssociationResolution>();
        for( AssociationModel associationModel : objectModel.getAssociationModels() )
        {
            String associationModelName = associationModel.getName();
            associationResolutions.put( associationModelName, new AssociationResolution( associationModel ) );
        }

        ObjectResolution objectResolution = new ObjectResolution( objectModel, constructors, fields, methods, propertyResolutions, associationResolutions );
        return objectResolution;
    }
}