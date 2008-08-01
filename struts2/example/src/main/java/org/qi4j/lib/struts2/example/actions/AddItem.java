package org.qi4j.lib.struts2.example.actions;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.lib.struts2.example.Item;

@Mixins( AddItem.Mixin.class )
public interface AddItem extends Preparable, Action, Composite
{

    Item getItem();

    String input();

    void prepareExecute();

    abstract class Mixin extends ActionSupport implements AddItem
    {
        @Structure UnitOfWorkFactory uowf;

        private static final long serialVersionUID = 1L;

        EntityBuilder<Item> builder;

        public Item getItem()
        {
            return builder.stateOfComposite();
        }

        public void prepare()
        {
        }

        @Override
        public final String input()
        {
            createEntityBuilder();
            return INPUT;
        }

        public void prepareExecute()
        {
            createEntityBuilder();
        }

        @Override
        public final String execute()
        {
            builder.newInstance();
            return SUCCESS;
        }

        private void createEntityBuilder()
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            builder = uow.newEntityBuilder( Item.class );
        }
    }
}
