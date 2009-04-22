package org.qi4j.library.struts2.support;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.Validateable;
import com.opensymphony.xwork2.ValidationAware;

public interface StrutsAction
    extends Action, Validateable, ValidationAware, TextProvider, LocaleProvider
{
}
