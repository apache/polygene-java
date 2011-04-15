package org.qi4j.tutorials.services.step2;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

@Mixins( LibraryMixin.class )
public interface LibraryService
    extends Library, ServiceComposite
{
}