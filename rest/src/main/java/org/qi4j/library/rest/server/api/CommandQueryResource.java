/**
 *
 * Copyright 2009-2011 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.server.api;

import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.library.rest.common.Resource;
import org.qi4j.library.rest.common.link.Link;
import org.qi4j.library.rest.server.restlet.RequestReaderDelegator;
import org.qi4j.library.rest.server.restlet.ResponseWriterDelegator;
import org.qi4j.library.rest.server.restlet.ResultConverter;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.*;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.qi4j.api.util.Annotations.isType;
import static org.qi4j.functional.Iterables.*;
import static org.qi4j.library.rest.server.api.RoleMap.current;

/**
 * JAVADOC
 */
public class CommandQueryResource
        implements Uniform
{
   private static final String ARGUMENTS = "arguments";
   private Map<String, Class> interactionClasses = new ConcurrentHashMap<String, Class>();
   private Map<String, Method> resourceMethods = new ConcurrentHashMap<String, Method>();
   private Map<String, Method> contextMethods = new ConcurrentHashMap<String, Method>();

   private
   @Structure
   Qi4jSPI spi;

   protected
   @Structure
   Module module;

   private
   @Service
   ResponseWriterDelegator responseWriter;

   @Service
   RequestReaderDelegator requestReader;

   private
   @Service
   InteractionConstraints constraints;

   private
   @Optional
   @Service
   ResultConverter converter;

   private
   @Uses
   CommandQueryRestlet restlet;

   private Class[] contextClasses;

   public CommandQueryResource(@Uses Class... contextClasses)
   {
      this.contextClasses = contextClasses;

      // Resource method mappings
      for (Method method : getClass().getMethods())
      {
         if (CommandQueryResource.class.isAssignableFrom(method.getDeclaringClass()) && !CommandQueryResource.class.equals(method.getDeclaringClass()))
         {
            Method oldMethod = resourceMethods.put(method.getName().toLowerCase(), method);

            if (oldMethod != null)
            {
               throw new IllegalStateException("Two methods in resource "+getClass().getName()+" with same name "+oldMethod.getName()+", which is not allowed");
            }
         }
      }

      // Context method mappings
      for (Class contextClass : contextClasses)
      {
         for (Method method : contextClass.getMethods())
         {
            if (!method.isSynthetic())
            {
               if (!interactionClasses.containsKey(method.getName().toLowerCase()))
               {
                  interactionClasses.put(method.getName().toLowerCase(), contextClass);
                  contextMethods.put(method.getName().toLowerCase(), method);
               }
            }
         }
      }
   }

   // Uniform implementation
   public final void handle(Request request, Response response)
   {
      RoleMap roleMap = current();

      // Check constraints for this resource
      if (!constraints.isValid(getClass(), roleMap, module))
      {
         throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
      }

      RoleMap.setCurrentRoleMap(new RoleMap(roleMap));

      // Find remaining segments
      List<String> segments = getSegments();

      if (segments.size() > 0)
      {
         String segment = segments.remove(0);

         if (segments.size() > 0)
         {
            handleSubResource(segment);
         } else
         {
            handleResource(segment);
         }
      }
   }

   // API methods
   protected void setResourceValidity(EntityComposite entity)
   {
      ResourceValidity validity = new ResourceValidity(entity, spi);
      current().set(validity);
   }

   protected <T> T context(Class<T> contextClass)
   {
      if (TransientComposite.class.isAssignableFrom(contextClass))
      {
         return module.newTransient( contextClass, toArray( Object.class, current().getAll( Object.class ) ) );
      } else
      {
         return module.newObject( contextClass, toArray( Object.class, current().getAll( Object.class ) ) );
      }
   }

   protected void subResource(Class<? extends CommandQueryResource> subResourceClass)
   {
      restlet.subResource(subResourceClass);
   }

   protected void subResourceContexts(Class<?>... contextClasses)
   {
      restlet.subResourceContexts(contextClasses);
   }

   protected <T> T setRole(Class<T> entityClass, String id, Class... roleClasses)
           throws ResourceException
   {
      try
      {
         T composite = module.unitOfWorkFactory().currentUnitOfWork().get(entityClass, id);
         current().set(composite, roleClasses);
         return composite;
      } catch (EntityTypeNotFoundException e)
      {
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
      } catch (NoSuchEntityException e)
      {
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
      }
   }

   protected <T> T findManyAssociation(ManyAssociation<T> manyAssociation, String id)
           throws ResourceException
   {
      for (T entity : manyAssociation)
      {
         if (entity.toString().equals(id))
         {
            current().set(entity);
            return entity;
         }
      }

      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
   }

   protected void findList(List<?> list, String indexString)
   {
      Integer index = Integer.decode(indexString);

      if (index < 0 || index >= list.size())
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

      current().set(index, Integer.class);

      Object value = list.get(index);
      current().set(value);

   }

   protected Locale getLocale()
   {
      Request request = Request.getCurrent();

      List<Preference<Language>> preferenceList = request.getClientInfo().getAcceptedLanguages();

      if (preferenceList.isEmpty())
         return Locale.getDefault();

      Language language = preferenceList
            .get( 0 ).getMetadata();
      String[] localeStr = language.getName().split( "-" );

      Locale locale;
      switch (localeStr.length)
      {
         case 1:
            locale = new Locale( localeStr[0] );
            break;
         case 2:
            locale = new Locale( localeStr[0], localeStr[1] );
            break;
         case 3:
            locale = new Locale( localeStr[0], localeStr[1], localeStr[2] );
            break;
         default:
            locale = Locale.getDefault();
      }
      return locale;
   }

   // Private implementation
   private void handleSubResource(String segment)
   {
      if (this instanceof SubResources)
      {
         SubResources subResources = (SubResources) this;
         try
         {
            StringBuilder template = (StringBuilder) Request.getCurrent().getAttributes().get("template");
            template.append("resource/");
            subResources.resource(URLDecoder.decode(segment, "UTF-8"));
         } catch (UnsupportedEncodingException e)
         {
            subResources.resource(segment);
         }
      } else
      {
         // Find @SubResource annotated method
         try
         {
            Method method = getSubResourceMethod(segment);

            StringBuilder template = (StringBuilder) Request.getCurrent().getAttributes().get("template");
            template.append(segment).append("/");

            method.invoke(this);
         } catch (Throwable e)
         {
            handleException(Response.getCurrent(), e);
         }
      }
   }

   private Method getSubResourceMethod(String resourceName)
   {
      for (Method method : getClass().getMethods())
      {
         if (method.getName().equalsIgnoreCase(resourceName) && method.getAnnotation(SubResource.class) != null)
            return method;
      }

      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
   }

   private void resource()
   {
      if (Request.getCurrent().getMethod().equals(org.restlet.data.Method.GET))
      {
         RoleMap roleMap = current();

         final List<Method> queries = new ArrayList<Method>();
         final List<Method> commands = new ArrayList<Method>();
         final List<Method> subResources = new ArrayList<Method>();

         // Add commands+queries from the context classes
         for (Class contextClass : contextClasses)
         {
            // Check context class constraints
            if (!constraints.isValid(contextClass, roleMap, module))
               continue; // Skip this class entirely

            // Filter out methods first.
            // TODO Cache this
            Iterable<Method> methods = filter( new Specification<Method>()
                    {
                        public boolean satisfiedBy( Method method )
                        {
                            return !method.isSynthetic() &&
                                    !(method.getDeclaringClass().isAssignableFrom( TransientComposite.class )) &&
                                    !(method.getName().equals( "isValid" )) && !(method.getName().equals( "bind" ));

                        }
                    }, iterable( contextClass.getMethods() ) );

            for (Method method : methods)
            {
               if (constraints.isValid(method, roleMap, module))
                  if (isCommand(method))
                  {
                     commands.add(method);
                  } else
                  {
                     queries.add(method);
                  }
            }
         }

         // Add subresources available from this resource
         if (!SubResources.class.isAssignableFrom(getClass()))
         {
            Iterable<Method> methods = Arrays.asList(getClass().getMethods());

            for (Method method : methods)
            {
               if (method.getAnnotation(SubResource.class) != null && constraints.isValid(method, roleMap, module))
                  subResources.add(method);
            }
         }

         ValueBuilder<Resource> builder = module.valueBuilderFactory().newValueBuilder(Resource.class);
         ValueBuilder<Link> linkBuilder = module.valueBuilderFactory().newValueBuilder(Link.class);
         Link prototype = linkBuilder.prototype();

         if (queries.size() > 0)
         {
            List<Link> queriesProperty = builder.prototype().queries().get();
            prototype.classes().set("query");
            for (Method query : queries)
            {
               prototype.text().set(humanReadable(query.getName()));
               prototype.href().set(query.getName().toLowerCase());
               prototype.rel().set(query.getName().toLowerCase());
               prototype.id().set(query.getName().toLowerCase());
               queriesProperty.add(linkBuilder.newInstance());
            }
         }

         if (commands.size() > 0)
         {
            List<Link> commandsProperty = builder.prototype().commands().get();
            prototype.classes().set("command");
            for (Method command : commands)
            {
               prototype.text().set(humanReadable(command.getName()));
               prototype.href().set(command.getName().toLowerCase());
               prototype.rel().set(command.getName().toLowerCase());
               prototype.id().set(command.getName().toLowerCase());
               commandsProperty.add(linkBuilder.newInstance());
            }
         }

         if (subResources.size() > 0)
         {
            List<Link> resourcesProperty = builder.prototype().resources().get();
            prototype.classes().set("resource");
            for (Method subResource : subResources)
            {
               prototype.text().set(humanReadable(subResource.getName()));
               prototype.href().set(subResource.getName().toLowerCase() + "/");
               prototype.rel().set(subResource.getName().toLowerCase());
               prototype.id().set(subResource.getName().toLowerCase());
               resourcesProperty.add(linkBuilder.newInstance());
            }
         }


         try
         {
            Object index = convert(invokeResource(getInteractionMethod("index")));

            if (index != null && index instanceof ValueComposite)
            {
               builder.prototype().index().set((ValueComposite) index);
            }

         } catch (Throwable e)
         {
            // Ignore
         }

         try
         {
            responseWriter.writeResponse( builder.newInstance(), Response.getCurrent() );
         } catch (Throwable e)
         {
            handleException(Response.getCurrent(), e);
         }
      } else
      {
         Response.getCurrent().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
      }

   }

   private boolean isCommand(Method method)
   {
      return method.getReturnType().equals(Void.TYPE) || method.getName().equals("create");
   }

   /**
    * Transform a Java name to a human readable string by replacing uppercase characters
    * with space+toLowerCase(char)
    * Example:
    * changeDescription -> Change description
    * doStuffNow -> Do stuff now
    *
    * @param name
    * @return
    */
   private String humanReadable(String name)
   {
      StringBuilder humanReadableString = new StringBuilder();

      for (int i = 0; i < name.length(); i++)
      {
         char character = name.charAt(i);
         if (i == 0)
         {
            // Capitalize first character
            humanReadableString.append(Character.toUpperCase(character));
         } else if (Character.isLowerCase(character))
         {
            humanReadableString.append(character);
         } else
         {
            humanReadableString.append(' ').append(Character.toLowerCase(character));
         }
      }

      return humanReadableString.toString();
   }

   private Object createContext(final String interactionName)
   {
      Class contextClass = interactionClasses.get(interactionName);

      if (contextClass == null)
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

      return context(contextClass);
   }

   private void result(Object resultValue) throws Exception
   {
      if (resultValue != null)
      {
         if (!responseWriter.writeResponse( resultValue, Response.getCurrent() ))
         {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No result writer for type " + resultValue.getClass().getName());
         }
      }
   }

   private Object invoke(Object target, Method method, Method contextMethod) throws Throwable
   {
      Object[] arguments;

      // Check context class constraints
      if (!constraints.isValid(contextMethod.getDeclaringClass(), current(), module))
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

      // Check method constraints
      if (!constraints.isValid(contextMethod, current(), module))
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

      if (isCommand(contextMethod))
      {
         // Command

         // Create argument
         arguments = requestReader.readRequest(Request.getCurrent(), method);
         Request.getCurrent().getAttributes().put(ARGUMENTS, arguments);

         // Invoke method
         try
         {
            method.invoke(target, arguments);
            return null; // TODO Get events here
         } catch (IllegalAccessException e)
         {
            throw e;
         } catch (IllegalArgumentException e)
         {
            throw e;
         } catch (InvocationTargetException e)
         {
            throw e.getCause();
         }
      } else
      {
         // Query

         // Create argument
         if (method.getParameterTypes().length > 0)
         {
            try
            {
               arguments = requestReader.readRequest(Request.getCurrent(), method);

               if (arguments == null)
               {
                  // Show form
                  return formForMethod(method);
               }
            } catch (IllegalArgumentException e)
            {
               // Still missing some values - show form
               return formForMethod(method);
            }

         } else
         {
            // No arguments to this query
            arguments = new Object[0];
         }

         // Invoke method
         try
         {
            Request.getCurrent().getAttributes().put(ARGUMENTS, arguments);
            return method.invoke(target, arguments);
         } catch (IllegalAccessException e)
         {
            throw e;
         } catch (IllegalArgumentException e)
         {
            throw e;
         } catch (InvocationTargetException e)
         {
            throw e.getCause();
         }
      }
   }

   private Method getInteractionMethod(String methodName) throws ResourceException
   {
      Method method = contextMethods.get(methodName);

      if (method == null)
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

      return method;
   }

   private List<String> getSegments()
   {
      return (List<String>) Request.getCurrent().getAttributes().get("segments");
   }

   private void handleResource(String segment)
   {
      if (segment.equals("") || segment.equals("."))
      {
         StringBuilder template = (StringBuilder) Request.getCurrent().getAttributes().get("template");
         template.append("resource");

         // Index for this resource
         resource();
      } else
      {
         StringBuilder template = (StringBuilder) Request.getCurrent().getAttributes().get("template");
         template.append(segment);

         Method contextMethod = null;
         try
         {
            contextMethod = getInteractionMethod(segment);
         } catch (ResourceException e)
         {
            // Not found as interaction, try SubResource
            Method resourceMethod = resourceMethods.get(segment);
            if (resourceMethod != null && resourceMethod.getAnnotation(SubResource.class) != null)
            {
               Response.getCurrent().setStatus(Status.REDIRECTION_FOUND);
               Response.getCurrent().setLocationRef(new Reference(Request.getCurrent().getResourceRef().toString()+"/").toString());
               return;
            } else
               throw e;

         }

         if (isCommand(contextMethod))
         {
            handleCommand(contextMethod);
         } else
         {
            handleQuery(contextMethod);
         }
      }
   }

   private void handleCommand(Method contextMethod)
   {
      // Check if this is a request to show the form for this command
      if (shouldShowCommandForm(contextMethod))
      {
         // Show form
         Request.getCurrent().setMethod(org.restlet.data.Method.POST);

         try
         {
            result(formForMethod(contextMethod));
         } catch (Exception e)
         {
            handleException(Response.getCurrent(), e);
         }
      } else
      {
         try
         {
            // Check timestamps
            ResourceValidity validity = RoleMap.role(ResourceValidity.class);
            validity.checkRequest(Request.getCurrent());
         } catch (IllegalArgumentException e)
         {
            // Ignore
         }

         // We have input data - do command
         try
         {
            Object result = invokeResource(contextMethod);

            if (result != null)
            {
               if (result instanceof Representation)
               {
                  Response.getCurrent().setEntity((Representation) result);
               } else
                  result(convert(result));
            }
         } catch (Throwable e)
         {
            handleException(Response.getCurrent(), e);
         }
      }
   }

   private Object invokeResource(Method contextMethod) throws Throwable
   {
      Method method = resourceMethods.get(contextMethod.getName().toLowerCase());

      if (method != null)
         return invoke(this, method, contextMethod);  // Invoke on resource
      else
      {
         Object context = createContext(contextMethod.getName().toLowerCase());
         return invoke(context, contextMethod, contextMethod); // Invoke directly on context
      }
   }

   private boolean shouldShowCommandForm(Method contextMethod)
   {
      // Show form on GET/HEAD
      if (Request.getCurrent().getMethod().isSafe())
         return true;

      if (contextMethod.getParameterTypes().length > 0)
      {
         return !(contextMethod.getParameterTypes()[0].equals(Response.class) || Request.getCurrent().getEntity().isAvailable() || Request.getCurrent().getEntityAsText() != null || Request.getCurrent().getResourceRef().getQuery() != null);
      }

      return false;
   }

   private void handleQuery(Method contextMethod)
   {
      // Query
      // Check if this is a Request.getCurrent() to show the form for this interaction
      if ((Request.getCurrent().getMethod().isSafe() && contextMethod.getParameterTypes().length != 0 && Request.getCurrent().getResourceRef().getQuery() == null) ||
              (!Request.getCurrent().getMethod().isSafe() && contextMethod.getParameterTypes().length != 0 && !(Request.getCurrent().getEntity().isAvailable() || Request.getCurrent().getResourceRef().getQuery() != null || contextMethod.getParameterTypes()[0].equals(Response.class))))
      {
         // Show form
         try
         {
            result(formForMethod(contextMethod));
         } catch (Exception e)
         {
            handleException(Response.getCurrent(), e);
         }
      } else
      {
         try
         {
            // Check timestamps
            ResourceValidity validity = RoleMap.role(ResourceValidity.class);
            validity.checkRequest(Request.getCurrent());
         } catch (IllegalArgumentException e)
         {
            // Ignore
         }

         // We have input data - do query
         try
         {
            Object result = invokeResource(contextMethod);
            if (result != null)
            {
               if (result instanceof Representation)
               {
                  Response.getCurrent().setEntity((Representation) result);
               } else
                  result(convert(result));
            }
         } catch (IllegalAccessException e)
         {
            Response.getCurrent().setStatus(Status.CLIENT_ERROR_NOT_FOUND);

         } catch (Throwable e)
         {
            handleException(Response.getCurrent(), e);
         }
      }
   }

   private Object convert( Object result )
   {
      if (converter != null)
         result = converter.convert(result, Request.getCurrent(), (Object[]) Request.getCurrent().getAttributes().get(ARGUMENTS));

      return result;
   }

   private void handleException(Response response, Throwable ex)
   {
      while (ex instanceof InvocationTargetException)
      {
         ex = ex.getCause();
      }

      try
      {
         throw ex;
      } catch (ResourceException e)
      {
         // IAE (or subclasses) are considered client faults
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(e.getStatus());
      } catch (ConstraintViolationException e)
      {
         try
         {
            ConstraintViolationMessages cvm = new ConstraintViolationMessages();

            // CVE are considered client faults
            String messages = "";
            Locale locale = RoleMap.role(Locale.class);
            for (ConstraintViolation constraintViolation : e.constraintViolations())
            {
               if (!messages.equals(""))
                  messages += "\n";
               messages += cvm.getMessage(constraintViolation, locale);
            }

            response.setEntity(new StringRepresentation(messages));
            response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
         } catch (Exception e1)
         {
            response.setEntity(new StringRepresentation(e.getMessage()));
            response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
         }
      } catch (IllegalArgumentException e)
      {
         // IAE (or subclasses) are considered client faults
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
      } catch (RuntimeException e)
      {
         // RuntimeExceptions are considered server faults
         LoggerFactory.getLogger(getClass()).warn("Exception thrown during processing", e);
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(Status.SERVER_ERROR_INTERNAL);
      } catch (Exception e)
      {
         // Checked exceptions are considered client faults
         String s = e.getMessage();
         if (s == null)
            s = e.getClass().getSimpleName();
         response.setEntity(new StringRepresentation(s));
         response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
      } catch (Throwable e)
      {
         // Anything else are considered server faults
         LoggerFactory.getLogger(getClass()).error("Exception thrown during processing", e);
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(Status.SERVER_ERROR_INTERNAL);
      }
   }

   private Form formForMethod(Method contextMethod)
   {
      Form form = new Form();

      Form queryAsForm = Request.getCurrent().getResourceRef().getQueryAsForm();
      Form entityAsForm = null;
      Representation representation = Request.getCurrent().getEntity();
      if (representation != null && !EmptyRepresentation.class.isInstance(representation))
      {
         entityAsForm = new Form(representation);
      } else
         entityAsForm = new Form();

      Class valueType = contextMethod.getParameterTypes()[0];
      if (ValueComposite.class.isAssignableFrom(valueType))
      {
         ValueDescriptor valueDescriptor = module.valueDescriptor(valueType.getName());

         for (PropertyDescriptor propertyDescriptor : valueDescriptor.state().properties())
         {
            String value = getValue(propertyDescriptor.qualifiedName().name(), queryAsForm, entityAsForm);

            if (value == null)
            {
                Object initialValue = propertyDescriptor.initialValue(module);
                if (initialValue != null)
                    value = initialValue.toString();
            }

            form.add(propertyDescriptor.qualifiedName().name(), value);
         }
      } else if (valueType.isInterface() && contextMethod.getParameterTypes().length == 1)
      {
         // Single entity as input
         form.add("entity", getValue("entity", queryAsForm, entityAsForm));
      } else
      {
         // Construct form out of individual parameters instead
         int idx = 0;
         for (Annotation[] annotations : contextMethod.getParameterAnnotations())
         {
            Name name = (Name) first( filter( isType( Name.class ), iterable( annotations ) ) );

            String value = getValue(name.value(), queryAsForm, entityAsForm);

            String paramName;
            if (name != null)
            {
               paramName = name.value();
            } else
            {
               paramName = "param" + idx;
            }

            form.add(paramName, value);

            idx++;
         }
      }

      return form;
   }

   private String getValue(String name, Form queryAsForm, Form entityAsForm)
   {
      String value = queryAsForm.getFirstValue(name);
      if (value == null)
         value = entityAsForm.getFirstValue(name);
      return value;
   }
}
