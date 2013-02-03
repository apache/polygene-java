package org.qi4j.api.mixin.decoratorMixin;

public class FooModelImpl
    implements FooModel
{
    private String bar;

    public FooModelImpl( String bar )
    {
        this.bar = bar;
    }

    @Override
    public String getBar()
    {
        return bar;
    }

    public void setBar( String bar )
    {
        this.bar = bar;
    }
}
