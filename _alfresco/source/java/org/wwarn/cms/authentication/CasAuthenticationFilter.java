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
 */
package org.wwarn.cms.authentication;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
import javax.transaction.UserTransaction;

import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AbstractAuthenticationFilter;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.LanguagesConfigElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Sample authentication for CAS.
 *
 * @author Andy Hind
 * @author Laurent Meunier <l.meunier@atolcd.com>
 */
public class CasAuthenticationFilter extends AbstractAuthenticationFilter implements Filter {
    private static final String LOCALE = "locale";
    public static final String MESSAGE_BUNDLE = "alfresco.messages.webclient";
    private static Log logger = LogFactory.getLog(CasAuthenticationFilter.class);
    private ServletContext context;
    private String loginPage;
    private AuthenticationComponent authComponent;
    private AuthenticationService authService;
    private TransactionService transactionService;
    private PersonService personService;
    private NodeService nodeService;
    private List<String> m_languages;

    public CasAuthenticationFilter() {
        super();
    }

    public void init(FilterConfig config) throws ServletException {
        this.context = config.getServletContext();
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();

        authComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authService = (AuthenticationService) ctx.getBean("authenticationService");
        personService = (PersonService) ctx.getBean("personService");

        // Get a list of the available locales
        ConfigService configServiceService = (ConfigService) ctx.getBean("webClientConfigService");
        LanguagesConfigElement configElement = (LanguagesConfigElement) configServiceService.getConfig("Languages").
                getConfigElement(LanguagesConfigElement.CONFIG_ELEMENT_ID);

        m_languages = configElement.getLanguages();
    }

    public void destroy() {
        // Nothing to do
    }

    /**
     * Run the filter
     *
     * @param sreq  ServletRequest
     * @param sresp ServletResponse
     * @param chain FilterChain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain)
            throws IOException, ServletException {

        // Get the HTTP request/response/session
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse resp = (HttpServletResponse) sresp;
        HttpSession httpSess = req.getSession(true);

        // Get CAS information
        Assertion assertion = (Assertion) httpSess.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
        String username = assertion.getPrincipal().getName();

        // See if there is a user in the session and test if it matches
        User user = (User) httpSess.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
        if (user != null) {
            try {
                if (user.getUserName().equals(username)) {
                    authComponent.setCurrentUser(user.getUserName());
                    I18NUtil.setLocale(Application.getLanguage(httpSess));
                    chain.doFilter(sreq, sresp);
                    return;
                } else {
                    // No match
                    setAuthenticatedUser(req, httpSess, username);
                }
            } catch (AuthenticationException ex) {
                if (logger.isErrorEnabled())
                    logger.error("Failed to validate user " + user.getUserName(), ex);
            }
        }

        setAuthenticatedUser(req, httpSess, username);

        // Redirect the login page as it is never seen as we always login by name
        if (req.getRequestURI().endsWith(getLoginPage()) == true) {
            if (logger.isDebugEnabled())
                logger.debug("Login page requested, chaining ...");

            resp.sendRedirect(req.getContextPath() + "/faces/jsp/browse/browse.jsp");
            return;
        } else {
            chain.doFilter(sreq, sresp);
            return;
        }
    }

    /**
     * Set the authenticated user.
     * <p/>
     * It does not check that the user exists at the moment.
     *
     * @param req
     * @param httpSess
     * @param userName
     */
    private void setAuthenticatedUser(HttpServletRequest req, HttpSession httpSess, String userName) {
        // Set the authentication
        authComponent.setCurrentUser(userName);

        // Set up the user information
        UserTransaction tx = transactionService.getUserTransaction();
        NodeRef homeSpaceRef = null;
        User user;
        try {
            tx.begin();
            user = new User(userName, authService.getCurrentTicket(), personService.getPerson(userName));
            homeSpaceRef = (NodeRef) nodeService.getProperty(
                    personService.getPerson(userName), ContentModel.PROP_HOMEFOLDER);
            if (homeSpaceRef == null) {
                logger.warn("Home Folder is null for user '" + userName + "', using company_home.");
                homeSpaceRef = (NodeRef) nodeService.getRootNode(Repository.getStoreRef());
            }
            user.setHomeSpaceId(homeSpaceRef.getId());
            tx.commit();
        } catch (Throwable ex) {
            logger.error(ex);

            try {
                tx.rollback();
            } catch (Exception ex2) {
                logger.error("Failed to rollback transaction", ex2);
            }

            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new RuntimeException("Failed to set authenticated user", ex);
            }
        }

        // Store the user
        httpSess.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
        httpSess.setAttribute(LoginBean.LOGIN_EXTERNAL_AUTH, Boolean.TRUE);

        // Set the current locale from the Accept-Lanaguage header if available
        Locale userLocale = parseAcceptLanguageHeader(req, m_languages);

        if (userLocale != null) {
            httpSess.setAttribute(LOCALE, userLocale);
            httpSess.removeAttribute(MESSAGE_BUNDLE);
        }

        // Set the locale using the session
        I18NUtil.setLocale(Application.getLanguage(httpSess));

    }

    /**
     * Return the login page address
     *
     * @return String
     */
    private String getLoginPage() {
        if (loginPage == null) {
            loginPage = Application.getLoginPage(context);
        }

        return loginPage;
    }
}
