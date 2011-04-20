/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 */
package org.wwarn.cms.authentication;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.extensions.webscripts.*;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;


/**
 * Login via a CAS proxy ticket.
 * <p/>
 * Java implementation of WebScript LoginCas located here:
 * trunk\_alfresco\config\alfresco\extension\templates\webscripts\org\wwarn\authentication
 */
public class LoginCas extends DeclarativeWebScript {
    private final static String CAS_WEBAPP_URL = "http://cloud1.cggh.org/sso";
    //private final static String ALFRESCO_WEBAPP_URL = "http://46.137.92.130:8080/alfresco";
    private final static String ALFRESCO_WEBAPP_URL = "http://IXDV1210.ixxus.co.uk:8080/alfresco";

    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
        this.authenticationComponent = authenticationComponent;
    }

    /* (non-Javadoc)
    * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
    */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status) {
        // Extract username
        String username = req.getParameter("u");
        if (username == null || username.length() == 0) {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Username not specified");
        }
        // Extract CAS ticket
        String ticket = req.getParameter("t");
        if (ticket == null) {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Ticket not specified");
        }

        try {
            // Add ticket to model for javascript and template access
            Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);

            // Validate our proxy CAS ticket
            Cas20ProxyTicketValidator tv = new Cas20ProxyTicketValidator(CAS_WEBAPP_URL);
            tv.setAcceptAnyProxy(true);
            String legacyServerServiceUrl = ALFRESCO_WEBAPP_URL;
            Assertion assertion = tv.validate(ticket, legacyServerServiceUrl);
            String cas_username = assertion.getPrincipal().getName();

            // Compare usernames
            if (!username.equals(assertion.getPrincipal().getName())) {
                throw new TicketValidationException("usernames does not match: " + username + "/" + cas_username);
            }

            // Authenticate our user
            authenticationComponent.setCurrentUser(username);

            // Create a new alfresco ticket
            String alfticket = authenticationService.getCurrentTicket();
            model.put("ticket", alfticket);

            return model;
        } catch (AuthenticationException e) {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Login failed");
        } catch (TicketValidationException ex) {
            ex.printStackTrace();
        } finally {
            authenticationService.clearCurrentSecurityContext();
        }

        return null;
    }
}
