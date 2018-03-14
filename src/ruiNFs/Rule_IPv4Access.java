package ruiNFs;

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

public class Rule_IPv4Access extends NetworkObject {
	List<BoolExpr> constraints;
	Context ctx;
	DatatypeExpr n_IPv4Access;
	Network net;
	NetContext nctx;
	FuncDecl isInternal;
	FuncDecl matchEntry;
	ArrayList<ArrayList<Expr>> entries;

	public Rule_IPv4Access(Context ctx, Object[]... args) {
		super(ctx, args);
	}

	@Override
	protected void init(Context ctx, Object[]... args) {
		this.ctx = ctx;
		isEndHost = false;
		constraints = new ArrayList<BoolExpr>();
		z3Node = ((NetworkObject) args[0][0]).getZ3Node();
		n_IPv4Access = z3Node;
		net = (Network) args[0][1];
		nctx = (NetContext) args[0][2];
		net.saneSend(this);
		isInternal = ctx.mkFuncDecl(n_IPv4Access + "_isInternal", nctx.address, ctx.mkBoolSort());
		matchEntry = ctx.mkFuncDecl(n_IPv4Access + "_matchEntry", new Sort[] { nctx.address }, ctx.mkBoolSort());
		entries = new ArrayList<ArrayList<Expr>>();
	}

	@Override
	public DatatypeExpr getZ3Node() {
		return n_IPv4Access;
	}

	@Override
	protected void addConstraints(Solver solver) {
		BoolExpr[] constr = new BoolExpr[constraints.size()];
		solver.add(constraints.toArray(constr));
		if (entries.size() == 0)
			return;
		Expr e_0 = ctx.mkConst(n_IPv4Access + "_entry_e_0", nctx.address);
		BoolExpr[] entry_map = new BoolExpr[entries.size()];
		for (int i = 0; i < entries.size(); i++) {
			entry_map[i] = ctx.mkAnd(ctx.mkEq(e_0, entries.get(i).get(0)));
		}
		solver.add(ctx.mkForall(new Expr[] { e_0 }, ctx.mkEq(matchEntry.apply(e_0), ctx.mkOr(entry_map)), 1, null, null,
				null, null));
	}

	public void setInternalAddress(ArrayList<DatatypeExpr> internalAddress) {
		List<BoolExpr> constr = new ArrayList<BoolExpr>();
		Expr in_0 = ctx.mkConst(n_IPv4Access + "_internal_node", nctx.address);
		for (DatatypeExpr n : internalAddress)
			constr.add(ctx.mkEq(in_0, n));
		BoolExpr[] constrs = new BoolExpr[constr.size()];
		constraints.add(ctx.mkForall(new Expr[] { in_0 },
				ctx.mkEq(isInternal.apply(in_0), ctx.mkOr(constr.toArray(constrs))), 1, null, null, null, null));
	}

	public void addEntry(Expr expr_0) {
		if (expr_0 == null)
			return;
		ArrayList<Expr> entry = new ArrayList<Expr>();
		entry.add(expr_0);
		entries.add(entry);
	}

