# migration-one

Simple playground for hand rolled database migrations with Spring Boot

## todo

    - table creation
    - validate the model (not in prod)
    - run scripts in transactions

## run

    gradle bootRun

## gradle dep updates

    .\gradlew dependencyUpdates -Drevision=release

## gradle warnings

    .\gradlew build --warning-mode=all