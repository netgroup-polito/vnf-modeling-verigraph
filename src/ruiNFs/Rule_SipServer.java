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
	}

	@Override
	public DatatypeExpr getZ3Node() {
		return n_SipServer;
	}

	@Override
	protected void addConstraints(Solver solver) {
		BoolExpr[] constr = new BoolExpr[constraints.size()];
		solver.add(constraints.toArray(constr));
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

	public void installSipServer(Expr ip_sipServer, Expr domain, Expr ip_dns) {
		Expr n_0 = ctx.mkConst("n_SipServer_" + n_SipServer + "_n_0", nctx.node);
		Expr n_1 = ctx.mkConst("n_SipServer_" + n_SipServer + "_n_1", nctx.node);
		Expr n_2 = ctx.mkConst("n_SipServer_" + n_SipServer + "_n_2", nctx.node);
		Expr p_0 = ctx.mkConst("n_SipServer_" + n_SipServer + "_p_0", nctx.packet);
		Expr p_1 = ctx.mkConst("n_SipServer_" + n_SipServer + "_p_1", nctx.packet);
		Expr p_2 = ctx.mkConst("n_SipServer_" + n_SipServer + "_p_2", nctx.packet);
		Expr p_3 = ctx.mkConst("n_SipServer_" + n_SipServer + "_p_3", nctx.packet);
		constraints
				.add(ctx.mkForall(
						new Expr[] { p_0, n_0 }, ctx
								.mkImplies(
										ctx.mkAnd(
												(BoolExpr) nctx.send.apply(n_SipServer, n_0, p_0),
												ctx.mkEq(nctx.pf.get("proto").apply(p_0),
														ctx.mkInt(nctx.SIP_REGISTE_OK))),
										ctx.mkExists(
												new Expr[] { p_1, n_1 }, ctx
														.mkAnd((BoolExpr) nctx.recv.apply(n_1, n_SipServer, p_1),
																ctx.mkEq(nctx.pf.get("proto").apply(p_1),
																		ctx.mkInt(nctx.SIP_REGISTE)),
																ctx.mkEq(nctx.pf.get("dest").apply(p_1), ip_sipServer),
																ctx.mkEq(nctx.pf.get("src").apply(p_0),
																		nctx.pf.get("dest").apply(
																				p_1)),
																ctx.mkEq(nctx.pf.get("dest").apply(p_0),
																		nctx.pf.get("src").apply(p_1)),
																ctx.mkEq(nctx.pf.get("origin").apply(p_0),
																		nctx.pf.get("origin").apply(p_1)),
																ctx.mkEq(nctx.pf.get("orig_body").apply(p_0),
																		nctx.pf.get("orig_body").apply(p_1)),
																ctx.mkEq(nctx.pf.get("body").apply(p_0),
																		nctx.pf.get("body").apply(p_1)),
																ctx.mkEq(nctx.pf.get("seq").apply(p_0),
																		nctx.pf.get("seq").apply(p_1)),
																ctx.mkEq(nctx.pf.get("emailFrom").apply(p_0),
																		nctx.pf.get("emailFrom").apply(p_1)),
																ctx.mkEq(nctx.pf.get("url").apply(p_0),
																		nctx.pf.get("url").apply(p_1)),
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
								ctx.mkAnd((BoolExpr) nctx.send.apply(n_SipServer, n_0, p_0),
										ctx.mkEq(nctx.pf.get("url").apply(p_0), domain)),
								ctx.mkExists(new Expr[] { p_1, n_1 },
										ctx.mkAnd((BoolExpr) nctx.recv.apply(n_1, n_SipServer, p_1),
												ctx.mkEq(nctx.pf.get("proto").apply(p_1), ctx.mkInt(nctx.SIP_INVITE)),
												ctx.mkEq(nctx.pf.get("dest").apply(p_1), ip_sipServer),
												ctx.mkEq(nctx.pf.get("url").apply(p_1), domain),
												ctx.mkExists(
														new Expr[] { p_2, n_2 },
														ctx.mkAnd((BoolExpr) nctx.recv.apply(n_2, n_SipServer, p_2),
																ctx.mkEq(nctx.pf.get("dest").apply(p_0),
																		nctx.pf.get("src").apply(p_2))),
														1, null, null, null, null),
												ctx.mkEq(nctx.pf.get("src").apply(p_0), nctx.pf.get("src").apply(p_1)),
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
		constraints.add(ctx.mkForall(
				new Expr[] { p_0, n_0 }, ctx
						.mkImplies(
								ctx.mkAnd((BoolExpr) nctx.send.apply(n_SipServer, n_0, p_0),
										ctx.mkNot(ctx.mkEq(nctx.pf.get("url").apply(p_0), domain))),
								ctx.mkExists(
										new Expr[] { p_1,
												n_1 },
										ctx.mkAnd((BoolExpr) nctx.recv.apply(n_1, n_SipServer, p_1),
												ctx.mkEq(nctx.pf.get("proto").apply(p_1), ctx.mkInt(nctx.SIP_INVITE)),
												ctx.mkEq(nctx.pf.get("dest").apply(p_1), ip_sipServer),
												ctx.mkNot(ctx.mkEq(nctx.pf.get("url").apply(p_1), domain)),
												ctx.mkExists(new Expr[] { p_2, n_2 },
														ctx.mkAnd((BoolExpr) nctx.send.apply(n_SipServer, n_2, p_2),
																ctx.mkEq(nctx.pf.get("src").apply(p_2), ip_sipServer),
																ctx.mkEq(nctx.pf.get("dest").apply(p_2), ip_dns),
																ctx.mkEq(nctx.pf.get("proto").apply(p_2),
																		ctx.mkInt(nctx.DNS_REQUEST)),
																ctx.mkEq(nctx.pf.get("url").apply(p_2),
																		nctx.pf.get("url").apply(p_1))),
														1, null, null, null, null),
												ctx.mkExists(new Expr[] { p_3 },
														ctx.mkAnd((BoolExpr) nctx.recv.apply(n_2, n_SipServer, p_3),
																ctx.mkEq(nctx.pf.get("src").apply(p_3),
																		nctx.pf.get("dest").apply(p_2)),
																ctx.mkEq(nctx.pf.get("dest").apply(p_3),
																		nctx.pf.get("src").apply(p_2)),
																ctx.mkEq(nctx.pf.get("proto").apply(p_3),
																		ctx.mkInt(nctx.DNS_RESPONSE)),
																ctx.mkEq(nctx.pf.get("url").apply(p_3),
																		nctx.pf.get("url").apply(p_2)),
																ctx.mkNot(ctx.mkEq(nctx.pf.get("inner_dest").apply(p_3),
																		nctx.am.get("null"))),
																ctx.mkEq(nctx.pf.get("dest").apply(p_0),
																		nctx.pf.get("inner_dest").apply(p_3))),
														1, null, null, null, null),
												ctx.mkEq(nctx.pf.get("url").apply(p_0), nctx.pf.get("url").apply(p_1)),
												ctx.mkEq(nctx.pf.get("src").apply(p_0), nctx.pf.get("src").apply(p_1)),
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
						new Expr[] { p_0, n_0 }, ctx
								.mkImplies(
										ctx.mkAnd(
												(BoolExpr) nctx.send.apply(n_SipServer, n_0, p_0),
												ctx.mkEq(nctx.pf.get("proto").apply(p_0),
														ctx.mkInt(nctx.SIP_INVITE_OK))),
										ctx.mkExists(new Expr[] { p_1, n_1 },
												ctx.mkAnd((BoolExpr) nctx.recv.apply(n_1, n_SipServer, p_1),
														ctx.mkEq(nctx.pf.get("proto").apply(p_1),
																ctx.mkInt(nctx.SIP_INVITE_OK)),
														ctx.mkEq(nctx.pf.get("src").apply(p_0),
																nctx.pf.get("src").apply(p_1)),
														ctx.mkEq(nctx.pf.get("dest").apply(p_0),
																nctx.pf.get("dest").apply(p_1)),
														ctx.mkEq(nctx.pf.get("origin").apply(p_0),
																nctx.pf.get("origin").apply(p_1)),
														ctx.mkEq(nctx.pf.get("orig_body").apply(p_0),
																nctx.pf.get("orig_body").apply(p_1)),
														ctx.mkEq(nctx.pf.get("body").apply(p_0),
																nctx.pf.get("body").apply(p_1)),
														ctx.mkEq(nctx.pf.get("seq").apply(p_0),
																nctx.pf.get("seq").apply(p_1)),
														ctx.mkEq(nctx.pf.get("emailFrom").apply(p_0),
																nctx.pf.get("emailFrom").apply(p_1)),
														ctx.mkEq(nctx.pf.get("url").apply(p_0),
																nctx.pf.get("url").apply(p_1)),
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
		constraints.add(ctx.mkForall(new Expr[] { p_0, n_0 }, ctx.mkImplies(
				ctx.mkAnd((BoolExpr) nctx.send.apply(n_SipServer, n_0, p_0),
						ctx.mkEq(nctx.pf.get("proto").apply(p_0), ctx.mkInt(nctx.SIP_END))),
				ctx.mkExists(new Expr[] { p_1, n_1 },
						ctx.mkAnd((BoolExpr) nctx.recv.apply(n_1, n_SipServer, p_1),
								ctx.mkEq(nctx.pf.get("proto").apply(p_1), ctx.mkInt(nctx.SIP_END)),
								ctx.mkEq(nctx.pf.get("src").apply(p_0), nctx.pf.get("src").apply(p_1)),
								ctx.mkEq(nctx.pf.get("dest").apply(p_0), nctx.pf.get("dest").apply(p_1)),
								ctx.mkEq(nctx.pf.get("origin").apply(p_0), nctx.pf.get("origin").apply(p_1)),
								ctx.mkEq(nctx.pf.get("orig_body").apply(p_0), nctx.pf.get("orig_body").apply(p_1)),
								ctx.mkEq(nctx.pf.get("body").apply(p_0), nctx.pf.get("body").apply(p_1)),
								ctx.mkEq(nctx.pf.get("seq").apply(p_0), nctx.pf.get("seq").apply(p_1)),
								ctx.mkEq(nctx.pf.get("emailFrom").apply(p_0), nctx.pf.get("emailFrom").apply(p_1)),
								ctx.mkEq(nctx.pf.get("url").apply(p_0), nctx.pf.get("url").apply(p_1)),
								ctx.mkEq(nctx.pf.get("options").apply(p_0), nctx.pf.get("options").apply(p_1)),
								ctx.mkEq(nctx.pf.get("inner_src").apply(p_0), nctx.pf.get("inner_src").apply(p_1)),
								ctx.mkEq(nctx.pf.get("inner_dest").apply(p_0), nctx.pf.get("inner_dest").apply(p_1)),
								ctx.mkEq(nctx.pf.get("encrypted").apply(p_0), nctx.pf.get("encrypted").apply(p_1))),
						1, null, null, null, null)),
				1, null, null, null, null));
	}
}
