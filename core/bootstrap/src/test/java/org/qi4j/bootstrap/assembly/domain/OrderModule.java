package org.qi4j.bootstrap.assembly.domain;

import org.qi4j.api.association.Association;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.layered.ModuleAssembler;

public class OrderModule
    implements ModuleAssembler
{
    @Override
    public ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Order.class, Customer.class );
        module.values( Address.class );
        return module;
    }

    public interface Order
    {
        Association<Customer> customer();

        Property<Address> invoicingAddress();

        Property<Address> deliveryAddress();
    }

    public interface Customer
    {
    }

    public interface Address
    {
    }
}
