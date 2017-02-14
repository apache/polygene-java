package org.apache.polygene.api.property;

import java.util.function.BiFunction;
import org.apache.polygene.api.structure.Module;

public interface InitialValueProvider extends BiFunction<Module, PropertyDescriptor, Object>
{
}
