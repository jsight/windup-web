package org.jboss.windup.web.ui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@WebServlet(urlPatterns = "/autologin")
public class AutologinServlet extends HttpServlet
{
    public static final String DEFAULT_USER = "guest";
    public static final String DEFAULT_PASSWORD = "guest";
    private static Logger LOG = Logger.getLogger(AutologinServlet.class.getName());

    private static AccessTokenResponse getAccessToken() {
        LOG.info("Creating authorization client...");
        // create a new instance based on the configuration define at keycloak-authz.json
        String authServerUrl = System.getProperty(KeycloakConfigurationConstants.SYSPROP_KEYCLOAK_SERVER_URL);
        String realm = KeycloakConfigurationConstants.WINDUP_REALM;
        String clientid = KeycloakConfigurationConstants.CLIENT_ID;
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", "public");
        clientCredentials.put("ssl-required", "external");
        clientCredentials.put("public-client", true);
        clientCredentials.put("client-id", clientid);

        Configuration configuration = new Configuration(authServerUrl, realm, clientid, clientCredentials, null);
        AuthzClient authzClient = AuthzClient.create(configuration);
        LOG.info("Requesting token...");

        AccessTokenResponse accessTokenResponse = authzClient.obtainAccessToken(DEFAULT_USER, DEFAULT_PASSWORD);

        Map<String, String> otherClaims = new HashMap<>();
        ResteasyClient client = new ResteasyClientBuilder()
                .register(new ClientResponseFilter()
                {
                    @Override
                    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException
                    {
                        System.out.println("Cookies: " + responseContext.getCookies());
                        if (responseContext.getCookies().get("KEYCLOAK_IDENTITY") != null)
                        {
                            otherClaims.put("cookie", responseContext.getCookies().get("KEYCLOAK_IDENTITY").getValue());
                        }
                    }
                })
                .connectionPoolSize(10)
                .build();
        Keycloak kc = null;
//        new Keycloak(authServerUrl,
//                "master", // the realm to log in to
//                "admin", "password", // the user
//                "admin-cli",
//                null,
//                OAuth2Constants.PASSWORD,
//                client);
        try {
            Constructor<Keycloak> keycloakConstructor = Keycloak.class.getDeclaredConstructor(String.class, String.class, String.class, String.class, String.class, String.class, String.class, ResteasyClient.class);
            keycloakConstructor.setAccessible(true);
            kc = keycloakConstructor.newInstance(authServerUrl,
                    "master", // the realm to log in to
                    "admin", "password", // the user
                    "admin-cli",
                    null,
                    OAuth2Constants.PASSWORD,
                    client);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        UserRepresentation representation = kc.realm("windup").users().search("guest", null, null, null, 0, 1000).get(0);
        Map<String, Object> impersonated = kc.realm("windup").users().get(representation.getId()).impersonate();
        System.out.println("Impersonating: " + impersonated);

        accessTokenResponse.setOtherClaims("cookie", otherClaims.get("cookie"));
        return accessTokenResponse;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String varName = req.getParameter("variable");
        AccessTokenResponse accessTokenResponse = getAccessToken();
        String tokenObject = JsonSerialization.writeValueAsPrettyString(accessTokenResponse);
        if (accessTokenResponse.getOtherClaims() != null && accessTokenResponse.getOtherClaims().get("cookie") != null)
            resp.addCookie(new Cookie("KEYCLOAK_IDENTITY", accessTokenResponse.getOtherClaims().get("cookie").toString()));

        resp.getWriter().write("var " + varName + " = " + tokenObject + ";");
    }
}
