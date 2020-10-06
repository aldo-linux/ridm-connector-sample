# HOW TO RUN DATABASE TESTS

- Go to dockerDb folder and run `docker-compose up` to get mysql and postgresql instance
- Create in postgresql a table named "demo" with attributes
    1. id(varchar)(primaryKey)(3072)
    2. firstname(varchar)(50)
    3. displayname(varchar)(100)
- Create in mysql a table named "example" with attributes
    1. id(varchar)
    2. firstname(varchar)(primaryKey)(50)
    3. displayname(varchar)(100)
- Open connector folder with Spring tools suite
- Go to src/test/java and open DatabaseConnectorUnitTest.java
- Change `bundleDirectory` (line 58) variable with your local root that has database table bundle
- Right click in the file and select Run as J Unit Test

-You will see result like following image:

![image](../dockerDB/test.jpg)

NOTE: If testDeleteDatabaseAccount fails, you need to run test again, this test fails because test suite run it first and the first time you don't have records in your database

## HOW TO RUN TEST REST SERVICE USING SPRING BOOT

./mvnw spring-boot:run