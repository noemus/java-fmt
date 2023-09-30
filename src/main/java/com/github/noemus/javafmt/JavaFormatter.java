package com.github.noemus.javafmt;

import com.puppycrawl.tools.checkstyle.CheckstyleParserErrorStrategy;
import com.puppycrawl.tools.checkstyle.grammar.CommentListener;
import com.puppycrawl.tools.checkstyle.grammar.java.JavaLanguageLexer;
import com.puppycrawl.tools.checkstyle.grammar.java.JavaLanguageParser;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

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
            javaFiles.forEach(javaFile -> {
                try {
                    formatSource(javaFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        System.out.println("Finished.");
    }

    static void formatSource(Path javaFile) throws IOException {
        var codePointCharStream = CharStreams.fromPath(javaFile);
        var lexer = new JavaLanguageLexer(codePointCharStream, true);
        lexer.removeErrorListeners();
        lexer.setCommentListener(new CommentListener() {
            @Override
            public void reportSingleLineComment(String type, int startLineNo, int startColNo) {

            }
            @Override
            public void reportBlockComment(String type, int startLineNo, int startColNo, int endLineNo, int endColNo) {

            }
        });
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new JavaLanguageParser(tokenStream, JavaLanguageParser.CLEAR_DFA_LIMIT);
        parser.setErrorHandler(new CheckstyleParserErrorStrategy());
        parser.removeErrorListeners();
        parser.addErrorListener(new LinterErrorListener());

        var visitor = new JavaFileVisitor(tokenStream);
        String source = visitor.visitCompilationUnit(parser.compilationUnit());
        System.out.println(source);
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

    private static final class LinterErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException ex
        ) {
            throw new IllegalStateException(line + ":" + charPositionInLine + ": " + msg, ex);
        }
    }
}