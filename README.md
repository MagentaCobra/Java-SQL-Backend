# Java-SQL-Backend

Resource Manager functionality written in a Java class connected to two SQL databases linked by a foreign key. Unless otherwise specified, all methods take a request for input and output either a JSON of the query made or some indicator as to whether the query was successful or not.

Two packages (jar files) are needed for this to run successfully. The first package provides the Java Database Backend Connecter (JDBC), the second package provides JSONObject and JSONArray for parsing and manipulating information stored in JSON units.

I used MySQL Workbench to build the schema for the User and UserAddress tables, and I used IntelliJ to implement the Java backend component. The CREATE TABLE commands with the required schema are provided here.
