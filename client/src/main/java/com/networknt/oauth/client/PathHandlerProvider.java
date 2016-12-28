package com.networknt.oauth.client;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.networknt.oauth.client.handler.*;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

import java.util.Map;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.DELETE, "/oauth2/client/{clientId}", new Oauth2ClientClientIdDeleteHandler())
            .add(Methods.GET, "/oauth2/client/{clientId}", new Oauth2ClientClientIdGetHandler())
            .add(Methods.GET, "/oauth2/client", new Oauth2ClientGetHandler())
            .add(Methods.POST, "/oauth2/client", new Oauth2ClientPostHandler())
            .add(Methods.PUT, "/oauth2/client", new Oauth2ClientPutHandler())
        ;
        return handler;
    }
}

