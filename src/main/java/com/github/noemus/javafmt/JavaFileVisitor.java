package com.github.noemus.javafmt;

import com.puppycrawl.tools.checkstyle.grammar.java.JavaLanguageLexer;
import com.puppycrawl.tools.checkstyle.grammar.java.JavaLanguageParser;
import com.puppycrawl.tools.checkstyle.grammar.java.JavaLanguageParserBaseVisitor;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.util.List;

import static java.lang.System.lineSeparator;

public class JavaFileVisitor extends JavaLanguageParserBaseVisitor<String> {
    private static final String INDENT = "    ";
    private static final String NL = lineSeparator();

    private final StringBuilder buffer = new StringBuilder();

    private int level = 0;

    private void indent() {
        level++;
    }

    private void unndent() {
        level--;
    }

    private void indentation() {
        buffer.append(INDENT.repeat(level));
    }

    private final BufferedTokenStream tokens;

    public JavaFileVisitor(CommonTokenStream tokenStream) {
        tokens = tokenStream;
    }

    @Override
    public String visitCompilationUnit(JavaLanguageParser.CompilationUnitContext ctx) {
        visit(ctx.packageDeclaration());
        newLine();

        ctx.importDeclaration().forEach(this::visit);
        newLine();

        ctx.typeDeclaration().forEach(this::visit);

        tokens.getHiddenTokensToLeft(ctx.stop.getTokenIndex()).forEach(this::visitComment);

        return buffer.toString();
    }

    private void newLine() {
        buffer.append(NL);
    }

    private void visitComment(Token token) {
        if (token.getType() == JavaLanguageLexer.BLOCK_COMMENT_BEGIN) {
            indentation();
            buffer.append("/*");
            newLine();
            buffer.append(" * ").append(token.getText().trim());
            newLine();
            indentation();
            buffer.append(" */");
            newLine();
        }
        if (token.getType() == JavaLanguageLexer.BLOCK_COMMENT_CONTENT) {
            buffer.append(" * ").append(token.getText().trim());
            newLine();
        }
        if (token.getType() == JavaLanguageLexer.BLOCK_COMMENT_END) {
            indentation();
            buffer.append(" */");
            newLine();
        }
    }

    @Override
    public String visitPackageDeclaration(JavaLanguageParser.PackageDeclarationContext ctx) {
        var comments = tokens.getHiddenTokensToLeft(ctx.start.getTokenIndex());
        if (comments != null) {
            comments.forEach(this::visitComment);
        }
        buffer.append("package ");
        buffer.append(ctx.qualifiedName().getText());
        buffer.append(";");
        newLine();
        return super.visitPackageDeclaration(ctx);
    }

    @Override
    public String visitImportDec(JavaLanguageParser.ImportDecContext ctx) {
        var comments = tokens.getHiddenTokensToLeft(ctx.start.getTokenIndex());
        if (comments != null) {
            comments.forEach(this::visitComment);
        }

        buffer.append("import ");
        if (ctx.LITERAL_STATIC() != null) {
            buffer.append("static ");
        }
        buffer.append(ctx.qualifiedName().getText());
        if (ctx.STAR() != null) {
            buffer.append(".*");
        }
        buffer.append(";");
        newLine();

        var trailingComments = tokens.getHiddenTokensToRight(ctx.stop.getTokenIndex());
        if (trailingComments != null) {
            trailingComments.forEach(this::visitComment);
        }
        return super.visitImportDec(ctx);
    }
}