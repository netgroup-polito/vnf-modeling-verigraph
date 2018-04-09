package it.polito.verigraph.mcnet.netobjs2;

import java.util.List;
import java.util.ArrayList;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.DatatypeExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Sort;
import it.polito.verigraph.mcnet.components.NetContext;
import it.polito.verigraph.mcnet.components.Network;
import it.polito.verigraph.mcnet.components.NetworkObject;
import it.polito.verigraph.mcnet.components.Tuple;

public class Rule_UA extends NetworkObject {
	List<BoolExpr> constraints;
	Context ctx;
	DatatypeExpr n_UA;
	Network net;
	NetContext nctx;
	FuncDecl isInternal;

	public Rule_UA(Context ctx, Object[]... args) {
		super(ctx, args);
	}

	@Override
	protected void init(Context ctx, Object[]... args) {
		this.ctx = ctx;
		isEndHost = false;
		constraints = new ArrayList<BoolExpr>();
		z3Node = ((NetworkObject) args[0][0]).getZ3Node();
		n_UA = z3Node;
		net = (Network) args[0][1];
		nctx = (NetContext) args[0][2];
		net.saneSend(this);
		isInternal = ctx.mkFuncDecl(n_UA + "_isInternal", nctx.address, ctx.mkBoolSort());
	}

	@Override
	public DatatypeExpr getZ3Node() {
		return n_UA;
	}

	@Override
	protected void addConstraints(Solver solver) {
		BoolExpr[] constr = new BoolExpr[constraints.size()];
		solver.add(constraints.toArray(constr));
	}

	public void setInternalAddress(ArrayList<DatatypeExpr> internalAddress) {
		List<BoolExpr> constr = new ArrayList<BoolExpr>();
		Expr in_0 = ctx.mkConst(n_UA + "_internal_node", nctx.address);
		for (DatatypeExpr n : internalAddress)
			constr.add(ctx.mkEq(in_0, n));
		BoolExpr[] constrs = new BoolExpr[constr.size()];
		constraints.add(ctx.mkForall(new Expr[] { in_0 },
				ctx.mkEq(isInternal.apply(in_0), ctx.mkOr(constr.toArray(constrs))), 1, null, null, null, null));
	}

	public void installUA(Expr domain, Expr ip_caller, Expr ip_sipServer, Expr num) {
		Expr n_0 = ctx.mkConst("n_UA_" + n_UA + "_n_0", nctx.node);
		Expr p_0 = ctx.mkConst("n_UA_" + n_UA + "_p_0", nctx.packet);
		constraints.add(ctx.mkForall(new Expr[] { p_0, n_0 }, ctx.mkImplies(
				/*ctx.mkAnd(*/(BoolExpr) nctx.send.apply(n_UA, n_0, p_0), /*ctx.mkEq(nctx.pf.get("url").apply(p_0), domain)),*/
				ctx.mkAnd(ctx.mkEq(nctx.pf.get("src").apply(p_0), ip_caller),
						ctx.mkEq(nctx.pf.get("dest").apply(p_0), ip_sipServer),
						 ctx.mkEq(nctx.pf.get("url").apply(p_0), domain),
						ctx.mkEq(nctx.pf.get("body").apply(p_0), num),
						ctx.mkEq(nctx.pf.get("proto").apply(p_0), ctx.mkInt(nctx.SIP_REGISTE)))),
				1, null, null, null, null));
	}
}
