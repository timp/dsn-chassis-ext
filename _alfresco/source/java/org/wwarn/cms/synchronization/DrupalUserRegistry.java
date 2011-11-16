package org.wwarn.cms.synchronization;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.sync.NodeDescription;
import org.alfresco.repo.security.sync.UserRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.util.*;

/**
 * A {@link org.alfresco.repo.security.sync.UserRegistry} implementation with the ability to
 * query Alfresco-like descriptions of users and groups from a Drupal database,
 * optionally restricted to those modified since a certain time.
 *
 * @author martin.bergljung@ixxus.co.uk
 */
public class DrupalUserRegistry implements UserRegistry, InitializingBean, ActivateableBean {
    private final Log logger = LogFactory.getLog(getClass());

    public static final String USERS_USERNAME_UNIQUE_KEY = "name";
    public static final String GET_USERS_SQL = "select " + USERS_USERNAME_UNIQUE_KEY + ", mail from users";
    public static final String GET_USERS_NAME_SQL = "select " + USERS_USERNAME_UNIQUE_KEY + " from users";

    /**
     * Alfresco services
     */
    private NamespaceService m_namespaceService;

    /**
     * Is this bean active? I.e. should this part of the subsystem be used?
     */
    private boolean m_active = true;

    /**
     * Spring JDBC template used to query or update a JDBC data source
     */
    private JdbcTemplate m_jdbcTemplate;

    /**
     * The person attribute mapping.
     * Repo QName -> USERS column
     * e.g. cm:userName -> name
     */
    private Map<String, String> m_personRepoPropName2DrupalColMap = Collections.emptyMap();

    /**
     * The person property defaults.
     * Such as home folder provider
     */
    private Map<String, String> m_personRepoPropDefaults = Collections.emptyMap();

    /**
     * A set of repo property QNames that have been mapped to drupal USERS table column.
     */
    private Set<QName> m_personMappedProperties = new HashSet<QName>();

