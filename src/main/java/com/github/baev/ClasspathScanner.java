package com.github.baev;

import com.google.common.collect.Sets;
import javassist.bytecode.ClassFile;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
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

    private final Set<URI> scannedUris = Sets.newHashSet();

    private final Set<ClassFile> classes = new HashSet<>();

    public Set<ClassFile> getClasses() {
        return classes;
    }

    public void scan(URI uri) {
        if (uri.getScheme().equals("file") && scannedUris.add(uri)) {
            scanFrom(Paths.get(uri));
        }
    }

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

    protected void scanDirectory(Path path) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.class")) {
            for (Path child : stream) {
                processClass(child);
            }
        } catch (Exception ignored) {
            //ignore
        }
    }

    protected void scanJar(Path path) {
        try (JarFile jar = new JarFile(path.toFile())) {
            scanFromManifestClassPath(path, jar.getManifest());
            List<JarEntry> entries = Collections.list(jar.entries());
            entries.stream()
                    .filter(this::isClassEntry)
                    .forEach(entry -> processClass(jar, entry));
        } catch (IOException ignored) {
            //ignore
        }
    }

    protected void processClass(Path path) {
        try (InputStream stream = Files.newInputStream(path)) {
            processClass(stream);
        } catch (IOException ignored) {
            //ignore
        }
    }

    protected void processClass(JarFile jar, JarEntry entry) {
        try (InputStream stream = jar.getInputStream(entry)) {
            processClass(stream);
        } catch (IOException ignored) {
            //ignore
        }
    }


    protected void processClass(InputStream in) {
        try (DataInputStream stream = new DataInputStream(in)) {
            classes.add(new ClassFile(stream));
        } catch (IOException ignored) {
            //ignore
        }
    }

    protected boolean isClassEntry(JarEntry entry) {
        return entry.getName().endsWith(".class");
    }

    protected void scanFromManifestClassPath(Path jar, Manifest manifest) throws IOException {
        List<String> classpath = classpath(manifest);
        classpath.stream()
                .map(element -> getUri(jar, element))
                .forEach(this::scan);
    }

    protected URI getUri(Path jar, String classpathElement) {
        URI uri = URI.create(classpathElement);
        return uri.isAbsolute() ? uri : jar.getParent().resolve(classpathElement).toUri();
    }

    protected List<String> classpath(Manifest manifest) {
        String classpath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
        return Optional.ofNullable(classpath)
                .map(this::splitClasspath)
                .orElse(Collections.emptyList());
    }

    protected List<String> splitClasspath(String classpath) {
        return Arrays.asList(classpath.split("\\s+"));
    }
}
