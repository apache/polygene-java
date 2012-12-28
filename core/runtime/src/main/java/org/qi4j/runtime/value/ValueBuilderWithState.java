package org.qi4j.runtime.value;

import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.runtime.structure.ModelModule;
import org.qi4j.runtime.structure.ModuleInstance;

public class ValueBuilderWithState<T> implements ValueBuilder<T>
{
    private final ModelModule<ValueModel> model;
    private ValueInstance prototypeInstance;

    public ValueBuilderWithState( ModelModule<ValueModel> compositeModelModule, ModuleInstance currentModule, ValueStateModel.StateResolver stateResolver ) {
        ValueStateInstance state = new ValueStateInstance( compositeModelModule, currentModule, stateResolver );
        ValueInstance instance = compositeModelModule.model().newValueInstance( compositeModelModule.module(), state );
        instance.prepareToBuild();
        this.model = compositeModelModule;
        this.prototypeInstance = instance;
    }

    @Override
    public T prototype()
    {
        verifyUnderConstruction();
        return prototypeInstance.<T>proxy();
    }

    @Override
    public AssociationStateHolder state()
    {
        verifyUnderConstruction();
        return prototypeInstance.state();
    }

    @Override
    public <K> K prototypeFor( Class<K> mixinType )
    {
        verifyUnderConstruction();

        return prototypeInstance.newProxy( mixinType );
    }

    @Override
    public T newInstance()
        throws ConstructionException
    {
        verifyUnderConstruction();

        // Set correct info's (immutable) on the state
        prototypeInstance.prepareBuilderState();

        // Check that it is valid
        model.model().checkConstraints( prototypeInstance.state() );

        try
        {
            return prototypeInstance.<T>proxy();
        }
        finally
        {
            // Invalidate builder
            prototypeInstance = null;
        }
    }

    private void verifyUnderConstruction()
    {
        if( prototypeInstance == null )
        {
            throw new IllegalStateException( "ValueBuilder instances cannot be reused" );
        }
    }
}
