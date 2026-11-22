package net.guarnie.maven.plugin.data.jdbc;

import org.jspecify.annotations.NonNull;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class CompileHelper {
    private CompileHelper() {}

    /**
     * Compiles all Java files located in the specified directory and its subdirectories.
     *
     * @param sourceDir the root directory containing the Java source files to be compiled
     * @return true if the compilation succeeds and false if there are no Java files to compile
     * @throws Exception if an error occurs during compilation or file reading
     */
    public static boolean compile(Path sourceDir) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("No Java compilers found. Make sure you test with a JDK and not just a JRE");

        JavaFileCollector collector = new JavaFileCollector();
        Files.walkFileTree(sourceDir, collector);

        List<File> javaFiles = collector.getJavaFiles();
        if (javaFiles.isEmpty()) return false;

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(javaFiles);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
            return Boolean.TRUE.equals(task.call());
        }
    }

    /**
     * Extends {@link SimpleFileVisitor} to collect all Java files within a directory
     * and its subdirectories during a file tree walk. Java files are identified by the
     * ".java" file extension and stored in an internal list.
     */
    private static class JavaFileCollector extends SimpleFileVisitor<Path> {
        private final List<File> javaFiles = new ArrayList<>();

        @Override
        public @NonNull FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) {
            if (attrs.isRegularFile() && file.getFileName().toString().endsWith(".java")) {
                javaFiles.add(file.toFile());
            }
            return FileVisitResult.CONTINUE;
        }

        public List<File> getJavaFiles() {
            return javaFiles;
        }
    }
}


