package org.qi4j.lib.struts2.example.actions;

import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.lib.struts2.example.Item;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;

@Mixins({EditItem.Mixin.class})
public interface EditItem extends Preparable, Action, Composite {

    String getId();
    void setId(String id);
    
    Item getItem();

    String input() throws Exception;
    
    abstract class Mixin extends ActionSupport implements EditItem {
        @Structure UnitOfWorkFactory uowf;
        
        private String id;
        private Item item;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public Item getItem() {
            return item;
        }
        
        public void prepare() {
            UnitOfWork uow = uowf.currentUnitOfWork();
            item = uow.find(id, Item.class);
        }
    }
}
