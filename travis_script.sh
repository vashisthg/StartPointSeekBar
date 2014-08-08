#!/bin/bas

if [[ $TRAVIS_BRANCH == 'master' ]]
    if(./gradlew assembleDebugTest)
        ./gradlew assembleDebug
    fi

else
    ./gradlew assembleDebugTest
 fi