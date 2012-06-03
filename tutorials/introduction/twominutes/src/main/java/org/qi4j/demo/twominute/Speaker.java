package org.qi4j.demo.twominute;

import org.qi4j.api.mixin.Mixins;

// START SNIPPET: documentation
@Mixins( SpeakerMixin.class )
public interface Speaker
{
    String sayHello();
}
// END SNIPPET: documentation
