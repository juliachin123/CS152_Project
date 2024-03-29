package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * FWJS expressions.
 */
public interface Expression {
    /**
     * Evaluate the expression in the context of the specified environment.
     */
    public Value evaluate(Environment env);
}

// NOTE: Using package access so that all implementations of Expression
// can be included in the same file.

/**
 * FWJS constants.
 */
class ValueExpr implements Expression {
    private Value val;
    public ValueExpr(Value v) {
        this.val = v;
    }
    public Value evaluate(Environment env) {
        return this.val;
    }
}

/**
 * Expressions that are a FWJS variable.
 */
class VarExpr implements Expression {
    private String varName;
    public VarExpr(String varName) {
        this.varName = varName;
    }
    public Value evaluate(Environment env) {
        return env.resolveVar(varName);
    }
}

/**
 * A print expression.
 */
class PrintExpr implements Expression {
    private Expression exp;
    public PrintExpr(Expression exp) {
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        Value v = exp.evaluate(env);
        System.out.println(v.toString());
        return v;
    }
}
/**
 * Binary operators (+, -, *, etc).
 * Currently only numbers are supported.
 */
class BinOpExpr implements Expression {
    private Op op;
    private Expression e1;
    private Expression e2;
    public BinOpExpr(Op op, Expression e1, Expression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    @SuppressWarnings("incomplete-switch")
    public Value evaluate(Environment env) {
        // ADD, SUBTRACT, MULTIPLY, DIVIDE, MOD, GT, GE, LT, LE, EQ
    	Value tmp1 = e1.evaluate(env);
    	Value tmp2 = e2.evaluate(env);
    	if(tmp1 instanceof IntVal && tmp2 instanceof IntVal) {
    		if(op == Op.ADD) {
    			return new IntVal(((IntVal) tmp1).toInt() + ((IntVal) tmp2).toInt());
    		}
    		if(op == Op.SUBTRACT) {
    			return new IntVal(((IntVal) tmp1).toInt() - ((IntVal) tmp2).toInt());
    		}
			if(op == Op.DIVIDE) {
				return new IntVal(((IntVal) tmp1).toInt() / ((IntVal) tmp2).toInt());		
			}
			if(op == Op.MULTIPLY) {
				return new IntVal(((IntVal) tmp1).toInt() * ((IntVal) tmp2).toInt());
			}
			if(op == Op.MOD) {
				return new IntVal(((IntVal) tmp1).toInt() % ((IntVal) tmp2).toInt());
			}
			if(op == Op.GT) {
    			return new BoolVal(((IntVal)tmp1).toInt() > ((IntVal)tmp2).toInt());
    		}
			if(op == Op.GE) {
    			return new BoolVal(((IntVal)tmp1).toInt() >= ((IntVal)tmp2).toInt());
    		}
			if(op == Op.LT) {
    			return new BoolVal(((IntVal)tmp1).toInt() < ((IntVal)tmp2).toInt());
    		}
			if(op == Op.LE) {
    			return new BoolVal(((IntVal)tmp1).toInt() <= ((IntVal)tmp2).toInt());
    		}
			if(op == Op.EQ) {
    			return new BoolVal(((IntVal)tmp1).toInt() == ((IntVal)tmp2).toInt());
    		}
    	}
        return null;
    }
}

/**
 * If-then-else expressions.
 * Unlike JS, if expressions return a value.
 */
class IfExpr implements Expression {
    private Expression cond;
    private Expression thn;
    private Expression els;
    public IfExpr(Expression cond, Expression thn, Expression els) {
        this.cond = cond;
        this.thn = thn;
        this.els = els;
    }
    public Value evaluate(Environment env) {
        if(new BoolVal(true).equals(cond.evaluate(env))) {
        	return thn.evaluate(env);
        }
        else if(new BoolVal(false).equals(cond.evaluate(env))){
        	return els.evaluate(env);
        }
        else {
        	return (Value) new Exception();
        }
    }
}

/**
 * While statements (treated as expressions in FWJS, unlike JS).
 */
class WhileExpr implements Expression {
    private Expression cond;
    private Expression body;
    public WhileExpr(Expression cond, Expression body) {
        this.cond = cond;
        this.body = body;
    }
    public Value evaluate(Environment env) {
    	Value tmp = cond.evaluate(env);
        if(tmp instanceof BoolVal) {
        	if(((BoolVal)tmp).toBoolean()) {
        		body.evaluate(env);
        		return evaluate(env);
        	}
        }
        return new NullVal();
    }
}

/**
 * Sequence expressions (i.e. 2 back-to-back expressions).
 */
class SeqExpr implements Expression {
    private Expression e1;
    private Expression e2;
    public SeqExpr(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Value evaluate(Environment env) {
        // YOUR CODE HERE
    	e1.evaluate(env);
    	return e2.evaluate(env);
    }
}

/**
 * Declaring a variable in the local scope.
 */
class VarDeclExpr implements Expression {
    private String varName;
    private Expression exp;
    public VarDeclExpr(String varName, Expression exp) {
        this.varName = varName;
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        // YOUR CODE HERE
    	Value tmp = exp.evaluate(env);
    	try {
    		env.createVar(this.varName, tmp);
    	}catch(RuntimeException e) {
    		return (Value)(new Exception());
    	}
    	return env.resolveVar(varName);  	
    }
}

/**
 * Updating an existing variable.
 * If the variable is not set already, it is added
 * to the global scope.
 */
class AssignExpr implements Expression {
    private String varName;
    private Expression e;
    public AssignExpr(String varName, Expression e) {
        this.varName = varName;
        this.e = e;
    }
    public Value evaluate(Environment env) {
        env.updateVar(varName, e.evaluate(env));
        return e.evaluate(env);
    }
}

/**
 * A function declaration, which evaluates to a closure.
 */
class FunctionDeclExpr implements Expression {
    private List<String> params;
    private Expression body;
    public FunctionDeclExpr(List<String> params, Expression body) {
        this.params = params;
        this.body = body;
    }
    public Value evaluate(Environment env) {
        return new ClosureVal(params, body, env);
    }
}

/**
 * Function application.
 */
class FunctionAppExpr implements Expression {
    private Expression f;
    private List<Expression> args;
    public FunctionAppExpr(Expression f, List<Expression> args) {
        this.f = f;
        this.args = args;
    }
    public Value evaluate(Environment env) {
        // YOUR CODE HERE
    	Value tmp = f.evaluate(env);
    	List<Value> argVals = new ArrayList<Value>();
    	if(tmp instanceof ClosureVal) {
    		Iterator<Expression> i = args.iterator();
    		while(i.hasNext()) {
    			argVals.add(i.next().evaluate(env));
    		}
    		return ((ClosureVal) tmp).apply(argVals);
    	}
    	else {
    		return null;
    	}
    }
}

