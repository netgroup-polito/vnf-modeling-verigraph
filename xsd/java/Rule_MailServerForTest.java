package ruiNFs;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;



public class Rule_MailServerForTest extends Rule_MailServer{

	public Rule_MailServerForTest(Context ctx, Object[] objects) {
		super(ctx, objects);
	}

	public void addConstraintForTest() {
		Expr n_0 = ctx.mkConst("n_MailServer_" + n_MailServer + "_n_0", nctx.node);
		Expr p_0 = ctx.mkConst("n_MailServer_" + n_MailServer + "_p_0", nctx.packet);
		IntExpr t_0 = ctx.mkIntConst("n_MailServer_" + n_MailServer + "_t_0");
		constraints.add(ctx.mkForall(new Expr[]{p_0,n_0},ctx.mkImplies(
				(BoolExpr)nctx.send.apply(n_MailServer, n_0, p_0),
						ctx.mkEq(nctx.pf.get("emailFrom").apply(p_0), ctx.mkInt(1))),1,null,null,null,null));
	}


}