	public void installIPv4Access(Expr accessIp, Expr exitIp) {
		Expr n_0 = ctx.mkConst("n_IPv4Access_" + n_IPv4Access + "_n_0", nctx.node);
		Expr n_1 = ctx.mkConst("n_IPv4Access_" + n_IPv4Access + "_n_1", nctx.node);
		Expr p_0 = ctx.mkConst("n_IPv4Access_" + n_IPv4Access + "_p_0", nctx.packet);
		Expr p_1 = ctx.mkConst("n_IPv4Access_" + n_IPv4Access + "_p_1", nctx.packet);
		constraints
				.add(ctx.mkForall(
						new Expr[] { p_0, n_0 }, ctx
								.mkImplies(
										ctx.mkAnd((BoolExpr) nctx.send.apply(n_IPv4Access, n_0, p_0),
												ctx.mkNot(
														ctx.mkEq(nctx.pf.get("inner_src").apply(p_0),
																nctx.am.get("null")))),
										ctx.mkExists(new Expr[] { p_1, n_1 }, ctx.mkAnd(
												(BoolExpr) nctx.recv.apply(n_1, n_IPv4Access, p_1),
												ctx.mkNot((BoolExpr) nctx.pf.get("encrypted").apply(p_1)),
												ctx.mkEq(nctx.pf.get("inner_src").apply(p_1), nctx.am.get("null")),
												ctx.mkEq(nctx.pf.get("inner_dest").apply(p_1), nctx.am.get("null")),
												ctx.mkEq(nctx.pf.get("src").apply(p_0), accessIp),
												ctx.mkEq(nctx.pf.get("dest").apply(p_0), exitIp),
												ctx.mkEq(nctx.pf.get("inner_dest").apply(p_0),
														nctx.pf.get("dest").apply(p_1)),
												ctx.mkEq(nctx.pf.get("inner_src").apply(p_0),
														nctx.pf.get("src").apply(p_1)),
												(BoolExpr) nctx.pf.get("encrypted").apply(p_0),
												(BoolExpr) isInternal.apply(nctx.pf.get("inner_src").apply(p_0)),
												ctx.mkEq(nctx.pf.get("proto").apply(p_0),
														nctx.pf.get("proto").apply(p_1)),
												ctx.mkEq(nctx.pf.get("origin").apply(p_0),
														nctx.pf.get("origin").apply(p_1)),
												ctx.mkEq(nctx.pf.get("orig_body").apply(p_0),
														nctx.pf.get("orig_body").apply(p_1)),
												ctx.mkEq(nctx.pf.get("body").apply(p_0),
														nctx.pf.get("body").apply(p_1)),
												ctx.mkEq(nctx.pf.get("seq").apply(p_0), nctx.pf.get("seq").apply(p_1)),
												ctx.mkEq(nctx.pf.get("emailFrom").apply(p_0),
														nctx.pf.get("emailFrom").apply(p_1)),
												ctx.mkEq(nctx.pf.get("url").apply(p_0), nctx.pf.get("url").apply(p_1)),
												ctx.mkEq(nctx.pf.get("options").apply(p_0),
														nctx.pf.get("options").apply(p_1))),
												1, null, null, null, null)),
						1, null, null, null, null));
		constraints.add(ctx.mkForall(new Expr[] { p_0, n_0 }, ctx.mkImplies(
				ctx.mkAnd((BoolExpr) nctx.send.apply(n_IPv4Access, n_0, p_0),
						ctx.mkEq(nctx.pf.get("inner_src").apply(p_0), nctx.am.get("null"))),
				ctx.mkExists(new Expr[] { p_1, n_1 },
						ctx.mkAnd((BoolExpr) nctx.recv.apply(n_1, n_IPv4Access, p_1),
								ctx.mkEq(nctx.pf.get("src").apply(p_1), exitIp),
								ctx.mkEq(nctx.pf.get("dest").apply(p_1), accessIp),
								(BoolExpr) nctx.pf.get("encrypted").apply(p_1),
								ctx.mkEq(nctx.pf.get("dest").apply(p_0), nctx.pf.get("inner_dest").apply(p_1)),
								ctx.mkEq(nctx.pf.get("src").apply(p_0), nctx.pf.get("inner_src").apply(p_1)),
								ctx.mkEq(nctx.pf.get("inner_dest").apply(p_0), nctx.am.get("null")),
								ctx.mkNot((BoolExpr) nctx.pf.get("encrypted").apply(p_0)),
								(BoolExpr) isInternal.apply(nctx.pf.get("dest").apply(p_0)),
								ctx.mkEq(nctx.pf.get("proto").apply(p_0), nctx.pf.get("proto").apply(p_1)),
								ctx.mkEq(nctx.pf.get("origin").apply(p_0), nctx.pf.get("origin").apply(p_1)),
								ctx.mkEq(nctx.pf.get("orig_body").apply(p_0), nctx.pf.get("orig_body").apply(p_1)),
								ctx.mkEq(nctx.pf.get("body").apply(p_0), nctx.pf.get("body").apply(p_1)),
								ctx.mkEq(nctx.pf.get("seq").apply(p_0), nctx.pf.get("seq").apply(p_1)),
								ctx.mkEq(nctx.pf.get("emailFrom").apply(p_0), nctx.pf.get("emailFrom").apply(p_1)),
								ctx.mkEq(nctx.pf.get("url").apply(p_0), nctx.pf.get("url").apply(p_1)),
								ctx.mkEq(nctx.pf.get("options").apply(p_0), nctx.pf.get("options").apply(p_1))),
						1, null, null, null, null)),
				1, null, null, null, null));
	}
}
