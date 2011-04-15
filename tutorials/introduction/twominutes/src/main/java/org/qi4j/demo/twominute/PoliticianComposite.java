package org.qi4j.demo.twominute;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;

@Mixins( SpeakerMixin.class )
public interface PoliticianComposite
    extends TransientComposite, Speaker // +others
{
}
