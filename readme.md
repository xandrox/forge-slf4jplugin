Jboss Forge slf4j Plugin
========================

This plugin adds slf4j dependencies to your project and generates a initial logging configuration.

Setup slf4j
-----------

    slf4j setup --log-backend LOG4J
    
or
    
    slf4j setup --log-backend JBOSS_AS_7
    
Add a logger constant to your class
-----------------------------------

If you want a Logger constant like this:

    private static final Logger LOG = LoggerFactory.getLogger(Test.class);

just call:

    slf4j addlogger com.example.slf4jtest.Test.java
  

Remove slf4j from your project
------------------------------

type:

    slf4j uninstall
