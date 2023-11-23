create table tenants(
    id integer primary key AUTOINCREMENT,
    name varchar(75),
    phoneNumber varchar(12),
    email varchar(100),
    checkInDate varchar(100),
    checkOutDate varchar(100),
    aptNum varchar(10)
);

create table requests(
     id integer primary key AUTOINCREMENT,
     tenantId integer,
     aptNum varchar(10),
     location varchar(50),
     description varchar(500),
     time timestamp,
     photoURL varchar(500),
     status char(1)
);