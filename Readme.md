# Font failure in headless mode on lambda Java Runtime 21 in Localstack.
                     
## Issue
In AWT headless mode we should still be able to load and work with fonts.  Due to malformed 
open jdk build due to fontconfig being misconfigured at the OS level, Headless font operations break as shown in the code below:
```java
    GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    for (Font font : environment.getAllFonts()) {
        System.out.println(font.getFontName() + " " + font.getFamily());
    }
```
## Expected Behaviour
No output if no fonts configured, or at least the "default" fonts that are part of the Java VM implementation.   On AWS real we get the JVM minimum logical font families (Dialog, DialogInput, Monospaced, SansSerif, Serif) and "Noto Sans" on the 21 runtime.  You can also load custom fonts into the lambda Runtime VM.

## Current behaviour on Localstack only
Throwing a Runtime exception with 
```text
Fontconfig head is null, check your fonts or fonts configuration.
```

# What's in this code?
* maven module that generates a Java lambda jar (font-failure-lambda).
* Jar is deployed to localstack and tests are run (See LambdaIT.java).
* Jar is deployed to an AWS account (CDK using AWS default profile credentials). See font-failure-deploy.
* CDK deploy does a CDK bootstrap with default settings first.

### How do I deploy this on an AWS account with that failing test?
```shell
mvn clean install -DskipITs
```

---

# What happens on AWS Real?

This deploys cleanly in a real AWS account and returns configured font
information:

## Using the deployed lambda
Send an input event and the font dump is returned.  The dump is also output to logs that are visible in the AWS Lambda console (cloudwatch).

#### Input Event
```json
{
}
```

#### AWS Runtime Font Dump
```text
Dialog.bold Dialog
Dialog.bolditalic Dialog
Dialog.italic Dialog
Dialog.plain Dialog
DialogInput.bold DialogInput
DialogInput.bolditalic DialogInput
DialogInput.italic DialogInput
DialogInput.plain DialogInput
Monospaced.bold Monospaced
Monospaced.bolditalic Monospaced
Monospaced.italic Monospaced
Monospaced.plain Monospaced
Noto Sans Italic Noto Sans
Noto Sans Regular Noto Sans
SansSerif.bold SansSerif
SansSerif.bolditalic SansSerif
SansSerif.italic SansSerif
SansSerif.plain SansSerif
Serif.bold Serif
Serif.bolditalic Serif
Serif.italic Serif
Serif.plain Serif
```
       
# Localstack Git Issue raised
https://github.com/localstack/localstack/issues/12104
