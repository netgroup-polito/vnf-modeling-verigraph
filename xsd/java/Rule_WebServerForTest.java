package ruiNFs;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;



public class Rule_WebServerForTest extends Rule_WebServer{

	public Rule_WebServerForTest(Context ctx, Object[] objects) {
		super(ctx, objects);
	}

	public void addConstraintForTest() {
		Expr n_0 = ctx.mkConst("n_WebServer" + n_WebServer + "_n_0", nctx.node);
		Expr p_0 = ctx.mkConst("n_WebServer" + n_WebServer + "_p_0", nctx.packet);
		IntExpr t_0 = ctx.mkIntConst("n_WebServer" + n_WebServer + "_t_0");
		
		constraints.add(ctx.mkForall(new Expr[]{p_0,n_0},ctx.mkImplies(
				(BoolExpr)nctx.send.apply(n_WebServer, n_0, p_0),
				ctx.mkAnd(ctx.mkEq(nctx.pf.get("url").apply(p_0), ctx.mkInt(1)),
						ctx.mkEq(nctx.pf.get("proto").apply(p_0), ctx.mkInt(nctx.HTTP_RESPONSE))
				)),1,null,null,null,null));
	}


}

