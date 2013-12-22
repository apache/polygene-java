package org.qi4j.runtime.structure;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.functional.Specification;

/**
 * TODO
 */
public class VisibilitySpecification
    implements Specification<ModelDescriptor>
{
    public static final Specification<ModelDescriptor> MODULE = new VisibilitySpecification( Visibility.module );
    public static final Specification<ModelDescriptor> LAYER = new VisibilitySpecification( Visibility.layer );
    public static final Specification<ModelDescriptor> APPLICATION = new VisibilitySpecification( Visibility.application );

    private final Visibility visibility;

    public VisibilitySpecification( Visibility visibility )
    {
        this.visibility = visibility;
    }

    @Override
    public boolean satisfiedBy( ModelDescriptor item )
    {
        return item.visibility().ordinal() >= visibility.ordinal();
    }
}
