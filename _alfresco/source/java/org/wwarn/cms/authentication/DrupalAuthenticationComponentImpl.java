package org.wwarn.cms.authentication;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.security.MessageDigest;

/**
 * Authenticates a user by Drupal.
 * <p/>
 * The authentication is done against the database table USERS and the
 * column NAME (username) and column PASS (MD5 password)
 * <p/>
 * Tested with Drupal 6
 *
 * @author martin.bergljung@ixxus.co.uk
 */
public class DrupalAuthenticationComponentImpl extends AbstractAuthenticationComponent implements ActivateableBean {
    public static final String GET_USER_PWD_SQL = "SELECT PASS FROM USERS WHERE NAME=?";

    private final Log logger = LogFactory.getLog(getClass());

    /**
     * Spring JDBC template used to query or update a JDBC data source
     */
    private JdbcTemplate m_jdbcTemplate;

    /**
     * Is this bean active? I.e. should this part of the subsystem be used?
     */
    private boolean m_active = true;

    public DrupalAuthenticationComponentImpl() {
        super();
    }

    /**
     * Controls whether this bean is active. I.e. should this part of the subsystem be used?
     *
     * @param active <code>true</code> if this bean is active
     */
    public void setActive(boolean active) {
        m_active = active;
    }

    /**
     * Dependeny Injects the data source to be used for querying Drupal database
     *
     * @param dataSource the data source to use
     */
    public void setDataSource(DataSource dataSource) {
        m_jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /*
    * (non-Javadoc)
    * @see org.alfresco.repo.management.subsystems.ActivateableBean#isActive()
    */
    public boolean isActive() {
        return m_active;
    }

    /**
     * Authenticate against the Drupal database
     *
     * @param userName the username to authenticate
     * @param password the password to authenticate (passed in as plain text)
     * @throws AuthenticationException if authentication failed
     */
    @Override
    protected void authenticateImpl(String userName, char[] password) throws AuthenticationException {
        String userPwd = new String(password);

        // Generate an MD5 hash for the password as that is what we get back from Drupal
        // Get the value as hex
        String userPwdMd5 = DigestUtils.md5Hex(userPwd);

        if (logger.isDebugEnabled()) {
            logger.debug("About to authenticate user: " + userName + " with MD5 password: " + userPwdMd5);
        }

        try {
            String drupalPwdMd5 = m_jdbcTemplate.queryForObject(GET_USER_PWD_SQL, new Object[]{userName}, String.class);

            if (logger.isDebugEnabled()) {
                logger.debug("Got MD5 password from Drupal database: " + drupalPwdMd5);
            }

            if (StringUtils.isNotBlank(drupalPwdMd5)) {
                if (MessageDigest.isEqual(userPwdMd5.getBytes(), drupalPwdMd5.getBytes())) {
                    // Authentication has been successful.
                    // Set the current user, they are now authenticated.
                    setCurrentUser(userName);
                } else {
                    throw new AuthenticationException("Access denied for user: " + userName +
                            ", incorrect password provided.");
                }
            } else {
                throw new AuthenticationException(
                        "Password in Drupal database is blank, empty, or null for user: " + userName);
            }
        } catch (DataAccessException dae) {
            throw new AuthenticationException(
                    "Error getting password from Drupal database for user: " + userName +
                            ", user may not exist in the Drupal database", dae);
        }
    }

    @Override
    protected boolean implementationAllowsGuestLogin() {
        return true;
    }
}
