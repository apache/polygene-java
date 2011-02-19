package org.qi4j.demo.twominute;

public class SpeakerMixin
    implements Speaker
{
    public String sayHello()
    {
        return "Hello, World!";
    }
}