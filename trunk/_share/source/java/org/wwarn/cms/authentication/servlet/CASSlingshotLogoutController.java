package org.wwarn.cms.authentication.servlet;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.mvc.LogoutController;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.surf.support.AlfrescoUserFactory;
import org.springframework.extensions.webscripts.connector.AlfrescoAuthenticator;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.web.servlet.ModelAndView;

/**
 * CAS specific override of the Share specific override of the SpringSurf
 * dologout controller.
 * <p>
 * The implementation ensures Alfresco tickets are removed if appropriate and as
 * it can't delegates to the SpringSurf implementation for framework cleanup
 * does that clean up and then sends a logout to the CAS host
 * 
 * @see org.alfresco.web.site.servlet.SlingshotLogoutController
 * 
 * @author Ian Wright
 */
public class CASSlingshotLogoutController extends LogoutController {
	private static Log logger = LogFactory
			.getLog(CASSlingshotLogoutController.class);
	private ConnectorService connectorService;
	private String casHost;
	private String casPath;

	/**
	 * @param connectorService
	 *            the ConnectorService to set
	 */
	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	@Override
	public ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		try {
			HttpSession session = request.getSession(false);
			if (session != null) {
				// retrieve the current user ID from the session
				String userId = (String) session
						.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID);

				if (userId != null) {
					// get the ticket from the Alfresco connector
					Connector connector = connectorService.getConnector(
							AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID, userId,
							session);
					String ticket = connector.getConnectorSession()
							.getParameter(
									AlfrescoAuthenticator.CS_PARAM_ALF_TICKET);

					if (ticket != null) {
						// if we found a ticket, then expire it via REST API -
						// not all auth will have a ticket i.e. SSO
						Response res = connector.call("/api/login/ticket/"
								+ ticket, new ConnectorContext(
								HttpMethod.DELETE));
						if (logger.isDebugEnabled())
							logger.debug("Expired ticket: " + ticket
									+ " user: " + userId + " - status: "
									+ res.getStatus().getCode());
					}
				}
			}
		} finally {
			AuthenticationUtil.logout(request, response);
			String target = request.getContextPath();
			if (casHost != null && casHost.length() > 0) {
				target = casHost;
			} else {
				URL reconstructedURL = new URL(request.getScheme(),
                        request.getServerName(),
                        request.getServerPort(),
                        "");
				target = reconstructedURL.toExternalForm();
			}
			if (casPath != null && casPath.length() > 0) {
				target += '/' + casPath;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Logout to:" + target);
			}
			response.sendRedirect(target);
		}

		return null;
	}

	/**
	 * @param casHostValue
	 *            the casHost to set - defaults to the same as Share
	 */
	public void setCasHost(String casHostValue) {
		casHost = casHostValue;
	}

	/**
	 * @param casPathValue
	 *            the location of the CAS logout servlet
	 */
	public void setCasPath(String casPathValue) {
		casPath = casPathValue;
	}
}