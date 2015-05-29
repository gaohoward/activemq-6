# ActiveMQ 5 unit tests against ActiveMQ Artemis wrapper


This maven module is used to run ActiveMQ5 unit tests against
ActiveMQ Artemis broker.

The Artemis broker is 'wrapped' in BrokerService and the unit
tests are slightly modified.

This module depends on activemq source jars that are not available
on maven repositories. To get the source jar you need to build and install
them into your local repository. First clone the activemq5 git repo

git clone https://github.com/apache/activemq.git

then patch the pom.xml using etc/patches/amq5_source_pom.patch.
then do

```mvn install -DskipTests```

Then run the tests simply do

```mvn -DskipActiveMQTests=false -DskipLicenseCheck=true clean test```

It will kickoff the whole test suite.

