CREATE TABLE UserAddress (
    addressId INT UNSIGNED AUTO_INCREMENT UNIQUE NOT NULL PRIMARY KEY,
    addrLn1 varchar(50) not null,
    addrName varchar(30) not null,
    addrType varchar(50) not null,
    city varchar(30) not null,
    stateCode varchar(10) not null,
    postalCode varchar(10) not null,
    country varchar(10) not null,
    userId int unsigned,
    FOREIGN KEY (userId) REFERENCES User(userId)
);
