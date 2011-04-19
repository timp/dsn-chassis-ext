/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 *
 * @author Alfresco
 * @author Laurent Meunier <lme@atolcd.com>
 */
package org.wwarn.cms.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import org.springframework.extensions.webscripts.connector.AbstractAuthenticator;
import org.springframework.extensions.webscripts.connector.AlfrescoAuthenticator;
import org.springframework.extensions.webscripts.connector.ConnectorSession;
import org.springframework.extensions.webscripts.connector.Credentials;
import org.springframework.extensions.webscripts.connector.RemoteClient;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.surf.exception.AuthenticationException;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * CAS proxy authenticator.
 */
public class CasAlfrescoAuthenticator extends AbstractAuthenticator {
    public final static String CS_PARAM_ALF_TICKET = "alfTicket";
    private static Log logger = LogFactory.getLog(AlfrescoAuthenticator.class);

    /* (non-Javadoc)
    * @see org.alfresco.connector.AbstractAuthenticator#authenticate(java.lang.String, org.alfresco.connector.Credentials, org.alfresco.connector.ConnectorSession)
    */
    public ConnectorSession authenticate(String endpoint, Credentials credentials, ConnectorSession connectorSession)
            throws AuthenticationException {
        ConnectorSession cs = null;

        if (credentials != null) {    // credentials endpoint = 'alfresco'  cleartextUserName for example colin@example.org
            // Build a new remote client
            RemoteClient remoteClient = new RemoteClient(endpoint);    // endpoint = http://129.67.46.156:8080/alfresco/s

            System.out.println("get credentials");
            // Call the login web script
            String username = (String) credentials.getProperty(Credentials.CREDENTIAL_USERNAME);
            String proxyticket = (String) credentials.getProperty(Credentials.CREDENTIAL_PASSWORD);

            if (logger.isDebugEnabled())
                logger.debug("Authenticating user: " + username);

            // Call the LoginCas WebScript
            //  (located in trunk\_alfresco\config\alfresco\extension\templates\webscripts\org\wwarn\authentication)
            if (proxyticket == null) {
                // The CAS Server can probably not reach Alfresco
                // The following should have happened:
                // 1) Call CAS to login with Share service: http://cloud1.cggh.org/sso/login?service=http://129.67.46.156:8080/share
                // 2) CAS responds by calling back/redirecting with ticket:  http://129.67.46.156:8080/share?ticket=ST-31-1yGp0Dd3jbFIGH9PTcrN-sso

                // later on: Turning ticket into username = http://cloud1.cggh.org/sso/login/serviceValidate?ticket=ST-31-1yGp0Dd3jbFIGH9PTcrN-sso&service=http://129.67.46.156:8080/share
                throw new AuthenticationException("Proxy ticket is null, make sure that the CAS Server can reach the Alfresco server");
            }
            String casLoginUrl = "/api/logincas?u=" + URLEncoder.encode(username) + "&t=" + URLEncoder.encode(proxyticket);
            Response response = remoteClient.call(casLoginUrl);

            // Read back the ticket
            if (response.getStatus().getCode() == 200) {
                String responseText = response.getResponse();

                // Read out the ticket id
                String ticket = null;
                try {
                    ticket = DocumentHelper.parseText(responseText).getRootElement().getTextTrim();
                } catch (DocumentException de) {
                    // The ticket that came back was unparseable or invalid
                    // This will cause the entire handshake to fail
                    throw new AuthenticationException("Unable to retrieve ticket from Alfresco", de);
                }

                if (logger.isDebugEnabled())
                    logger.debug("Parsed ticket: " + ticket);

                // Place the ticket back into the connector session
                if (connectorSession != null) {
                    connectorSession.setParameter(CS_PARAM_ALF_TICKET, ticket);

                    // Signal that this succeeded
                    cs = connectorSession;
                }
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("Authentication failed, received response code: " + response.getStatus().getCode());
            }
        } else {
            cs = connectorSession;      // connector session endpoint = 'alfresco'
        }

        return cs;
    }

    /* (non-Javadoc)
    * @see org.alfresco.connector.AbstractAuthenticator#isAuthenticated(java.lang.String, org.alfresco.connector.ConnectorSession)
    */
    public boolean isAuthenticated(String endpoint, ConnectorSession connectorSession) {
        return (connectorSession.getParameter(CS_PARAM_ALF_TICKET) != null);
    }

}
