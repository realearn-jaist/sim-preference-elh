# Similarity under Preference in Description Logic ELH

## Features
This project exposes JAVA-based APIs to measure a similarity of OWL-based class expression 
based on the logic-based semantics of the Description Logic ELH (see the requirements in [pom.xml](https://github.com/realearn-jaist/sim-preference-elh/blob/main/pom.xml)). 

The project is based on the [Spring Boot CLI](https://docs.spring.io/spring-boot/docs/current/reference/html/cli.html). 
Its structure is organized as follows: 
* core: This module contains all the implementation versions of our similarity measure for the description logic ELH, namely the top-down and bottom-up versions for both OWL-styled and KRSS-styled concept expressions. 
* batch-krss-dynamicprogramming-sim: This implementation is an example of using the core module for creating a batch program for the bottom-up similarity measure for the KRSS-styled concept expressions. 
* batch-krss-dynamicprogramming-simpi: This implementation is an example of using the core module for creating a batch program for the bottom-up similarity measure under preference for the KRSS-styled concept expressions.
* batch-krss-topdown-sim: This implementation is an example of using the core module for creating a batch program for the top-down similarity measure for the KRSS-styled concept expressions. 
* batch-krss-topdown-simpi: This implementation is an example of using the core module for creating a batch program for the top-down similarity measure under preference for the KRSS-styled concept expressions. 
* batch-owl-dynamicprogramming-sim: TBD
* batch-owl-dynamicprogramming-simpi: TBD
* batch-owl-topdown-sim: TBD
* batch-owl-topdown-simpi: TBD

## Stanalone
TBD

## How to Tune with Pre-trained Embedding
TBD

## Publications 

* Teeradaj Racharak, On Approximation of Concept Similarity Measure in Description Logic ELH with Pre-trained Word Embedding, In IEEE Access, vol. 9, pp. 61429-61443, 2021. DOI: 10.1109/ACCESS.2021.3073730
* Teeradaj Racharak, Boontawee Suntisrivaraporn, and Satoshi Tojo, Personalizing a Concept Similarity Measure in the Description Logic ELH with Preference Profile, In Computing and Informatics vol. 37, no. 3, pp. 581-613, 2018. DOI: 10.4149/cai_2018_3_581
