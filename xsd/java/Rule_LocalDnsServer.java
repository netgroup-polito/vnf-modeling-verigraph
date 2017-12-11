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
public class Rule_LocalDnsServer extends NetworkObject {
  List<BoolExpr> constraints;
  Context ctx;
  DatatypeExpr n_LocalDnsServer;
  Network net;
  NetContext nctx;
  FuncDecl isInternal;
  public Rule_LocalDnsServer(  Context ctx,  Object[]... args){
    super(ctx,args);
  }
  @Override protected void init(  Context ctx,  Object[]... args){
    this.ctx=ctx;
    isEndHost=false;
    constraints=new ArrayList<BoolExpr>();
    z3Node=((NetworkObject)args[0][0]).getZ3Node();
    n_LocalDnsServer=z3Node;
    net=(Network)args[0][1];
    nctx=(NetContext)args[0][2];
    net.saneSend(this);
    isInternal=ctx.mkFuncDecl(n_LocalDnsServer + "_isInternal",nctx.address,ctx.mkBoolSort());
  }
  @Override public DatatypeExpr getZ3Node(){
    return n_LocalDnsServer;
  }
  @Override protected void addConstraints(  Solver solver){
    BoolExpr[] constr=new BoolExpr[constraints.size()];
    solver.add(constraints.toArray(constr));
  }
  public void setInternalAddress(  ArrayList<DatatypeExpr> internalAddress){
    List<BoolExpr> constr=new ArrayList<BoolExpr>();
    Expr in_0=ctx.mkConst(n_LocalDnsServer + "_internal_node",nctx.address);
    for (    DatatypeExpr n : internalAddress)     constr.add(ctx.mkEq(in_0,n));
    BoolExpr[] constrs=new BoolExpr[constr.size()];
    constraints.add(ctx.mkForall(new Expr[]{in_0},ctx.mkEq(isInternal.apply(in_0),ctx.mkOr(constr.toArray(constrs))),1,null,null,null,null));
  }
  public void installLocalDnsServer(  Expr ip_GlobalBalancer){
    Expr n_0=ctx.mkConst("n_LocalDnsServer_" + n_LocalDnsServer + "_n_0",nctx.node);
    Expr n_1=ctx.mkConst("n_LocalDnsServer_" + n_LocalDnsServer + "_n_1",nctx.node);
    Expr n_2=ctx.mkConst("n_LocalDnsServer_" + n_LocalDnsServer + "_n_2",nctx.node);
    Expr p_0=ctx.mkConst("n_LocalDnsServer_" + n_LocalDnsServer + "_p_0",nctx.packet);
    Expr p_1=ctx.mkConst("n_LocalDnsServer_" + n_LocalDnsServer + "_p_1",nctx.packet);
    Expr p_2=ctx.mkConst("n_LocalDnsServer_" + n_LocalDnsServer + "_p_2",nctx.packet);
    IntExpr t_0=ctx.mkIntConst("n_LocalDnsServer_" + n_LocalDnsServer + "_t_0");
    IntExpr t_1=ctx.mkIntConst("n_LocalDnsServer_" + n_LocalDnsServer + "_t_1");
    IntExpr t_2=ctx.mkIntConst("n_LocalDnsServer_" + n_LocalDnsServer + "_t_2");
    constraints.add(ctx.mkForall(new Expr[]{t_0,p_0,n_0},ctx.mkImplies(ctx.mkAnd((BoolExpr)nctx.send.apply(n_LocalDnsServer,n_0,p_0,t_0),ctx.mkNot((BoolExpr)isInternal.apply(nctx.pf.get("dest").apply(p_0)))),ctx.mkExists(new Expr[]{t_1,p_1,n_1},ctx.mkAnd((BoolExpr)nctx.recv.apply(n_1,n_LocalDnsServer,p_1,t_1),ctx.mkLt(t_1,t_0),(BoolExpr)isInternal.apply(nctx.pf.get("src").apply(p_1)),ctx.mkEq(nctx.pf.get("proto").apply(p_1),ctx.mkInt(nctx.HTTP_REQUEST)),ctx.mkExists(new Expr[]{t_2,p_2,n_2},ctx.mkAnd((BoolExpr)nctx.recv.apply(n_2,n_LocalDnsServer,p_2,t_2),ctx.mkLt(t_2,t_1),ctx.mkNot((BoolExpr)isInternal.apply(nctx.pf.get("src").apply(p_2))),ctx.mkEq(nctx.pf.get("application_data").apply(p_1),nctx.pf.get("application_data").apply(p_2)),ctx.mkEq(nctx.pf.get("dest").apply(p_0),nctx.pf.get("oldDest").apply(p_2))),1,null,null,null,null),ctx.mkEq(nctx.pf.get("oldDest").apply(p_0),nctx.pf.get("dest").apply(p_1)),ctx.mkEq(nctx.pf.get("src").apply(p_0),nctx.pf.get("src").apply(p_1)),ctx.mkEq(nctx.src_port.apply(p_0),nctx.src_port.apply(p_1)),ctx.mkEq(nctx.dest_port.apply(p_0),nctx.dest_port.apply(p_1)),ctx.mkEq(nctx.pf.get("transport_protocol").apply(p_0),nctx.pf.get("transport_protocol").apply(p_1)),ctx.mkEq(nctx.pf.get("proto").apply(p_0),nctx.pf.get("proto").apply(p_1)),ctx.mkEq(nctx.pf.get("application_data").apply(p_0),nctx.pf.get("application_data").apply(p_1)),ctx.mkEq(nctx.pf.get("oldSrc").apply(p_0),nctx.pf.get("oldSrc").apply(p_1))),1,null,null,null,null)),1,null,null,null,null));
    constraints.add(ctx.mkForall(new Expr[]{t_0,p_0,n_0},ctx.mkImplies(ctx.mkAnd((BoolExpr)nctx.send.apply(n_LocalDnsServer,n_0,p_0,t_0),ctx.mkNot((BoolExpr)isInternal.apply(nctx.pf.get("dest").apply(p_0)))),ctx.mkExists(new Expr[]{t_1,p_1,n_1},ctx.mkAnd((BoolExpr)nctx.recv.apply(n_1,n_LocalDnsServer,p_1,t_1),ctx.mkLt(t_1,t_0),(BoolExpr)isInternal.apply(nctx.pf.get("src").apply(p_1)),ctx.mkEq(nctx.pf.get("proto").apply(p_1),ctx.mkInt(nctx.HTTP_REQUEST)),ctx.mkNot(ctx.mkExists(new Expr[]{t_2,p_2,n_2},ctx.mkAnd((BoolExpr)nctx.recv.apply(n_2,n_LocalDnsServer,p_2,t_2),ctx.mkLt(t_2,t_1),ctx.mkNot((BoolExpr)isInternal.apply(nctx.pf.get("src").apply(p_2))),ctx.mkEq(nctx.pf.get("application_data").apply(p_1),nctx.pf.get("application_data").apply(p_2))),1,null,null,null,null)),ctx.mkEq(nctx.pf.get("oldDest").apply(p_0),nctx.pf.get("dest").apply(p_1)),ctx.mkEq(nctx.pf.get("dest").apply(p_0),ip_GlobalBalancer),ctx.mkEq(nctx.pf.get("proto").apply(p_0),ctx.mkInt(nctx.DNS_REQUEST)),ctx.mkEq(nctx.pf.get("src").apply(p_0),nctx.pf.get("src").apply(p_1)),ctx.mkEq(nctx.src_port.apply(p_0),nctx.src_port.apply(p_1)),ctx.mkEq(nctx.dest_port.apply(p_0),nctx.dest_port.apply(p_1)),ctx.mkEq(nctx.pf.get("transport_protocol").apply(p_0),nctx.pf.get("transport_protocol").apply(p_1)),ctx.mkEq(nctx.pf.get("application_data").apply(p_0),nctx.pf.get("application_data").apply(p_1)),ctx.mkEq(nctx.pf.get("oldSrc").apply(p_0),nctx.pf.get("oldSrc").apply(p_1))),1,null,null,null,null)),1,null,null,null,null));
    constraints.add(ctx.mkForall(new Expr[]{t_0,p_0,n_0},ctx.mkImplies(ctx.mkAnd((BoolExpr)nctx.send.apply(n_LocalDnsServer,n_0,p_0,t_0),(BoolExpr)isInternal.apply(nctx.pf.get("dest").apply(p_0))),ctx.mkExists(new Expr[]{t_1,p_1,n_1},ctx.mkAnd((BoolExpr)nctx.recv.apply(n_1,n_LocalDnsServer,p_1,t_1),ctx.mkLt(t_1,t_0),ctx.mkNot((BoolExpr)isInternal.apply(nctx.pf.get("src").apply(p_1))),ctx.mkEq(nctx.pf.get("proto").apply(p_1),ctx.mkInt(nctx.HTTP_RESPONSE)),ctx.mkEq(nctx.pf.get("src").apply(p_0),nctx.pf.get("src").apply(p_1)),ctx.mkEq(nctx.pf.get("dest").apply(p_0),nctx.pf.get("dest").apply(p_1)),ctx.mkEq(nctx.src_port.apply(p_0),nctx.src_port.apply(p_1)),ctx.mkEq(nctx.dest_port.apply(p_0),nctx.dest_port.apply(p_1)),ctx.mkEq(nctx.pf.get("transport_protocol").apply(p_0),nctx.pf.get("transport_protocol").apply(p_1)),ctx.mkEq(nctx.pf.get("proto").apply(p_0),nctx.pf.get("proto").apply(p_1)),ctx.mkEq(nctx.pf.get("application_data").apply(p_0),nctx.pf.get("application_data").apply(p_1)),ctx.mkEq(nctx.pf.get("oldSrc").apply(p_0),nctx.pf.get("oldSrc").apply(p_1)),ctx.mkEq(nctx.pf.get("oldDest").apply(p_0),nctx.pf.get("oldDest").apply(p_1))),1,null,null,null,null)),1,null,null,null,null));
  }
}
