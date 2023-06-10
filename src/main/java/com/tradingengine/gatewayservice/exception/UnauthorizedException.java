package com.tradingengine.gatewayservice.exception;

public class UnauthorizedException extends Exception{
    public UnauthorizedException() {
        super("Unauthorized Access!!");
    }
}
