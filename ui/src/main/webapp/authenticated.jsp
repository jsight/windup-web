<%@ page import="org.keycloak.KeycloakPrincipal" %>
<%
    HttpSession requestSession = request.getSession(true);
%>
window.location.reload();

Name: <%= ((KeycloakPrincipal)request.getUserPrincipal()).getKeycloakSecurityContext().getToken().getName() %>