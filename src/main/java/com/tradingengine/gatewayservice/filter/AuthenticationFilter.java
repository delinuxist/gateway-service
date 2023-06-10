package com.tradingengine.gatewayservice.filter;


import com.tradingengine.gatewayservice.config.JwtConfig;
import com.tradingengine.gatewayservice.exception.UnauthorizedException;
import com.tradingengine.gatewayservice.utils.JwtService;
import com.tradingengine.gatewayservice.validator.RouteValidator;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;


@RefreshScope
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator routeValidator;
    private JwtConfig jwtConfig;

    @Autowired
    private JwtService jwtService;
    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if(routeValidator.isSecured.test(exchange.getRequest())){
                // header contains token or not
                if(!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                    throw new RuntimeException("Unauthorized Access!!");
                }

                String authHeader = Objects.requireNonNull(
                        exchange.getRequest()
                                .getHeaders()
                                .get(HttpHeaders.AUTHORIZATION))
                        .get(0);

                if (authHeader !=null && authHeader.startsWith("Bearer ")){
                    authHeader = authHeader.substring(7);
                }

                try {
                    var claims = jwtService.validateToken(authHeader);
                    String userId = claims.getId();
                    if(userId == null) {
                        throw new UnauthorizedException();
                    }
                    exchange.getRequest().mutate().header("userId",userId);
                } catch (UnauthorizedException e){
                    throw new RuntimeException(e);
                }
            }
            return chain.filter(exchange);
        } );
    }

    public static class Config {

    }
}
