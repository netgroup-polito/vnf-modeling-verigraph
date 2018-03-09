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

public class Rule_SipServer extends NetworkObject {
	List<BoolExpr> constraints;
	Context ctx;
	DatatypeExpr n_SipServer;
	Network net;
	NetContext nctx;
	FuncDecl isInternal;
	FuncDecl matchEntry;
	FuncDecl fetchValue;
	ArrayList<ArrayList<Expr>> entries;

	public Rule_SipServer(Context ctx, Object[]... args) {
		super(ctx, args);
	}

	@Override
	protected void init(Context ctx, Object[]... args) {
		this.ctx = ctx;
		isEndHost = false;
		constraints = new ArrayList<BoolExpr>();
		z3Node = ((NetworkObject) args[0][0]).getZ3Node();
		n_SipServer = z3Node;
		net = (Network) args[0][1];
		nctx = (NetContext) args[0][2];
		net.saneSend(this);
		isInternal = ctx.mkFuncDecl(n_SipServer + "_isInternal", nctx.address, ctx.mkBoolSort());
		matchEntry = ctx.mkFuncDecl(n_SipServer + "_matchEntry", new Sort[] { ctx.mkIntSort(), nctx.address },
				ctx.mkBoolSort());
		fetchValue = ctx.mkFuncDecl(n_SipServer + "_fetchValue", new Sort[] { ctx.mkIntSort(), nctx.address },
				ctx.mkBoolSort());
		entries = new ArrayList<ArrayList<Expr>>();
	}

	@Override
	public DatatypeExpr getZ3Node() {
		return n_SipServer;
	}

	@Override
	protected void addConstraints(Solver solver) {
		BoolExpr[] constr = new BoolExpr[constraints.size()];
		solver.add(constraints.toArray(constr));
		if (entries.size() == 0)
			return;
		Expr e_0 = ctx.mkIntConst(n_SipServer + "_entry_e_0");
		Expr e_1 = ctx.mkConst(n_SipServer + "_entry_e_1", nctx.address);
		BoolExpr[] entry_map = new BoolExpr[entries.size()];
		for (int i = 0; i < entries.size(); i++) {
			entry_map[i] = ctx.mkAnd(ctx.mkEq(e_0, entries.get(i).get(0)), ctx.mkEq(e_1, entries.get(i).get(1)));
		}
		solver.add(ctx.mkForall(new Expr[] { e_0, e_1 }, ctx.mkEq(matchEntry.apply(e_0, e_1), ctx.mkOr(entry_map)), 1,
				null, null, null, null));

	}

	public void setInternalAddress(ArrayList<DatatypeExpr> internalAddress) {
		List<BoolExpr> constr = new ArrayList<BoolExpr>();
		Expr in_0 = ctx.mkConst(n_SipServer + "_internal_node", nctx.address);
		for (DatatypeExpr n : internalAddress)
			constr.add(ctx.mkEq(in_0, n));
		BoolExpr[] constrs = new BoolExpr[constr.size()];
		constraints.add(ctx.mkForall(new Expr[] { in_0 },
				ctx.mkEq(isInternal.apply(in_0), ctx.mkOr(constr.toArray(constrs))), 1, null, null, null, null));
	}

	public void addEntry(Expr expr_0, Expr expr_1) {
		if (expr_0 == null && expr_1 == null)
			return;
		ArrayList<Expr> entry = new ArrayList<Expr>();
		if (expr_0 == null)
			entry.add(ctx.mkBool(true));
		else
			entry.add(expr_0);
		if (expr_1 == null)
			entry.add(ctx.mkBool(true));
		else
			entry.add(expr_1);
		entries.add(entry);
	}

