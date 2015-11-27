## Simple classpath scanner for Javassist

```java
List<URI> classpath = ...

ClasspathScanner scanner = new ClasspathScanner();
classpath.forEach(scanner::scan);
Set<ClassFile> = scanner.getClasses();
```