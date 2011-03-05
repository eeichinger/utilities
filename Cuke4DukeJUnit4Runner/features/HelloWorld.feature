Feature: Hello World Feature
  In order to ensure that my installation works
  As a Developer
  I want to run a quick Cucumber test

Scenario: Maven/Cucumber/Java successfully interact
	Given The Action is Hello
	When The Subject is World
	Then The Greeting is Hello, World

@doesntexist
Scenario: A global scenario
	Given The Action is Hallo
	When The Subject is Welt
	Then The Greeting is Hallo, Welt