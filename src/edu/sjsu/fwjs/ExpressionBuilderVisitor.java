package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.fwjs.parser.FeatherweightJavaScriptBaseVisitor;
import edu.sjsu.fwjs.parser.FeatherweightJavaScriptParser;

public class ExpressionBuilderVisitor extends FeatherweightJavaScriptBaseVisitor<Expression>{
    @Override
    public Expression visitProg(FeatherweightJavaScriptParser.ProgContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i=0; i<ctx.stat().size(); i++) {
            Expression exp = visit(ctx.stat(i));
            if (exp != null) stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    @Override
    public Expression visitBareExpr(FeatherweightJavaScriptParser.BareExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitIfThenElse(FeatherweightJavaScriptParser.IfThenElseContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block(0));
        Expression els = visit(ctx.block(1));
        return new IfExpr(cond, thn, els);
    }

    @Override
    public Expression visitIfThen(FeatherweightJavaScriptParser.IfThenContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block());
        return new IfExpr(cond, thn, new ValueExpr(new NullVal()));
    }

    @Override
    public Expression visitInt(FeatherweightJavaScriptParser.IntContext ctx) {
        int val = Integer.valueOf(ctx.INT().getText());
        return new ValueExpr(new IntVal(val));
    }


    @Override
    public Expression visitParens(FeatherweightJavaScriptParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitFullBlock(FeatherweightJavaScriptParser.FullBlockContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i=1; i<ctx.getChildCount()-1; i++) {
            Expression exp = visit(ctx.getChild(i));
            stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    /**
     * Converts a list of expressions to one sequence expression,
     * if the list contained more than one expression.
     */
    private Expression listToSeqExp(List<Expression> stmts) {
        if (stmts.isEmpty()) return new ValueExpr(new NullVal());
        Expression exp = stmts.get(0);
        for (int i=1; i<stmts.size(); i++) {
            exp = new SeqExpr(exp, stmts.get(i));
        }
        return exp;
    }

    @Override
    public Expression visitSimpBlock(FeatherweightJavaScriptParser.SimpBlockContext ctx) {
        return visit(ctx.stat());
    }


    //Modifications
    @Override
    public Expression visitBool(FeatherweightJavaScriptParser.BoolContext ctx) {
        boolean val = Boolean.valueOf(ctx.BOOL().getText());
        return new ValueExpr(new BoolVal(val));
    }

    @Override
    public Expression visitNull(FeatherweightJavaScriptParser.NullContext ctx) {
        return new ValueExpr(new NullVal());
    }

    @Override
    public Expression visitWhile(FeatherweightJavaScriptParser.WhileContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression body = visit(ctx.block());
        return new WhileExpr(cond, body);
    }

    @Override
    public Expression visitPrint(FeatherweightJavaScriptParser.PrintContext ctx) {
        Expression exp = visit(ctx.expr());
        return new PrintExpr(exp);
    }

    @Override
    public Expression visitEmpty(FeatherweightJavaScriptParser.EmptyContext ctx) {
        return new ValueExpr(new NullVal());
    }

    @Override
    public Expression visitMulDivMod(FeatherweightJavaScriptParser.MulDivModContext ctx) {
        Expression left = visit(ctx.expr(0));
        Expression right = visit(ctx.expr(1));
        Op op = null;
        if(ctx.op.getType() == FeatherweightJavaScriptParser.MUL) {
            op = Op.MULTIPLY;
        } 
        if(ctx.op.getType() == FeatherweightJavaScriptParser.MOD) {
            op = Op.MOD;
        } 
        if(ctx.op.getType() == FeatherweightJavaScriptParser.DIV) {
            op = Op.DIVIDE;
        } 
        return new BinOpExpr(op, left, right);
    }

    @Override
    public Expression visitAddSub(FeatherweightJavaScriptParser.AddSubContext ctx) {
        Expression left = visit(ctx.expr(0));
        Expression right = visit(ctx.expr(1));
        Op op = null;
        if(ctx.op.getType() == FeatherweightJavaScriptParser.ADD) {
            op = Op.ADD;
        } 
        if(ctx.op.getType() == FeatherweightJavaScriptParser.SUB) {
            op = Op.SUBTRACT;
        } 
        return new BinOpExpr(op, left, right);
    }

    @Override
    public Expression visitComparator(FeatherweightJavaScriptParser.ComparatorContext ctx) {
        Expression left = visit(ctx.expr(0));
        Expression right = visit(ctx.expr(1));
        Op op = null;
        if(ctx.op.getType() == FeatherweightJavaScriptParser.LT) {
            op = Op.LT;
        } 
        if(ctx.op.getType() == FeatherweightJavaScriptParser.LEQ) {
            op = Op.LE;
        } 
        if(ctx.op.getType() == FeatherweightJavaScriptParser.GT) {
            op = Op.GT;
        } 
        if(ctx.op.getType() == FeatherweightJavaScriptParser.GEQ) {
            op = Op.GE;
        } 
        if(ctx.op.getType() == FeatherweightJavaScriptParser.EQ) {
            op = Op.EQ;
        } 
        return new BinOpExpr(op, left, right);
    }

    @Override
    public Expression visitFunctionDeclaration(FeatherweightJavaScriptParser.FunctionDeclarationContext ctx) {
        List<String> params = new ArrayList<String> ();
        for (int i = 0; i < ctx.params().IDENTIFIER().size(); i++) {
            params.add(String.valueOf(ctx.params().IDENTIFIER(i).getText()));
        }
        Expression body = visit(ctx.block());
        return new FunctionDeclExpr(params, body);
    }

    @Override
    public Expression visitFunctionApplication(FeatherweightJavaScriptParser.FunctionApplicationContext ctx) {
        Expression f = visit(ctx.expr());
        List<Expression> args = new ArrayList<Expression>();
        for(int i = 0; i< ctx.args().expr().size(); i++) {
           args.add(visit(ctx.args().expr(i)));
        }
        return new FunctionAppExpr(f, args);
    }

    @Override
    public Expression visitVariableDeclaration(FeatherweightJavaScriptParser.VariableDeclarationContext ctx) {
        String id = String.valueOf(ctx.IDENTIFIER().getText());
        Expression exp = visit(ctx.expr());
        return new VarDeclExpr(id, exp);
    }

    @Override
    public Expression visitIdentifier(FeatherweightJavaScriptParser.IdentifierContext ctx) {
        String id = String.valueOf(ctx.IDENTIFIER().getText());
       //System.out.println("@@@@@@@");
        return new VarExpr(id);
    }

    @Override
    public Expression visitAssignmentStatement(FeatherweightJavaScriptParser.AssignmentStatementContext ctx) {
        String id = String.valueOf(ctx.IDENTIFIER().getText());
        Expression exp = visit(ctx.expr());
        return new AssignExpr(id, exp);
    }
}
