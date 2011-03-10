package org.qi4j.bootstrap;

import org.qi4j.api.composite.TransientComposite;

/**
 * TODO
 */
public interface TransientAssembly
{
    Class<? extends TransientComposite> type();
}
