package org.qi4j.runtime.value;

import org.json.JSONException;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.json.JSONDeserializer;
import org.qi4j.api.json.JSONObjectSerializer;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.structure.ModelModule;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * Implementation of ValueBuilder with a prototype supplied
 */
public class ValueBuilderWithPrototype<T>
    implements ValueBuilder<T>
{
    private ValueInstance prototypeInstance;
    private final ValueModel valueModel;

    public ValueBuilderWithPrototype(ModelModule<ValueModel> compositeModelModule, ModuleInstance currentModule, T prototype)
    {
        valueModel = compositeModelModule.model();
        // Use JSON serialization-deserialization to make a copy of it
        final Object value;
        try
        {
            // @TODO there is probably a more efficient way to do this
            JSONObjectSerializer serializer = new JSONObjectSerializer();
            serializer.serialize(prototype, valueModel.valueType());
            Object object = serializer.getRoot();

            JSONDeserializer deserializer = new JSONDeserializer( currentModule );
            value = deserializer.deserialize(object, valueModel.valueType());
        }
        catch( JSONException e )
        {
            throw new IllegalStateException( "Could not JSON-copy Value", e );
        }

        ValueInstance valueInstance = ValueInstance.getValueInstance( (ValueComposite) value );
        valueInstance.prepareToBuild();
        this.prototypeInstance = valueInstance;
    }

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

    public <K> K prototypeFor( Class<K> mixinType )
    {
        verifyUnderConstruction();
        return prototypeInstance.newProxy( mixinType );
    }

    public T newInstance()
        throws ConstructionException
    {
        verifyUnderConstruction();

        // Set correct info's (immutable) on the state
        prototypeInstance.prepareBuilderState();

        // Check that it is valid
        valueModel.checkConstraints( prototypeInstance.state() );

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
