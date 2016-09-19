$.ajax({
    url: "/windup-web/authenticated.jsp",
    headers: {
        'Authorization': 'Bearer ' + autologinBearerToken.access_token
    }
}).done(function(data) {
    console.log("Success");
}).fail(function() {
    console.log("Error");
});

var keycloak = new Keycloak('keycloak.json');
keycloak.init({
    onLoad: 'check-sso',
    checkLoginIframe: false,
    token: autologinBearerToken.access_token,
    refreshToken: autologinBearerToken.refresh_token,
    idToken: autologinBearerToken.id_token
}).success(function(authenticated) {
    console.log("Force login success: " + authenticated);
    console.log("Debug: " + keycloak.authenticated);
    console.log("Debug: " + keycloak.token);
    //if (authenticated)
        //appendAuthenticatedScript();
});