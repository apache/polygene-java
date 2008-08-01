package org.qi4j.lib.struts2.example.actions;

import java.util.ArrayList;
import java.util.List;

import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.lib.struts2.example.Item;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryBuilderFactory;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

@Mixins({ListItems.Mixin.class})
public interface ListItems extends Action, Composite {

    List<Item> list();
    
    abstract class Mixin extends ActionSupport implements ListItems {
        
        @Structure UnitOfWorkFactory uowf;
        
        private List<Item> list;
        
        public List<Item> list() {
            return list;
        }
        
        public String execute() {
            UnitOfWork uow = uowf.currentUnitOfWork();
            QueryBuilderFactory qbf = uow.queryBuilderFactory();
            QueryBuilder<Item> qb = qbf.newQueryBuilder(Item.class);
            list = new ArrayList<Item>();
            for (Item item : qb.newQuery()) {
                list.add(item);
            }
            return SUCCESS;
        }
    }
}
