#! /usr/bin/env bash

cd batch-krss-topdown-sim
mvn spring-boot:run
cd ..

cd batch-krss-topdown-simpi
mvn spring-boot:run
cd ..

cd batch-krss-dynamicprogramming-sim
mvn spring-boot:run
cd ..

cd batch-krss-dynamicprogramming-simpi
mvn spring-boot:run
cd ..

tail -n +1 ./batch-krss-*/output/output	
