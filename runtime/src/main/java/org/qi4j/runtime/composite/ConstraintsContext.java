package org.qi4j.runtime.composite;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.qi4j.composite.Constraint;
import org.qi4j.spi.composite.ConstraintBinding;
import org.qi4j.spi.composite.ConstraintModel;
import org.qi4j.spi.composite.ConstraintResolution;
import org.qi4j.spi.composite.ConstraintsBinding;

/**
 * TODO
 */
public final class ConstraintsContext
{
    private ConstraintsBinding constraintsBinding;

    public ConstraintsContext( ConstraintsBinding constraintsBinding )
    {
        this.constraintsBinding = constraintsBinding;
    }

    public ConstraintsInstance newInstance()
    {
        List<ConstraintInstance> constraintInstances = new ArrayList<ConstraintInstance>();
        Map<Annotation, ConstraintBinding> constraintBindings = constraintsBinding.getConstraintBindings();
        for( Map.Entry<Annotation, ConstraintBinding> entry : constraintBindings.entrySet() )
        {
            ConstraintBinding constraintBinding = entry.getValue();
            ConstraintResolution constraintResolution = constraintBinding.getConstraintResolution();
            ConstraintModel constraintModel = constraintResolution.getConstraintModel();
            Class<? extends Constraint> constraintType = constraintModel.getConstraintType();
            try
            {
                Constraint constraintInstance = constraintType.newInstance();
                constraintInstances.add( new ConstraintInstance( constraintInstance, entry.getKey() ) );
            }
            catch( Exception e )
            {
                throw new org.qi4j.composite.InstantiationException( "Could not instantiate constraint " + constraintType.getName(), e );
            }
        }
        return new ConstraintsInstance( constraintInstances );
    }
}
