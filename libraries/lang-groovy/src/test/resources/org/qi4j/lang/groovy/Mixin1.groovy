public class Mixin1
{
    def This
    def counter = 0

    public String do1()
    {
        counter = counter + 1
        return "do1() in Groovy:" + counter
    }
}
