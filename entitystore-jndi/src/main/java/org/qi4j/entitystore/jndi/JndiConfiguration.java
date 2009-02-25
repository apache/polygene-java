/*
 * Copyright 2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.jndi;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.configuration.ConfigurationComposite;

/**
 * Configuration for the JNDI EntityStore.
 * <p>
 * Many of the Property instances are passed on to the <code>javax.naming.InitialContext</code> as is, and the javadoc
 * below mirrors the meaning found for the constants in the <code>javax.naming.Context</code> interface.
 * </p>
 */
public interface JndiConfiguration extends ConfigurationComposite
{

    /**
     * If set to read-only mode, then no creation and no updates of the EntityStore will be allowed.
     *
     * @return if true, then don't allow updates of the underlying store. Such operations will throw a
     *         ReadOnlyEntityStoreException.
     */
    @UseDefaults Property<Boolean> readOnly();

    /** Instance version property.
     *
     * If the EntityStore is NOT readonly, each entry must have an attribute to store the instance version. This
     * Property names that attribute available in all entries.
     *
     * If not specified, the atttribute used will be "instanceVersion".
     *
     * @return The name of the attribute where to store the instance version number.
     */
    @Optional Property<String> versionAttribute();

    /** Last modified property.
     * If the EntityStore is NOT readonly, each entry must have an attribute to store the last modified date.
     * This property names that attributes in all entries.
     *
     * If not specified, the attribute used will be "lastModifiedDate".
     *
     * @return The name of the attribute where to store the last modified date.
     */
    @Optional Property<String> lastModifiedDateAttribute();

    /** QualifiedType attribute.
     *
     * If the EntityStore is going to support the Iterable interface, it must store the qualifiedType in an
     * attribute in the entry. This is also required for non-readOnly stores. 
     *
     * @return The name of the attribute where to store the qualifiedType.
     */
    @Optional Property<String> qualifiedTypeAttribute();

    /**
     * Whether Turbo Mode for serialization should be used or not.
     *
     * @return Whether turbo mode is enabled or not.
     */
    @UseDefaults Property<Boolean> turboMode();

    /** The Distinguished Name of the base.
     * All creation and retrieval will occur in this directory.
     *
     * @return The property of the DN to the directory of operation.
     */
    Property<String> baseDN();

    /**
     * This will provide the name of the attribute where the binary blob of the properties will be stored.
     * <p>
     * If there is no attribute name provided, each property will be stored in its own attribute, of the same
     * name as the property itself.
     * </p>
     * <p>
     * Note: Associations are always stored in attributes with the same name as the attribute itself. It doesn't
     * allow for aggregated/collapsing of all into a single attribute as is possible for property data.
     * </p>
     *
     * @return The property containing the attribute name, or empty if property collapsing should not occur.
     */
    @Optional Property<String> propertyAttribute();


    /**
     * The attribute to use for storing the identity in an entry.
     *
     * <p>
     * If there is no attribute name provided, then use "uid" as the identity attribute in the entry.
     * </p>
     *
     * @return
     */
    @UseDefaults Property<String> identityAttribute();

    /**
     * Property for specifying the authoritativeness of the service requested.
     * <p>
     * If the value of the property is the string "true", it means that the access is to the most authoritative
     * source (i.e. bypass any cache or replicas). If the value is anything else, the source need not be (but may be)
     * authoritative. If unspecified, the value defaults to "false".
     * </p>
     *
     * @return The property containing the string "true" or "false", whether the source is authorative or not.
     */
    @Optional Property<String> authorative();

    /**
     * Property for specifying the batch size to use when returning data via the service's protocol.
     * <p>
     * This is a hint to the provider to return the results of operations in batches of the specified size, so the
     * provider can optimize its performance and usage of resources. The value of the property is the string
     * representation of an integer. If unspecified, the batch size is determined by the service provider.
     *
     * @return The property that contains the batch size.
     */
    @Optional Property<String> batchSize();

