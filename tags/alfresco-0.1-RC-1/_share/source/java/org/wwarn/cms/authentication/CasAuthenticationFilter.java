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
 * http://www.alfresco.com/legal/licensing
 *
 * @author Alfresco
 * @author Laurent Meunier <lme@atolcd.com>
 * @author Aksel Bruun <aksel.bruun@webstep.no>
 */
package org.wwarn.cms.authentication;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;

import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.RequestContextUtil;
import org.springframework.extensions.surf.WebFrameworkServiceRegistry;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.exception.RequestContextException;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Share - CAS authentication
 */
public class CasAuthenticationFilter implements Filter {
    //private final static String ALFRESCO_WEBAPP_URL = "http://46.137.92.130:8080/alfresco";
    private final static String ALFRESCO_WEBAPP_URL = "http://localhost:8080/alfresco";

    private ServletContext servletContext;

    public CasAuthenticationFilter() {
        super();
    }

    public void destroy() {
    }

    public void init(FilterConfig args) throws ServletException {
        // Get reference to our ServletContext
        this.servletContext = args.getServletContext();
    }

    /**
     * Run the filter
     *
     * @param sreq  ServletRequest
     * @param sresp ServletResponse
     * @param chain FilterChain
     * @throws IOException
     * @throws ServletException
     * @throws
     */
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain)
            throws IOException, ServletException {

        // Get the HTTP request/response/session
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse resp = (HttpServletResponse) sresp;
        HttpSession httpSess = req.getSession(true);

        // Initialize a new request context
        RequestContext context = null;
        try {
            // Perform a "silent" init - i.e. no user creation or remote connections
            context = RequestContextUtil.initRequestContext(getApplicationContext(), (HttpServletRequest) sreq, true);
        } catch (RequestContextException ex) {
            throw new ServletException(ex);
        }

        // Get CAS information
        Assertion assertion = (Assertion) httpSess.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
        String username = assertion.getPrincipal().getName();

        if (AuthenticationUtil.isAuthenticated(req)) {
            // Already authenticated
            chain.doFilter(sreq, sresp);
            return;
        }

        String proxyticket = assertion.getPrincipal().getProxyTicketFor(ALFRESCO_WEBAPP_URL);

        try {

            WebFrameworkServiceRegistry serviceRegistry = context.getServiceRegistry();
            UserFactory userFactory = serviceRegistry.getUserFactory();

            // Pass the proxy CAS ticket to alfresco to authenticate (and get an alfresco ticket)
            boolean authenticated = userFactory.authenticate(req, username, proxyticket);
            if (authenticated) {
                // This will fully reset all connector sessions
                AuthenticationUtil.login(req, resp, username); // Will call CasAlfrescoAuthenticator.authenticate
            }
        } catch (Throwable err) {
            throw new ServletException(err);
        }

        chain.doFilter(sreq, sresp);
    }

    /**
     * Retrieves the root application context
     *
     * @return application context
     */
    private ApplicationContext getApplicationContext() {
        return WebApplicationContextUtils.getWebApplicationContext(servletContext);
    }
}
