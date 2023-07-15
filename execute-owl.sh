#! /usr/bin/env bash

cd batch-owl-topdown-sim
mvn spring-boot:run
cd ..

cd batch-owl-topdown-simpi
mvn spring-boot:run
cd ..

cd batch-owl-dynamicprogramming-sim
mvn spring-boot:run
cd ..

cd batch-owl-dynamicprogramming-simpi
mvn spring-boot:run
cd ..

tail -n +1 ./batch-owl-*/output/output	
