## Simple classpath scanner for Javassist

[![release](http://github-release-version.herokuapp.com/github/baev/javassist-classpath-scanner/release.svg?style=flat)](https://github.com/baev/javassist-classpath-scanner/releases/latest) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.baev/javassist-classpath-scanner/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.baev/javassist-classpath-scanner) [![build](https://img.shields.io/jenkins/s/http/ci.qatools.ru/javassist-classpath-scanner_master-deploy.svg?style=flat)](http://ci.qatools.ru/job/javassist-classpath-scanner_master-deploy/lastBuild/)
[![covarage](https://img.shields.io/sonar/http/sonar.qatools.ru/com.github.baev:javassist-classpath-scanner/coverage.svg?style=flat)](http://sonar.qatools.ru/dashboard/index/com.github.baev:javassist-classpath-scanner)

Lightweight javaassist classpath scanner. 

## Usage

The artifact avaliable at Maven Central:

```xml
<dependency>
	<groupId>com.github.baev</groupId>
	<artifactId>javassist-classpath-scanner</artifactId>
	<version>RELEASE</version>
</dependency>
```


```java
List<URI> classpath = ...

ClasspathScanner scanner = new ClasspathScanner();
classpath.forEach(scanner::scan);
Set<ClassFile> = scanner.getClasses();
```

Then you can do whatever you need with the found classes. For example, you can find all the JUnit tests using code like this: 
```java
public boolean isTestMethod(MethodInfo methodInfo) {
    return JavassistUtils.isAnnotated(methodInfo, "org.junit.Test");
}

public List<MethodInfo> getTestMethods(ClassFile clazz) {
    return JavassistUtils.getMethods(clazz, this::isTestMethod);
}

List<MethodInfo> testMethods = scanner.getClasses().stream()
    .map(this::getTestMethods)
    .collect(Collectors.toList());
```
