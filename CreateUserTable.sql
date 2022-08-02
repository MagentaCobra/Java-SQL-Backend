CREATE TABLE User (
  userId int unsigned NOT NULL AUTO_INCREMENT,
  firstName varchar(30) NOT NULL,
  lastName varchar(30) NOT NULL,
  annualSalary decimal(9,2) unsigned NOT NULL,
  dateOfBirth datetime NOT NULL,
  email varchar(45) NOT NULL,
  gender varchar(45) NOT NULL,
  mobilePhone varchar(10) NOT NULL,
  userType varchar(45) NOT NULL,
  PRIMARY KEY (userId)
);
