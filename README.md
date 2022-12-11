# Automated brewery project - host

The hobby project intended to automate process of brewing.

This is java application and controller javascript frontend part. The other projects are located at: [brewery-driver](https://github.com/Michal-Wadowski/brewery-driver)

## Development

### Start backend
Right click on `src/main/java/wadosm/breweryhost/BreweryHostApplication.java` and Modify Run Configuration.
In program arguments add `--spring.profiles.active=local`

### Start frontend

```shell
cd frontend
npm i
npm start
```

The HTTP requests to `/api/*` will be redirected from to backend port `8080`
without changing the path.

## Build package

```shell
./mvnw clean package
```

## Run application

```shell
java -jar backend/target/backend-*.jar
```