    /**
     * Instantiates a new Drupal user registry.
     */
    public DrupalUserRegistry() {
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

    /**
     * Sets the person property mapping.
     *
     * @param personRepoPropName2DrupalColMap
     *         the person repo property 2 drupal column map
     */
    public void setRepoPropName2DrupalColMap(Map<String, String> personRepoPropName2DrupalColMap) {
        m_personRepoPropName2DrupalColMap = personRepoPropName2DrupalColMap;
    }

    /**
     * Sets the person property defaults.
     *
     * @param personRepoPropDefaults the person property defaults
     */
    public void setPersonPropertyDefaults(Map<String, String> personRepoPropDefaults) {
        m_personRepoPropDefaults = personRepoPropDefaults;
    }

    /**
     * Sets the namespace service.
     *
     * @param namespaceService the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService) {
        m_namespaceService = namespaceService;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        for (Map.Entry<String, String> personMappedProperty : m_personRepoPropName2DrupalColMap.entrySet()) {
            // Get the key, for example 'cm:userName' and turn into QName object
            m_personMappedProperties.add(QName.createQName(personMappedProperty.getKey(), m_namespaceService));
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see org.alfresco.repo.management.subsystems.ActivateableBean#isActive()
     */
    public boolean isActive() {
        return m_active;
    }

    /**
     * Gets the set of property names that are auto-mapped by this user registry.
     * These should remain read-only for this registry's users in the UI.
     * <p/>
     * Basically get a Set of repository QName objects representing those properties that
     * have been mapped to columns in the Drupal table USERS.
     * <p/>
     * (non-Javadoc)
     *
     * @see org.alfresco.repo.security.sync.UserRegistry#getPersonMappedProperties()
     */
    public Set<QName> getPersonMappedProperties() {
        if (logger.isDebugEnabled()) {

            String allProps = "";
            for (QName prop : m_personMappedProperties) {
                allProps = prop + ", ";
            }

            logger.debug("Getting the property names that are mapped by this user registry: " + allProps);
        }

        return m_personMappedProperties;
    }

    /**
     * Gets descriptions of all the persons (users) in the user registry
     * or all those changed since a certain date.
     * <p/>
     * (non-Javadoc)
     *
     * @see org.alfresco.repo.security.sync.UserRegistry#getPersons(java.util.Date)
     */
    public Collection<NodeDescription> getPersons(Date modifiedSince) {
        final List<NodeDescription> persons = new LinkedList<NodeDescription>();

        if (logger.isDebugEnabled()) {
            logger.debug("About to import/sync user data from Drupal");
        }

        // Get the name and email for all users
        SqlRowSet drupalUserRows = m_jdbcTemplate.queryForRowSet(GET_USERS_SQL);

        // Loop through the repo properties we have mapped to drupal USERS columns
        while (drupalUserRows.next()) {
            if (StringUtils.isBlank(drupalUserRows.getString(USERS_USERNAME_UNIQUE_KEY))) {
                continue;
            }

            // Setup the mapped repo properties with values from drupal USERS columns
            NodeDescription nd = new NodeDescription(drupalUserRows.getString(USERS_USERNAME_UNIQUE_KEY));
            PropertyMap properties = nd.getProperties();
            for (String repoPropertyName : m_personRepoPropName2DrupalColMap.keySet()) {
                QName repoPropertyQName = QName.createQName(repoPropertyName, m_namespaceService);
                String drupalColumnName = m_personRepoPropName2DrupalColMap.get(repoPropertyName);

                if (drupalColumnName != null) {
                    // It's going to be either name or mail, so both are strings
                    String columnValue = drupalUserRows.getString(drupalColumnName.toUpperCase());
                    if (StringUtils.isNotBlank(columnValue)) {
                        properties.put(repoPropertyQName, columnValue);
                    } else {
                        String defaultValue = m_personRepoPropDefaults.get(repoPropertyName);
                        if (defaultValue != null) {
                            properties.put(repoPropertyQName, defaultValue);
                        }
                    }
                } else {
                    String defaultValue = m_personRepoPropDefaults.get(repoPropertyName);
                    if (defaultValue != null) {
                        properties.put(repoPropertyQName, defaultValue);
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Found " + persons.size() + " users to import/sync from Drupal");
            }


            persons.add(nd);
        }

        return persons;
    }

    /**
     * Gets the names of all persons in the registry. Used to detect local persons to be deleted. Note that the
     * treatment of these names will depend on Alfresco's username case-sensitivity setting.
     * <p/>
     * (non-Javadoc)
     *
     * @see org.alfresco.repo.security.sync.UserRegistry#getPersonNames()
     */
    public Collection<String> getPersonNames() {

        // Get the name of all users in the USERS table in drupal
        List<String> personNames = m_jdbcTemplate.queryForList(GET_USERS_NAME_SQL, String.class);

        if (logger.isDebugEnabled()) {
            logger.debug("Getting user names " + personNames.size() +
                    " from Drupal to detect users that can be deleted");
        }

        return personNames;
    }

    /**
     * Gets the names of all groups in the registry. Used to detect local groups to be deleted.
     * <p/>
     * (non-Javadoc)
     *
     * @see org.alfresco.repo.security.sync.UserRegistry#getGroupNames()
     */
    public Collection<String> getGroupNames() {
        logger.warn("Group sync is not supported!");

        final List<String> groupNames = new LinkedList<String>();
        return groupNames;
    }

    /**
     * Gets descriptions of all the groups in the user registry or all those changed since a certain date.
     * <p/>
     * (non-Javadoc)
     *
     * @see org.alfresco.repo.security.sync.UserRegistry#getGroups(java.util.Date)
     */
    public Collection<NodeDescription> getGroups(Date modifiedSince) {
        logger.warn("Group sync is not supported!");

        final Map<String, NodeDescription> lookup = new TreeMap<String, NodeDescription>();
        return lookup.values();
    }
}
