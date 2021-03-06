/*
 *  Copyright (c) 2020. Eugen Covaci
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 */

package org.kpax.winfoom.proxy.processor;

import org.apache.http.*;
import org.apache.http.conn.*;
import org.kpax.winfoom.annotation.*;
import org.kpax.winfoom.exception.*;
import org.kpax.winfoom.proxy.*;
import org.kpax.winfoom.util.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

import java.net.*;

/**
 * Process any type of non-CONNECT request for SOCKS/DIRECT proxy.
 */
@ThreadSafe
@Component
class SocksNonConnectClientConnectionProcessor extends NonConnectClientConnectionProcessor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    void handleError(ClientConnection clientConnection,
                     ProxyInfo proxyInfo, Exception e) throws ProxyConnectException {
        if (e instanceof UnknownHostException) {
            clientConnection.writeErrorResponse(HttpStatus.SC_NOT_FOUND, e.getMessage());
        } else if (e instanceof ConnectTimeoutException) {
            if (e.getCause() instanceof SocketTimeoutException) {
                clientConnection.writeErrorResponse(HttpStatus.SC_GATEWAY_TIMEOUT, e.getMessage());
            } else {
                throw new ProxyConnectException(e.getMessage(), e);
            }
        } else if (e instanceof SocketException && HttpUtils.isSOCKSAuthenticationFailed((SocketException) e)) {
            clientConnection.writeErrorResponse(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED);
        } else if (e instanceof HttpHostConnectException) {
            if (e.getCause() instanceof ConnectException) {
                throw new ProxyConnectException(e.getMessage(), e);
            } else {
                clientConnection.writeErrorResponse(HttpStatus.SC_GATEWAY_TIMEOUT, e.getMessage());
            }
        } else {
            // Generic error
            clientConnection.writeErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
