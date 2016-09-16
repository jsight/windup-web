package org.jboss.windup.web.ui;

import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Overridden Freemarker Servlet that includes the following Map in the template model:
 *  - keycloak
 *      - publicKey
 *      - serverUrl
 *
 *  This will also automatically append the ".ftl" extension during template path resolution for files
 *  that end with ".html" or ".json".
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FreemarkerServlet extends freemarker.ext.servlet.FreemarkerServlet
{
    public static final String USER_PRINCIPAL = "userPrincipal";
    private static Logger LOG = Logger.getLogger(FreemarkerServlet.class.getName());

    public static final String KEYCLOAK = "keycloak";
    public static final String PUBLIC_KEY = "publicKey";
    public static final String SERVER_URL = "serverUrl";

    @Override
    protected TemplateModel createModel(ObjectWrapper objectWrapper, ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) throws TemplateModelException {
        TemplateModel templateModel = super.createModel(objectWrapper, servletContext, request, response);
        if (templateModel instanceof SimpleHash)
        {
            SimpleHash hashModel = (SimpleHash) templateModel;

            if (request.getUserPrincipal() != null) {
                hashModel.put(USER_PRINCIPAL, request.getUserPrincipal().getName());
            } else {
                hashModel.put(USER_PRINCIPAL, null);
            }

            // The superclass seems to try to put these into the hash, but the code doesn't seem to work.
            // Thus, it is repeated here.
            hashModel.put("request", request);
            hashModel.put("response", response);

            Map<String, String> keycloakProperties = new HashMap<>();
            keycloakProperties.put(PUBLIC_KEY, System.getProperty("keycloak.realm.public.key"));
            keycloakProperties.put(SERVER_URL, System.getProperty("keycloak.server.url"));

            hashModel.put(KEYCLOAK, keycloakProperties);
        }
        return templateModel;
    }

    @Override
    protected String requestUrlToTemplatePath(HttpServletRequest request) throws ServletException
    {
        String superPath = super.requestUrlToTemplatePath(request);
        LOG.info("Resolving freemarker path from: " + superPath);

        if (superPath.endsWith(".html") || superPath.endsWith(".json"))
        {
            superPath += ".ftl";
        }
        LOG.info("Resolved freemarker path to: " + superPath);
        return superPath;
    }
}