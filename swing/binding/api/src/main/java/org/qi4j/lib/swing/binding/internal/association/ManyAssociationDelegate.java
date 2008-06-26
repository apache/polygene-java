package org.qi4j.lib.swing.binding.internal.association;

import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.lib.swing.binding.SwingBinding;
import org.qi4j.lib.swing.binding.internal.BoundField;

/**
 * @author Lan Boon Ping
 */
public interface ManyAssociationDelegate<T, K extends ManyAssociation<T>> extends BoundField<K, K>, SwingBinding<T>
{

}
