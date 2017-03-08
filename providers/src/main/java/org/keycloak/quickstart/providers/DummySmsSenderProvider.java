package org.keycloak.quickstart.providers;

/**
 * Created by st on 08/03/17.
 */
public class DummySmsSenderProvider {
    public void send(String number, String message) {
        System.out.println("SMS to " + number + ": " + message);
    }
}
