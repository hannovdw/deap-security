package za.co.dariel.deap.endpointsecurity.services;


import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import za.co.dariel.deap.endpointsecurity.entities.EmployeeEntity;
import javax.ws.rs.core.Response;
import java.util.Arrays;

@Component
public class KeycloakService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakService.class);
    private static final String ACTION = "Username==";

    @Value("${keycloak.credentials.secret}")
    private String secretKey;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.auth-server-url}")
    private String authUrl;

    @Value("${keycloak.realm}")
    private String realm;

    private String admin = "admin";


    public int createUserInKeyCloak(EmployeeEntity employeeEntity) {

        int statusId = 0;
        try {

            UsersResource userResource = getKeycloakUserResource();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(employeeEntity.getUsername());
            user.setEmail(employeeEntity.getEmail());
            user.setFirstName(employeeEntity.getFirstName());
            user.setLastName(employeeEntity.getLastName());
            user.setEnabled(true);

            // Create user
            Response result = userResource.create(user);
            System.out.println("Keycloak create user response code>>>>" + result.getStatus());

            statusId = result.getStatus();

            if (statusId == 201) {

                String userId = result.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

                System.out.println("User created with userId:" + userId);

                // Define password credential
                CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setTemporary(false);
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setValue(employeeEntity.getPassword());

                // Set password credential
                userResource.get(userId).resetPassword(passwordCred);


                // set role
                RealmResource realmResource = getRealmResource();
                RoleRepresentation savedRoleRepresentation = realmResource.roles().get("app-user").toRepresentation();
                realmResource.users().get(userId).roles().realmLevel().add(Arrays.asList(savedRoleRepresentation));

                logger.info(ACTION + employeeEntity.getUsername() + " created in keycloak successfully");

            }

            else if (statusId == 409) {
                logger.error(ACTION + employeeEntity.getUsername() + " already present in keycloak");

            } else {
                logger.error(ACTION + employeeEntity.getUsername() + " could not be created in keycloak");

            }

        } catch (Exception e) {
            logger.info("context", e);

        }

        return statusId;

    }

    // Reset passowrd
    public void resetPassword(String newPassword, String userId) {

        UsersResource userResource = getKeycloakUserResource();

        // Define password credential
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(newPassword.trim());

        // Set password credential
        userResource.get(userId).resetPassword(passwordCred);

    }

    private UsersResource getKeycloakUserResource() {

        Keycloak kc = KeycloakBuilder.builder().serverUrl(authUrl).realm("master").username(admin).password(admin)
                .clientId("admin-cli").resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();

        RealmResource realmResource = kc.realm(realm);

        return realmResource.users();
    }

    private RealmResource getRealmResource() {

        Keycloak kc = KeycloakBuilder.builder().serverUrl(authUrl).realm("master").username(admin).password(admin)
                .clientId("admin-cli").resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();


        return kc.realm(realm);

    }


}