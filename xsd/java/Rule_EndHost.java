package mcnet.netobjs.generated;
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
import mcnet.components.NetContext;
import mcnet.components.Network;
import mcnet.components.NetworkObject;
import mcnet.components.Tuple;
public class Rule_EndHost extends NetworkObject {
  List<BoolExpr> constraints;
  Context ctx;
  DatatypeExpr n_EndHost;
  Network net;
  NetContext nctx;
  FuncDecl isInternal;
  public Rule_EndHost(  Context ctx,  Object[]... args){
    super(ctx,args);
  }
  @Override protected void init(  Context ctx,  Object[]... args){
    this.ctx=ctx;
    isEndHost=false;
    constraints=new ArrayList<BoolExpr>();
    z3Node=((NetworkObject)args[0][0]).getZ3Node();
    n_EndHost=z3Node;
    net=(Network)args[0][1];
    nctx=(NetContext)args[0][2];
    net.saneSend(this);
    isInternal=ctx.mkFuncDecl(n_EndHost + "_isInternal",nctx.address,ctx.mkBoolSort());
  }
  @Override public DatatypeExpr getZ3Node(){
    return n_EndHost;
  }
  @Override protected void addConstraints(  Solver solver){
    BoolExpr[] constr=new BoolExpr[constraints.size()];
    solver.add(constraints.toArray(constr));
  }
  public void setInternalAddress(  ArrayList<DatatypeExpr> internalAddress){
    List<BoolExpr> constr=new ArrayList<BoolExpr>();
    Expr in_0=ctx.mkConst(n_EndHost + "_internal_node",nctx.address);
    for (    DatatypeExpr n : internalAddress)     constr.add(ctx.mkEq(in_0,n));
    BoolExpr[] constrs=new BoolExpr[constr.size()];
    constraints.add(ctx.mkForall(new Expr[]{in_0},ctx.mkEq(isInternal.apply(in_0),ctx.mkOr(constr.toArray(constrs))),1,null,null,null,null));
  }
  public void installEndHost(  Expr ip_EndHost,  Expr value_0,  Expr ip_WebServer){
    Expr n_0=ctx.mkConst("n_EndHost_" + n_EndHost + "_n_0",nctx.node);
    Expr p_0=ctx.mkConst("n_EndHost_" + n_EndHost + "_p_0",nctx.packet);
    IntExpr t_0=ctx.mkIntConst("n_EndHost_" + n_EndHost + "_t_0");
    constraints.add(ctx.mkForall(new Expr[]{t_0,p_0,n_0},
    		ctx.mkImplies((BoolExpr)nctx.send.apply(n_EndHost,n_0,p_0,t_0),
    				ctx.mkAnd(ctx.mkEq(nctx.pf.get("src").apply(p_0),ip_EndHost),
    						ctx.mkEq(nctx.src_port.apply(p_0),value_0),ctx.mkEq(nctx.pf.get("dest").apply(p_0),ip_WebServer),ctx.mkEq(nctx.pf.get("oldDest").apply(p_0),ip_WebServer),ctx.mkEq(nctx.pf.get("oldSrc").apply(p_0),ip_EndHost),ctx.mkEq(nctx.dest_port.apply(p_0),ctx.mkInt(nctx.HTTP_PORT_80)),ctx.mkEq(nctx.pf.get("proto").apply(p_0),ctx.mkInt(nctx.HTTP_REQUEST)),ctx.mkEq(nctx.pf.get("application_data").apply(p_0),ctx.mkInt(nctx.REQUESTED_URL)))),1,null,null,null,null));
  }
}
