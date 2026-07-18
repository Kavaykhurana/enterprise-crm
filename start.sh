#!/bin/sh
# Render provides DATABASE_URL as postgres://user:password@host:port/dbname
# Spring Boot requires jdbc:postgresql://host:port/dbname + separate user/password
if [ -n "$DATABASE_URL" ]; then
    # Extract components from postgres://user:password@host:port/dbname
    RAW="$DATABASE_URL"
    # Remove postgres:// prefix
    STRIPPED="${RAW#postgres://}"
    # user:password@host:port/dbname
    USERINFO="${STRIPPED%%@*}"
    HOSTINFO="${STRIPPED#*@}"
    DB_USER="${USERINFO%%:*}"
    DB_PASS="${USERINFO#*:}"
    HOST="${HOSTINFO%%/*}"
    DBNAME="${HOSTINFO#*/}"

    export DATABASE_URL="jdbc:postgresql://${HOST}/${DBNAME}"
    export DB_USERNAME="$DB_USER"
    export DB_PASSWORD="$DB_PASS"
fi

exec java -jar /app/app.jar
