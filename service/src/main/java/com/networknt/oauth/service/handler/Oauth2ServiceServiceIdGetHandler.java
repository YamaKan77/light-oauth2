package com.networknt.oauth.service.handler;

import com.networknt.config.Config;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class Oauth2ServiceServiceIdGetHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ServiceServiceIdGetHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> result = new HashMap<>();

        String serviceId = exchange.getQueryParameters().get("serviceId").getFirst();
        String sql = "SELECT * FROM services WHERE service_id = ?";

        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, serviceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result.put("serviceId", serviceId);
                    result.put("serviceType", rs.getString("service_type"));
                    result.put("serviceName", rs.getString("service_name"));
                    result.put("serviceDesc", rs.getString("service_desc"));
                    result.put("scope", rs.getString("scope"));
                    exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
                    exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
                } else {
                    // TODO not found.
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw e;
        }
    }
}