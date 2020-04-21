/*
 * Copyright (c) 2020. Eugen Covaci
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.kpax.winfoom.proxy;

import org.apache.http.*;
import org.apache.http.impl.execchain.TunnelRefusedException;
import org.kpax.winfoom.config.UserConfig;
import org.kpax.winfoom.util.LocalIOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Eugen Covaci {@literal eugen.covaci.q@gmail.com}
 * Created on 4/13/2020
 */
@Component
class HttpConnectRequestHandler implements RequestHandler {

    private final Logger logger = LoggerFactory.getLogger(HttpConnectRequestHandler.class);

    @Autowired
    private UserConfig userConfig;

    @Autowired
    private ProxyContext proxyContext;

    @Autowired
    private CustomProxyClient proxyClient;

    @Override
    public void handleRequest(final HttpRequest request,
                              final SocketWrapper socketWrapper)
            throws IOException, HttpException {
        logger.debug("Handle connect request");
        RequestLine requestLine = request.getRequestLine();
        HttpHost proxy = new HttpHost(userConfig.getProxyHost(), userConfig.getProxyPort());
        HttpHost target = HttpHost.create(requestLine.getUri());

        try (Tunnel tunnel = proxyClient.tunnel(proxy, target, requestLine.getProtocolVersion())) {
            try {
                handleResponse(tunnel, socketWrapper);
            } catch (Exception e) {
                logger.debug("Error on handling CONNECT response", e);
            }
        } catch (TunnelRefusedException tre) {
            logger.debug("The tunnel request was rejected by the proxy host", tre);
            try {
                socketWrapper.writeHttpResponse(tre.getResponse());
            } catch (Exception e) {
                logger.debug("Error on writing response", e);
            }
        }

    }

    /**
     * Handles the tunnel's response.<br>
     *
     * @param tunnel The tunnel's instance
     * @throws IOException
     */
    private void handleResponse(final Tunnel tunnel,
                                SocketWrapper localSocketChannel) throws IOException {
        logger.debug("Write status line");
        localSocketChannel.write(tunnel.getStatusLine());

        logger.debug("Write headers");
        for (Header header : tunnel.getResponse().getAllHeaders()) {
            localSocketChannel.write(header);
        }
        localSocketChannel.writeln();

        // The proxy facade mediates the full duplex communication
        // between the client and the remote proxy
        LocalIOUtils.duplex(proxyContext.executorService(),
                tunnel.getSocket().getInputStream(),
                tunnel.getSocket().getOutputStream(),
                localSocketChannel.getInputStream(),
                localSocketChannel.getOutputStream());
    }

}
