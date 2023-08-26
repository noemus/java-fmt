package com.github.noemus.javafmt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaFormatter {

    private static final int MAX_SOURCE_DEPTH = 99;

    public static void main(String... args) {
        if (args.length != 1) {
            usage();
        }
        try {
            formatSources(Path.of(args[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void formatSources(Path sourceRoot) throws IOException {
        System.out.println("Formatting sources:");
        try (var javaFiles = Files.find(sourceRoot, MAX_SOURCE_DEPTH, (path, attrs) -> isJavaFile(path))) {
            javaFiles.forEach(JavaFormatter::formatSource);
        }
        System.out.println("Finished.");
    }

    static void formatSource(Path javaFile) {
        Lexer.from(javaFile)
             .tokens()
             .map(Lexer.Token::text)
             .forEach(System.out::print);
    }

    private static boolean isJavaFile(Path path) {
        return path.getFileName().toString().endsWith(".java");
    }

    private static void usage() {
        throw new InvalidUsage("""
            
            ========================================================================
            Usage:
            java -jar java-fmt.jar <path to java source root>
            ========================================================================
            """
        );
    }
}