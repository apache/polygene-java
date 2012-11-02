/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.http;

import java.security.SecureRandom;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.GreaterThan;
import org.qi4j.library.constraints.annotation.OneOf;

/**
 * Configuration for {@link SecureJettyMixin}.
 *
 * Only the three keystore related properties are mandatory, all the other ones have sensible defaults.
 */
// START SNIPPET: configssl
public interface SecureJettyConfiguration
        extends JettyConfiguration
{

    /**
     * @return Type of the keystore that contains the {@link SecureJettyService} certificate.
     */
    @OneOf( { "PKCS12", "JCEKS", "JKS" } )
    Property<String> keystoreType();

    /**
     * @return Path of the keystore that contains the {@link SecureJettyService} certificate.
     */
    Property<String> keystorePath();

    /**
     * @return Password of the keystore that contains the {@link SecureJettyService} certificate.
     */
    @UseDefaults
    Property<String> keystorePassword();

    /**
     * Alias of the {@link SecureJettyService} certificate.
     *
     * If not set, the first certificate found in the keystore is used.
     *
     * @return Alias of the {@link SecureJettyService} certificate.
     */
    @Optional
    Property<String> certAlias();

    /**
     * @return Type of the keystore that contains the certificates trusted by the {@link SecureJettyService}.
     */
    @Optional
    @OneOf( { "PKCS12", "JCEKS", "JKS" } )
    Property<String> truststoreType();

    /**
     * @return Path of the keystore that contains the certificates trusted by the {@link SecureJettyService}.
     */
    @Optional
    Property<String> truststorePath();

    /**
     * @return Password of the keystore that contains the certificates trusted by the {@link SecureJettyService}.
     */
    @UseDefaults
    Property<String> truststorePassword();

    /**
     * If the {@link SecureJettyService} wants client authentication.
     *
     * Defaults to false. If set to true, the {@link SecureJettyService} will expose the fact that it can handle client
     * certificate based authentication.
     *
     * @return If the {@link SecureJettyService} wants client authentication.
     */
    @UseDefaults
    Property<Boolean> wantClientAuth();

    /**
     * If the {@link SecureJettyService} needs client authentication.
     *
     * Defaults to false. If set to true, only mutually authentified connections will be accepted.
     *
     * @return If the {@link SecureJettyService} needs client authentication.
     */
    @UseDefaults
    Property<Boolean> needClientAuth();

    /**
     * The algorithm used by {@link SecureRandom} for SSL operations.
     *
     * Default JVM algorithm is used if omitted.
     *
     * @return The algorithm used by {@link SecureRandom} for SSL operations.
     */
    @Optional
    Property<String> secureRandomAlgorithm();

    /**
     * @return Coma separated list of included cipher suites.
     */
    @Optional
    Property<String> includeCipherSuites();

    /**
     * @return Coma separated list of excluded cipher suites.
     */
    @Optional
    Property<String> excludeCipherSuites();

    /**
     * If SSL Session caching is enabled.
     *
     * SSL Session caching is enabled by default.
     *
     * @return If SSL Session caching is enabled.
     */
    @Optional
    Property<Boolean> cacheSslSessions();

    /**
     * If SSL/TLS renegotiation is allowed.
     *
     * Defaults to false. Setting this to true can open vulnerabilities, be sure of what you are doing.
     *
     * @return If SSL/TLS renegotiation is allowed.
     */
    @UseDefaults
    Property<Boolean> allowRenegotiation();

    /**
     * Maximum number of intermediate certificates in the PKIX path.
     *
     * Set to -1 for unlimited. Defaulted to -1.
     *
     * @return Maximum number of intermediate certificates in the PKIX path
     */
    @Optional
    @GreaterThan( -2 )
    Property<Integer> maxCertPathLength();

    /**
     * If the {@link SecureJettyService} certificate MUST be PKIX validated.
     *
     * <p/><b>IMPORTANT:</b>
     * <ul>
     *   <li>Server certificate validation do not use the configured truststore but the one of the JVM.</li>
     *   <li>Server certificates validation behavior depends on CRL and OCSP related configuration properties.</li>
     * </ul>
     * <p/>
     * Defaults to false.
     *
     * @return If the {@link SecureJettyService} certificate MUST be PKIX validated.
     */
    @UseDefaults
    Property<Boolean> validateServerCert();

    /**
     * If client certificates PKIX validation MUST use either CRL or OCSP.
     *
     * <p/><b>IMPORTANT:</b>
     * <ul>
     *   <li>Peer certificates validation use the configured truststore if present, the one of the JVM if not.</li>
     *   <li>Peer certificates validation behavior depends on CRL and OCSP related configuration properties.</li>
     * </ul>
     * <p/>
     * Defaults to false.
     *
     * @return If client certificates PKIX validation MUST use either CRL or OCSP.
     */
    @UseDefaults
    Property<Boolean> validatePeerCerts();

    /**
     * @return The path of a local CRL file in PEM or DER format used during PKIX validations.
     */
    @Optional
    Property<String> crlFilePath();

    /**
     * @return If PKIX validations use the CRL Distribution Points declared in CA certificates.
     */
    @UseDefaults
    Property<Boolean> enableCRLDP();

    /**
     * @return If PKIX validations use the OCSP protocol against responders declared in CA certificates.
     */
    @UseDefaults
    Property<Boolean> enableOCSP();

    /**
     * @return The URL of an OCSP responder to use during PKIX validations.
     */
    @Optional
    Property<String> ocspResponderURL();

}
// END SNIPPET: configssl
