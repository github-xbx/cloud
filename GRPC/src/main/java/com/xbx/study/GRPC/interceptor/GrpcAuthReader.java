package com.xbx.study.GRPC.interceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.devh.boot.grpc.common.security.SecurityConstants.AUTHORIZATION_HEADER;
import static net.devh.boot.grpc.common.security.SecurityConstants.BEARER_AUTH_PREFIX;

public class GrpcAuthReader {

    private static final Logger logger = LoggerFactory.getLogger(GrpcAuthReader.class);

    private static final String PREFIX = BEARER_AUTH_PREFIX.toLowerCase();
    private static final int PREFIX_LENGTH = PREFIX.length();

    private static final String TOKEN = "123456789";

    public Boolean readAuthentication(ServerCall<?, ?> call, Metadata headers){
        final String header = headers.get(AUTHORIZATION_HEADER);
        if (header == null || !header.toLowerCase().startsWith(PREFIX)) {
            logger.debug("No basic auth header found");
            return null;
        }
        String token = header.substring(PREFIX_LENGTH);
        if (TOKEN.equals(token)){
            return true;
        }
        return false;
    }


}
