package org.exist.xquery;

import java.util.List;

import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.FunctionReference;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.Type;

public class DynamicFunctionCall extends AbstractExpression {

    private Expression functionExpr;
    private List<Expression> arguments;
    private boolean isPartial = false;
    
    private AnalyzeContextInfo cachedContextInfo;

    public DynamicFunctionCall(XQueryContext context, Expression fun, List<Expression> args, boolean partial) {
        super(context);
        this.functionExpr = fun;
        this.arguments = args;
        this.isPartial = partial;
    }

    @Override
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
        cachedContextInfo = new AnalyzeContextInfo(contextInfo);
    }

    @Override
    public void dump(ExpressionDumper dumper) {
        functionExpr.dump(dumper);
        dumper.display('(');
        for (Expression arg : arguments) {
            arg.dump(dumper);
        }
        dumper.display(')');
    }

    @Override
    public Sequence eval(Sequence contextSequence, Item contextItem)
            throws XPathException {
        Sequence funcSeq = functionExpr.eval(contextSequence, contextItem);
        if (funcSeq.getCardinality() != Cardinality.EXACTLY_ONE)
            throw new XPathException(this, ErrorCodes.XPTY0004,
                "Expected exactly one item for the function to be called");
        Item item0 = funcSeq.itemAt(0);
        if(item0.getType() != Type.FUNCTION_REFERENCE)
            throw new XPathException(this, ErrorCodes.XPTY0004,
                "Type error: expected function, got " + Type.getTypeName(item0.getType()));
        FunctionReference ref = (FunctionReference)item0;
        // if the call is a partial application, create a new function
        if (isPartial) {
        	try {
	        	FunctionCall call = ref.getCall();
	        	call.setArguments(arguments);
	        	PartialFunctionApplication partialApp = new PartialFunctionApplication(context, call);
	        	return partialApp.eval(contextSequence, contextItem);
        	} catch (XPathException e) {
				e.setLocation(line, column, getSource());
				throw e;
        	}
        } else {
	        ref.setArguments(arguments);
	        ref.analyze(cachedContextInfo);
	        // Evaluate the function
	        return ref.eval(contextSequence);
        }
    }

    @Override
    public int returnsType() {
        return Type.ITEM; // Unknown until the reference is resolved
    }

    @Override
    public void resetState(boolean postOptimization) {
        super.resetState(postOptimization);
    }
}