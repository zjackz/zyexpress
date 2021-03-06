package net.zyexpress.site;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.UnauthorizedHandler;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.zyexpress.site.auth.AuthPrincipal;
import net.zyexpress.site.auth.TokenBasedAuthenticator;
import net.zyexpress.site.auth.TokenBasedAuthorizer;
import net.zyexpress.site.dao.*;
import net.zyexpress.site.resources.*;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ZYExpressApplication extends Application<ZYExpressConfiguration> {
    public static void main(String[] args) throws Exception {
        new ZYExpressApplication().run(args);
    }

    @Override
    public String getName() {
        return "ZYExpressApplication";
    }

    @Override
    public void initialize(Bootstrap<ZYExpressConfiguration> bootstrap) {
        bootstrap.addBundle(new MultiPartBundle());
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    }

    @Override
    public void run(ZYExpressConfiguration configuration, Environment environment) throws Exception {

        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "hsqldb");

        addUserResource(environment, jdbi);

        addUserIdCardResource(configuration, environment, jdbi);

        addAdminResource(configuration, environment, jdbi);

        addPackageResource(configuration, environment, jdbi);

        addAuthentication(configuration, environment);

        addAddressResource(configuration, environment, jdbi);

        addIdCardManageResource(configuration, environment, jdbi);
    }

    private void addAuthentication(ZYExpressConfiguration configuration, Environment environment) {
        TokenBasedAuthenticator authenticator = new TokenBasedAuthenticator();

        TokenBasedAuthorizer authorizer = new TokenBasedAuthorizer();

        // remove WWW-Authenticate header from 401 response so browser won't popup a dialog
        UnauthorizedHandler unauthorizedHandler = (prefix, realm) ->
                Response.status(Response.Status.UNAUTHORIZED).type(MediaType.TEXT_PLAIN_TYPE).build();

        AuthFilter authFilter = new BasicCredentialAuthFilter.Builder<AuthPrincipal>().setAuthenticator(authenticator)
                .setAuthorizer(authorizer).setUnauthorizedHandler(unauthorizedHandler).buildAuthFilter();
        environment.jersey().register(new AuthDynamicFeature(authFilter));

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        //If you want to use @Auth to inject a custom Principal type into your resource
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(AuthPrincipal.class));
    }

    private void addUserIdCardResource(ZYExpressConfiguration configuration, Environment environment, DBI jdbi) {
        final UserIdCardDAO userIdCardDAO = jdbi.onDemand(UserIdCardDAO.class);
        userIdCardDAO.createUserIdCardTable();
        //userIdCardDAO.insert("身份证号", "姓名"); // for test
        //userIdCardDAO.insert("", ""); // for test
        //userIdCardDAO.deleteByUserName("姓名");
        //userIdCardDAO.deleteByUserName("");

        final UserIdCardResource userIdcardResource = new UserIdCardResource(userIdCardDAO, configuration.getUploadDir());
        environment.jersey().register(userIdcardResource);
    }

    private void addAdminResource(ZYExpressConfiguration configuration, Environment environment, DBI jdbi) {
        final UserDAO userDAO = jdbi.onDemand(UserDAO.class);
        userDAO.createUserTable();
        final IdCardDAO idCardDAO = jdbi.onDemand(IdCardDAO.class);
        /* for test only
        userDAO.deleteByUserName("admin");
        userDAO.insert(new User("admin", "admin", true, true));
        */
        final AdminResource adminResource = new AdminResource(userDAO,idCardDAO, configuration.getUploadDir(), jdbi);
        environment.jersey().register(adminResource);
    }

    private void addUserResource(Environment environment, DBI jdbi) {
        final UserDAO userDAO = jdbi.onDemand(UserDAO.class);
        userDAO.createUserTable();

        final UserResource userResource = new UserResource(userDAO);
        environment.jersey().register(userResource);
    }

    private void addPackageResource(ZYExpressConfiguration configuration, Environment environment, DBI jdbi) {
        final PackageDAO packageDAO = jdbi.onDemand(PackageDAO.class);
        packageDAO.createPackageTable();
        packageDAO.createPackageItemTable();

        final PackageResource packageResource = new PackageResource(jdbi, packageDAO);
        environment.jersey().register(packageResource);
    }

    private void addAddressResource(ZYExpressConfiguration configuration, Environment environment, DBI jdbi) {
        final AddressDAO addressDAO = jdbi.onDemand(AddressDAO.class);
        addressDAO.createAddressItemTable();
        final AddressResource addressResource = new AddressResource(addressDAO);
        environment.jersey().register(addressResource);
    }

    private void addIdCardManageResource(ZYExpressConfiguration configuration, Environment environment, DBI jdbi) {
        final IdCardDAO idCardDAO = jdbi.onDemand(IdCardDAO.class);
        idCardDAO.createIdCardItemTable();
        String uploadDir = configuration.getUploadDir();
        final IdCardManageResource idCardManageResource = new IdCardManageResource (idCardDAO, uploadDir);
        environment.jersey().register(idCardManageResource);
    }
}
