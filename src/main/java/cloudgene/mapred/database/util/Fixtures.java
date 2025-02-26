package cloudgene.mapred.database.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;

public class Fixtures {

    private static final Logger log = LoggerFactory.getLogger(Fixtures.class);

    public static String ADMIN_USERNAME = "admin";
    public static String ADMIN_PASSWORD = "admin1978";

    public static String PUBLIC_USERNAME = "Public";
    public static String PUBLIC_PASSWORD = "test";

    public static void insert(Database database) {

        UserDao dao = new UserDao(database);

        // Insert admin user
        User adminUser = dao.findByUsername(ADMIN_USERNAME);
        if (adminUser == null) {
            adminUser = new User();
            adminUser.setUsername(ADMIN_USERNAME);
            ADMIN_PASSWORD = HashUtil.hashPassword(ADMIN_PASSWORD);
            adminUser.setPassword(ADMIN_PASSWORD);
            adminUser.makeAdmin();
            dao.insert(adminUser);
            log.info("User " + ADMIN_USERNAME + " created.");
        } else {
            log.info("User " + ADMIN_USERNAME + " already exists.");

            if (!adminUser.isAdmin()) {
                adminUser.makeAdmin();
                dao.update(adminUser);
                log.info("User " + ADMIN_USERNAME + " has admin rights now.");
            }
        }

        // Insert public user
        User publicUser = dao.findByUsername(PUBLIC_USERNAME);
        if (publicUser == null) {
            publicUser = new User();
            publicUser.setUsername(PUBLIC_USERNAME);
            PUBLIC_PASSWORD = HashUtil.hashPassword(PUBLIC_PASSWORD);
            publicUser.setPassword(PUBLIC_PASSWORD); // Empty password
            publicUser.setRoles(new String[] { "user" });
            dao.insert(publicUser);
            log.info("User " + PUBLIC_USERNAME + " created.");
        } else {
            if (!publicUser.hasRole("user")){
                publicUser.setRoles(new String[] { "user" });
                dao.update(publicUser);
            }
            log.info("User " + PUBLIC_USERNAME + " already exists.");
        }

        // Insert template messages
        TemplateDao htmlSnippetDao = new TemplateDao(database);

        for (Template defaultSnippet : Template.SNIPPETS) {
            Template snippet = htmlSnippetDao.findByKey(defaultSnippet.getKey());
            if (snippet == null) {
                htmlSnippetDao.insert(defaultSnippet);
                log.info("Template " + defaultSnippet.getKey() + " created.");
            } else {
                log.info("Template " + defaultSnippet.getKey() + " already exists.");
            }
        }
    }
}