    /**
     * Property for specifying the list of control factories to use.
     * <p>
     * The value of the property should be a colon-separated list of the fully qualified class names of factory
     * classes that will create a control given another control.
     * </p>
     *
     * @return The property that possibly contains the control factory(ies) class names.
     */
    @Optional Property<String> controlFactories();

    /**
     * Property for specifying the DNS host and domain names to use for the JNDI URL context (for example,
     * "dns://somehost/wiz.com").
     *
     * <p>
     * This property may be specified in the environment, an applet parameter, a system property, or a resource file.
     * If it is not specified in any of these sources and the program attempts to use a JNDI URL containing a DNS
     * name, a ConfigurationException will be thrown.
     * </p>
     *
     * @return
     */
    @Optional Property<String> dnsUrl();

    /**
     * Property for specifying the initial context factory to use.
     * <p>
     * The value of the property should be the fully qualified class name of the factory class that will create an
     * initial context. If it is not specified in any of these sources, NoInitialContextException is thrown when an
     * initial context is required to complete an operation.
     * </p>
     *
     * @return
     */
    @Optional Property<String> initialContextFactory();

    /**
     * Property for specifying the preferred language to use with the service.
     *
     * <p>
     * The value of the property is a colon-separated list of language tags as defined in RFC 1766. If this property
     * is unspecified, the language preference is determined by the service provider.
     * </p>
     *
     * @return
     */
    @Optional Property<String> language();

    /**
     * Property for specifying the list of object factories to use.
     * <p>
     * The value of the property should be a colon-separated list of the fully qualified class names of factory classes
     * that will create an object given information about the object.
     * </p>
     *
     * @return
     */
    @Optional Property<String> objectFactories();

    /**
     * The URL string to connect to the LDAP server.
     *
     * property for specifying configuration information for the service provider to use. The value of the property
     * should contain a URL string (e.g. "ldap://somehost:389"). It may also contain connect arguments as query
     * arguments in the URL.
     *
     * @return The connection string to use to connect.
     */
    Property<String> providerUrl();

    /**
     * A property to define how referrals should be dealt with.
     * <p>
     * The value of the property is one of the following strings:
     * <code><pre>
     * "follow" follow referrals automatically
     * "ignore" ignore referrals
     * "throw" throw ReferralException when a referral is encountered.
     * If this property is not specified, the default is determined by the provider.
     */
    @Optional Property<String> referral();

    /**
     * What type of authentication should be used.
     * <p>
     * Its value is one of the following strings: "none", "simple", "strong". If this property is unspecified, the
     * behaviour is determined by the service provider.
     * </p>
     *
     * @return
     */
    @Optional Property<String> securityAuthentication();

    /**
     * The Principal passworf to log into the LDAP server.
     *
     * <p>
     * The value of the property depends on the authentication scheme. For example, it could be a hashed password,
     * clear-text password, key, certificate, and so on.
     * </p>
     *
     * @return The password to use to log in.
     */
    Property<String> securityCredentials();

    /**
     * The Principal Name to log into the LDAP server.
     *
     * @return The username to use to log in.
     */
    Property<String> securityPrincipal();

    /**
     * The Security protocol, if any, to be used.
     *
     * @return The protocol that should be used, or null if defined by the provider.
     */
    @Optional Property<String> securityProtocol();

    /**
     * Property for specifying the list of state factories to use.
     * <p>
     * The value of the property should be a colon-separated list of the fully qualified class names of state factory
     * classes that will be used to get an object's state given the object itself.
     * </p>
     *
     * @return
     */
    @Optional Property<String> stateFactories();

    /**
     * Property for specifying the list of package prefixes to use when loading in URL context factories.
     * <p>
     * The value of the property should be a colon-separated list of package prefixes for the class name of the
     * factory class that will create a URL context factory.
     * The prefix com.sun.jndi.url is always appended to the possibly empty list of package prefixes.
     * </p>
     *
     * @return
     */
    @Optional Property<String> urlPkgPrefixes();

}