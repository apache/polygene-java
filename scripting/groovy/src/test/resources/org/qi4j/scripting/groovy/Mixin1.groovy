import org.qi4j.scripting.groovy.Mixin1

public class Mixin1
{
    def self
    def counter = 0

    public String do1()
    {
        counter = counter + 1
        return "do1() in Groovy:" + counter
    }
}
