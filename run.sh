#!/bin/bash

export $(cat .env | xargs)

# Run Spring Boot
mvn spring-boot:run