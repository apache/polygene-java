package org.qi4j.demo.twominute;

// START SNIPPET: documentation
public class SpeakerMixin
    implements Speaker
{
    @Override
    public String sayHello()
    {
        return "Hello, World!";
    }
}
// END SNIPPET: documentation
