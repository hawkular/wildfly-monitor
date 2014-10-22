package org.wildfly.metrics.scheduler.polling;

import org.jboss.as.controller.client.ModelControllerClient;
import org.wildfly.metrics.scheduler.ModelControllerClientFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Heiko Braun
 * @since 17/10/14
 */
public class ClientFactoryImpl implements ModelControllerClientFactory  {

    private String host;
    private int port;
    private String username;
    private String password;

    public ClientFactoryImpl(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    public ModelControllerClient createClient() {

        final CallbackHandler callbackHandler = new CallbackHandler() {

            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback current : callbacks) {
                    if (current instanceof NameCallback) {
                        NameCallback ncb = (NameCallback) current;
                        ncb.setName(username);
                    } else if (current instanceof PasswordCallback) {
                        PasswordCallback pcb = (PasswordCallback) current;
                        pcb.setPassword(password.toCharArray());
                    } else if (current instanceof RealmCallback) {
                        RealmCallback rcb = (RealmCallback) current;
                        rcb.setText(rcb.getDefaultText());
                    } else {
                        throw new UnsupportedCallbackException(current);
                    }
                }
            }
        };

        try {
            return ModelControllerClient.Factory.create(InetAddress.getByName(host), port, callbackHandler);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed ot create controller client", e);
        }
    }
}
