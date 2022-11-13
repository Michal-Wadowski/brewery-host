# Automated brewery project - host

The hobby project intended to automate process of brewing.

This is java application controller part. The other projects are located at: [brewery-driver](https://github.com/Michal-Wadowski/brewery-driver) and [brewery-android-client](https://github.com/Michal-Wadowski/brewery-android-client)

# Development

To build project:

```shell
./mvnw clean package
```

## Backend

Please run backend with profile **local**: `--spring.profiles.active=local`
To debug driver information please use **debug-driver** profile.

## Frontend

Frontend needs running backend to work. To start frontend environment:

```shell
cd frontend
npm install
npm start
```

Server runs on port 8000. HTTP requests are internally proxied to 8080 (backend). 
