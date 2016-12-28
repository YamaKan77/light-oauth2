package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth2ClientClientIdDeleteHandler implements HttpHandler {
    static final String CLIENT_NOT_FOUND = "ERR12014";

    static Logger logger = LoggerFactory.getLogger(Oauth2ClientClientIdDeleteHandler.class);
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String clientId = exchange.getQueryParameters().get("clientId").getFirst();

        IMap<String, Object> clients = CacheStartupHookProvider.hz.getMap("clients");
        if(clients.get(clientId) == null) {
            Status status = new Status(CLIENT_NOT_FOUND, clientId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        } else {
            clients.delete(clientId);
        }
    }
}
