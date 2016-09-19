package org.jboss.windup.web.ui;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class KeycloakConfigurationConstants
{
    /**
     * The name of the realm to be used for Windup.
     */
    public static final String WINDUP_REALM = "windup";

    /**
     * The client id to use while connecting to Keycloak.
     */
    public static final String CLIENT_ID = "windup-web";

    /**
     * A system property name containing the Keycloak public key.
     */
    public static final String SYSPROP_KEYCLOAK_REALM_PUBLIC_KEY = "keycloak.realm.public.key";

    /**
     * A system property name containing the Keycloak authentication url.
     */
    public static final String SYSPROP_KEYCLOAK_SERVER_URL = "keycloak.server.url";
}
