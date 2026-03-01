# Changes for turning this example project into a new project


## API
- in pom.xml, replace nl.wateralmanak with your with your package name (unique projectID = reversed url)
- in src/webapp/WEB-INF/web.xml, replace Wateralmanak API with your display name and nl.wateralmanak with your package name
- in src/main/java directory:
  - replace all nl.wateralmanak with your packagename in all file's packagenames
  - replace src/main/java/nl/wateralmanak with src/main/java/YOUR/PACKAGENAME
- in src/main/java/YOUR/PACKAGENAME/config/ApplicationConfig.java replace nl.wateralmanak with your packagename twice. Also rename the resources if you rename them in resource directory.
- in src/main/java/YOUR/PACKAGENAME/config/DatabaseConfig.java replace defaultvalues 3 times (with your defaults)
- in src/main/java/YOUR/PACKAGENAME/config/KeycloakSecurityFilter.java replace realname with your realm name
- Change Voorzieningen-model, -repository, -resource and -service with the ones you need.

## DATABASE
No changes needed

## FRONTEND
- in index.html replace wateralmanak into your projectname 1x.
- in app.component.html replace wateralmanak into your projectname 1x.
- in environments/environment.ts change this 4 values according to your env-file (they will only be used for local-dev: ng serve)
  apiUrl: 'http://<APP_HOSTNAME>/api',
  keycloakUrl: 'http://<APP_HOSTNAME>/auth',
  keycloakRealm: '<KEYCLOAK_REALM>'
- replace all references to the api with your new api-methods (including models etc)
- replace all references to keycloak-roles according t your roles and needs
- replace all components, services etc to whatever you need.

## KEYCLOAK
- rename realm/config/wateralmanak-realm.config into <your-new-realm-name>-realm.config.
- in the first line of the file, replace wateralmanak with your new realm name.
- also in the file: create roles, groupes and users as needed. Delete old ones. ALWAYS USE STRONG PASWORDS.

## LIQUIBASE
- Replace 'wateralmanak' in changelog/changes/001-create-schema.xml three times with your new database-schema name (see .env-file).
- Remove 002-create-voorzieningen-table.xml and add new files needed for creating tables, views, functions etc and to inject data. Use 002-create-voorzieningen-table.xml as an example.
- Remove te include for 002-create-voorzieningen-table.xml in changelog/db.changelog-master.xml and add all new created files in chages-directory.

## NGINX
No changes needed

## POSTMAN
change postman collection according to your api-changes

## docker-compose
No changes needed

## env-file
- Replace all usernames and passwords. ALWAYS USE STRONG PASSWORDS.
- replace version numbers if you must (take care of dependencies).
- replace ports if you like.