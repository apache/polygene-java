package org.qi4j.lib.struts2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ognl.ObjectPropertyAccessor;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;

import org.qi4j.composite.Composite;
import org.qi4j.composite.ConstraintViolation;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.lib.struts2.ConstraintViolationInterceptor.FieldConstraintViolations;
import org.qi4j.property.Property;

import com.opensymphony.xwork2.util.XWorkConverter;

/**
 * <p>An implementation of the ObjectPropertyAccessor that provides conversion for Qi4j properties.  The typical way that
 * OGNL gets/sets object attributes is by finding the corresponding JavaBean getter/setter methods.  This 
 * ObjectPropertyAccessor checks if there is a Qi4j property on the Composite and if there is uses the properties
 * get/set methods.</p>
 * 
 * <p>When setting Property values, if a ConstraintViolationException is thrown it is added to the context so that
 * it can be processed and by the ConstraintViolationInterceptor, similar to how conversion exceptions are handled by
 * the ConversionErrorInterceptor</p>
 * 
 * <p>NOTE: We can't do this as a regular converter because Qi4j composites doesn't (nor should it be) following the
 * JavaBean standard.  We might be able to only override the getProperty() method here and have regular converters for
 * Property, Association and SetAssociation but I haven't tried that yet so it may not work as expected.</>
 * 
 * <p>TODO: Doesn't yet handle the Association or SetAssociation, but these should be easy to add</p>
 */
public class Qi4jPropertyAccessor extends ObjectPropertyAccessor {

//    private Qi4jSPI spi;
//
//    @Inject
//    public void setQi4jSPI(Qi4jSPI spi) {
//        this.spi = spi;
//    }
    
    private final Property getPropertyInstance(OgnlContext context, Composite target, String name) throws OgnlException {
        // TODO: I'd prefer to use the SPI to get the PropertyDescriptor as it seems like that would be the "safer"
        //       thing to do.  It might also be faster being that Qi4j figures out all that stuff ahead of time which
        //       would make these simple lookups and not have the overhead that doing target.getClass().getMethod(name)
        //       might.  The reason I don't do this yet is because if the target isn't an instance and is instead a 
        //       state object from entityBuilder.stateOfComposite() or compositeBuilder.stateOfComposite(), the
        //       spi.getCompositeDescriptor(target) method throws an exception.
//        try {
//            PropertyDescriptor descriptor = spi.getCompositeDescriptor(target).state().getPropertyByName(name);
//            if (descriptor == null) {
//                return null;
//            }
//            return (Property) descriptor.accessor().invoke(target);
//        } catch (IllegalArgumentException e) {
//            throw new OgnlException(name, e);
//        } catch (IllegalAccessException e) {
//            throw new OgnlException(name, e);
//        } catch (InvocationTargetException e) {
//            throw new OgnlException(name, e);
//        }
        Method m;
        try {
            m = target.getClass().getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (SecurityException e) {
            throw new OgnlException(name, e);
        }

        try {
            return (Property) m.invoke(target);
        } catch (IllegalArgumentException e) {
            throw new OgnlException(name, e);
        } catch (IllegalAccessException e) {
            throw new OgnlException(name, e);
        } catch (InvocationTargetException e) {
            throw new OgnlException(name, e);
        }
    }
    
    @Override
    public Object getProperty(Map context, Object target, Object oname) throws OgnlException {
        String name = oname.toString();
        Property property = getPropertyInstance((OgnlContext) context, (Composite) target, name);
        if (property == null) {
            return super.getProperty(context, target, name);
        }
        return property.get();
    }

    @Override
    public void setProperty(Map context, Object otarget, Object oname, Object value) throws OgnlException {
        OgnlContext ognlContext = (OgnlContext) context;
        String name = oname.toString();
        Composite target = (Composite) otarget;
        Property property = getPropertyInstance(ognlContext, target, name);
        if (property == null) {
            super.setProperty(context, target, name, value);
            return;
        }

        Object convertedValue = OgnlRuntime.getConvertedType(ognlContext, target, null, name, value, (Class) property.type());
        try {
            property.set(convertedValue);
        } catch (ConstraintViolationException e) {
            handleConstraintViolation(context, target, name, convertedValue, e.constraintViolations());
        }
    }

    protected void handleConstraintViolation(Map context, Composite target, String property, Object value, Collection<ConstraintViolation> constraintViolations) {
        String realProperty = property;
        String fullName = (String) context.get(XWorkConverter.CONVERSION_PROPERTY_FULLNAME);

        if (fullName != null) {
            realProperty = fullName;
        }

        Map<String, FieldConstraintViolations> allPropertyConstraintViolations = (Map) context.get(ConstraintViolationInterceptor.CONTEXT_CONSTRAINT_VIOLATIONS);

        if (allPropertyConstraintViolations == null) {
            allPropertyConstraintViolations = new HashMap<String, FieldConstraintViolations>();
            context.put(ConstraintViolationInterceptor.CONTEXT_CONSTRAINT_VIOLATIONS, allPropertyConstraintViolations);
        }

        allPropertyConstraintViolations.put(realProperty, new FieldConstraintViolations(target, property, value, constraintViolations));
    }
}
