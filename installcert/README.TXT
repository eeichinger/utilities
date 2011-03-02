FROM http://blogs.sun.com/gc/entry/unable_to_find_valid_certification

run:

	java InstallCert host:port

to install private SSL certificate into a new store "jssecacerts"
to publish, copy to %JRE_HOME%/lib/security

it may be necessary to copy the generated jssecacerts file to the user's home ~/.keystore file for java apps to pick it up properly

