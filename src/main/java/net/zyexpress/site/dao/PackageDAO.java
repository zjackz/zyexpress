package net.zyexpress.site.dao;

import net.zyexpress.site.api.Package;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface PackageDAO {
    @SqlUpdate("create table if not exists package (id int generated by default as identity, " +
            "accountName varchar(64) not null, weight double not null)")
    void createPackageTable();

    @SqlUpdate("create table if not exists packageItem (id int generated by default as identity, " +
            "packageId integer not null, name varchar(64) not null, brand varchar(128) not null, " +
            "specification varchar(128) null, quantity integer not null)")
    void createPackageItemTable();

    @SqlUpdate("insert into package (accountName, weight) values(:accountName, :weight)")
    @GetGeneratedKeys
    long addPackage(@BindBean Package pkg);

    @SqlUpdate("insert into packageItem (packageId, name, brand, specification, quantity) " +
            "values(:packageId, :name, :brand, :specification, :quantity)")
    void addPackageItem(@Bind("packageId") long packageId, @BindBean Package.PackageItem packageItem);
}
