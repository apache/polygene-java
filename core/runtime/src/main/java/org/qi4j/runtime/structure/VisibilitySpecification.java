package org.qi4j.runtime.structure;

import java.util.function.Predicate;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.ModelDescriptor;

/**
 * TODO
 */
public class VisibilitySpecification
    implements Predicate<ModelDescriptor>
{
    public static final Predicate<ModelDescriptor> MODULE = new VisibilitySpecification( Visibility.module );
    public static final Predicate<ModelDescriptor> LAYER = new VisibilitySpecification( Visibility.layer );
    public static final Predicate<ModelDescriptor> APPLICATION = new VisibilitySpecification( Visibility.application );

    private final Visibility visibility;

    public VisibilitySpecification( Visibility visibility )
    {
        this.visibility = visibility;
    }

    @Override
    public boolean test( ModelDescriptor item )
    {
        return item.visibility().ordinal() >= visibility.ordinal();
    }
}
