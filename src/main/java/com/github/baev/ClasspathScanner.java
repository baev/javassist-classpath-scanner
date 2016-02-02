package com.github.baev;

import javassist.bytecode.ClassFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.11.15
 */
public class ClasspathScanner {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClasspathScanner.class);

    public static final String FILE_SCHEME = "file";
    public static final String CLASS_GLOB = "*.class";
    public static final String CLASS_SUFFIX = ".class";

    private final Set<URI> scannedUris = new HashSet<>();

    private final Set<ClassFile> classes = new HashSet<>();

    /**
     * Finds the all classes if given uris.
     */
    public static Set<ClassFile> getAllClasses(URI... uris) {
        return getAllClasses(Arrays.asList(uris));
    }

    /**
     * Finds the all classes if given uris.
     */
    public static Set<ClassFile> getAllClasses(List<URI> uris) {
        ClasspathScanner scanner = new ClasspathScanner();
        uris.forEach(scanner::scan);
        return scanner.getClasses();
    }

    public Set<ClassFile> getClasses() {
        return classes;
    }

    /**
     * Scan given uri and index all classes.
     *
     * @param uri the uri to scan.
     */
    public void scan(URI uri) {
        if (uri.getScheme().equals(FILE_SCHEME) && scannedUris.add(uri)) {
            scanFrom(Paths.get(uri));
        }
    }

    /**
     * Scan given path and index all classes.
     *
     * @param path the path to scan.
     */
    public void scanFrom(Path path) {
        if (Files.notExists(path)) {
            return;
        }

        if (Files.isDirectory(path)) {
            scanDirectory(path);
        } else {
            scanJar(path);
        }
    }

    /**
     * Scan given directory and index all classes.
     *
     * @param path the directory to scan.
     */
    protected void scanDirectory(Path path) {
        try {
            Files.walk(path)
                    .filter(this::isClassFile)
                    .forEach(this::processClass);
        } catch (Exception e) {
            LOGGER.debug("Could not scan the directory " + path, e);
        }
    }

    /**
     * Scan given jar and index all classes. The method also looks for the
     * classpath manifest attribute and scan it as well.
     *
     * @param path the path to the jar to scan.
     */
    protected void scanJar(Path path) {
        try (JarFile jar = new JarFile(path.toFile())) {
            scanFromManifestClassPath(path, jar.getManifest());
            List<JarEntry> entries = Collections.list(jar.entries());
            entries.stream()
                    .filter(this::isClassFile)
                    .forEach(entry -> processClass(jar, entry));
        } catch (IOException e) {
            LOGGER.debug("Could not scan the jar " + path, e);
        }
    }

    /**
     * Process the class by the given path.
     *
     * @param path the path to the class file.
     */
    protected void processClass(Path path) {
        try (InputStream stream = Files.newInputStream(path)) {
            processClass(stream);
        } catch (IOException e) {
            LOGGER.debug("Could not process class " + path, e);
        }
    }

    /**
     * Process the class entry in jar file.
     *
     * @param jar   the jar to process class entry.
     * @param entry the entry to process.
     */
    protected void processClass(JarFile jar, JarEntry entry) {
        try (InputStream stream = jar.getInputStream(entry)) {
            processClass(stream);
        } catch (IOException e) {
            LOGGER.debug("Could not process class entry " + entry + " into jar " + jar, e);
        }
    }

    /**
     * Process the class.
     *
     * @param in the byte input stream of the class file.
     * @throws IOException if any occurs.
     */
    protected void processClass(InputStream in) throws IOException {
        try (DataInputStream stream = new DataInputStream(in)) {
            classes.add(new ClassFile(stream));
        }
    }

    /**
     * Scan the classpath from given manifest.
     *
     * @param jar      the path to the jar file.
     * @param manifest the manifest to scan classpath.
     */
    protected void scanFromManifestClassPath(Path jar, Manifest manifest) {
        classpath(manifest).stream()
                .map(element -> toAbsoluteUri(jar, element))
                .forEach(this::scan);
    }

    /**
     * Get classpath from given manifest.
     *
     * @param manifest the manifest to extract classpath.
     * @return the list of classpath elements.
     */
    protected List<String> classpath(Manifest manifest) {
        return Optional.ofNullable(manifest)
                .map(Manifest::getMainAttributes)
                .map(attributes -> attributes.getValue(Attributes.Name.CLASS_PATH))
                .map(this::splitClasspath)
                .orElse(Collections.emptyList());
    }

    /**
     * Returns the absolute URI for given classpath element.
     *
     * @param jar              the path to jar file. Will be used in case the classpath element is relative.
     * @param classpathElement the element to convert to URI.
     * @return the absolute URI for given classpath element.
     */
    protected URI toAbsoluteUri(Path jar, String classpathElement) {
        URI uri = URI.create(classpathElement);
        return uri.isAbsolute() ? uri : jar.getParent().resolve(classpathElement).toUri();
    }

    /**
     * Split the given classpath.
     */
    protected List<String> splitClasspath(String classpath) {
        return Arrays.asList(classpath.split("\\s+"));
    }

    /**
     * Returns true if given path is a class and false otherwise.
     *
     * @param path the path to check.
     * @return true if given path is a class and false otherwise.
     */
    protected boolean isClassFile(Path path) {
        return isClassFile(path.getFileName().toString());
    }

    /**
     * Returns true if given entry is a class and false otherwise.
     *
     * @param entry the entry to check.
     * @return true if given entry is a class and false otherwise.
     */
    protected boolean isClassFile(JarEntry entry) {
        return isClassFile(entry.getName());
    }

    /**
     * Returns true if given string is a class name and false otherwise.
     *
     * @param fileName the entry name to check.
     * @return true if given entry name is a class name and false otherwise.
     */
    protected boolean isClassFile(String fileName) {
        return fileName.endsWith(CLASS_SUFFIX);
    }
}