	public void installSipServer(Expr ip_sipServer) {
		Expr n_0 = ctx.mkConst("n_SipServer_" + n_SipServer + "_n_0", nctx.node);
		Expr n_1 = ctx.mkConst("n_SipServer_" + n_SipServer + "_n_1", nctx.node);
		Expr p_0 = ctx.mkConst("n_SipServer_" + n_SipServer + "_p_0", nctx.packet);
		Expr p_1 = ctx.mkConst("n_SipServer_" + n_SipServer + "_p_1", nctx.packet);
		
		IntExpr u_0 = ctx.mkIntConst("n_SipServer_"+n_SipServer+"_u_0");
		Expr a_0 = ctx.mkConst("n_SipServer_" + n_SipServer + "_a_0", nctx.address);
		Expr a_1 = ctx.mkConst("n_SipServer_" + n_SipServer + "_a_1", nctx.address);
		
		constraints
		.add(ctx.mkForall(new Expr[] { u_0, a_0 },
				ctx.mkImplies((BoolExpr)fetchValue.apply(u_0, a_0),
						(BoolExpr) matchEntry.apply(u_0,a_0)),1,null,null,null,null));
		
		constraints
				.add(ctx.mkForall(new Expr[] { p_0, n_0 },
						ctx.mkImplies((BoolExpr) nctx.send.apply(n_SipServer, n_0, p_0),
								ctx.mkExists(
										new Expr[] { p_1, n_1 },
										ctx.mkAnd((BoolExpr) nctx.recv.apply(n_1, n_SipServer, p_1),
												ctx.mkEq(nctx.pf.get("proto").apply(p_1), ctx.mkInt(nctx.SIP_INVITE)),
												ctx.mkEq(nctx.pf.get("dest").apply(p_1), ip_sipServer),
											//	(BoolExpr) matchEntry.apply(nctx.pf.get("body").apply(p_1)),
												(BoolExpr) fetchValue.apply(nctx.pf.get("body").apply(p_1), a_1),
												ctx.mkEq(nctx.pf.get("src").apply(p_0), nctx.pf.get("src").apply(p_1)),
												ctx.mkEq(nctx.pf.get("dest").apply(p_0), a_1),
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
														nctx.pf.get("options").apply(p_1)),
												ctx.mkEq(nctx.pf.get("inner_src").apply(p_0),
														nctx.pf.get("inner_src").apply(p_1)),
												ctx.mkEq(nctx.pf.get("inner_dest").apply(p_0),
														nctx.pf.get("inner_dest").apply(p_1)),
												ctx.mkEq(nctx.pf.get("encrypted").apply(p_0),
														nctx.pf.get("encrypted").apply(p_1))),
										1, null, null, null, null)),
						1, null, null, null, null));
		constraints
				.add(ctx.mkForall(
						new Expr[] { p_0,
								n_0 },
						ctx.mkImplies(
								(BoolExpr) nctx.send
										.apply(n_SipServer, n_0,
												p_0),
								ctx.mkExists(new Expr[] { p_1, n_1 },
										ctx.mkAnd((BoolExpr) nctx.recv.apply(n_1, n_SipServer, p_1), ctx.mkOr(ctx.mkAnd(
												ctx.mkEq(nctx.pf.get("proto").apply(p_1), ctx.mkInt(nctx.SIP_INVITE)),
												ctx.mkNot(ctx.mkEq(nctx.pf.get("dest").apply(p_1), ip_sipServer))),
												ctx.mkEq(nctx.pf.get("proto").apply(p_1), ctx.mkInt(nctx.SIP_OK)),
												ctx.mkEq(nctx.pf.get("proto").apply(p_1), ctx.mkInt(nctx.SIP_ENDING))),
												ctx.mkEq(nctx.pf.get("src").apply(p_0), nctx.pf.get("src").apply(p_1)),
												ctx.mkEq(nctx.pf.get("dest").apply(p_0),
														nctx.pf.get("dest").apply(p_1)),
												ctx.mkEq(
														nctx.pf.get("proto").apply(p_0),
														nctx.pf.get("proto").apply(p_1)),
												ctx.mkEq(nctx.pf.get("origin").apply(p_0),
														nctx.pf.get("origin").apply(p_1)),
												ctx.mkEq(
														nctx.pf.get("orig_body").apply(p_0),
														nctx.pf.get("orig_body").apply(p_1)),
												ctx.mkEq(nctx.pf.get("body").apply(p_0),
														nctx.pf.get("body").apply(p_1)),
												ctx.mkEq(nctx.pf.get("seq").apply(p_0), nctx.pf.get("seq").apply(p_1)),
												ctx.mkEq(nctx.pf.get("emailFrom").apply(p_0),
														nctx.pf.get("emailFrom").apply(p_1)),
												ctx.mkEq(nctx.pf.get("url").apply(p_0), nctx.pf.get("url").apply(p_1)),
												ctx.mkEq(nctx.pf.get("options").apply(p_0),
														nctx.pf.get("options").apply(p_1)),
												ctx.mkEq(nctx.pf.get("inner_src").apply(p_0),
														nctx.pf.get("inner_src").apply(p_1)),
												ctx.mkEq(nctx.pf.get("inner_dest").apply(p_0),
														nctx.pf.get("inner_dest").apply(p_1)),
												ctx.mkEq(nctx.pf.get("encrypted").apply(p_0),
														nctx.pf.get("encrypted").apply(p_1))),
										1, null, null, null, null)),
						1, null, null, null, null));
			
	}
}
