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
public class Rule_WebClient extends NetworkObject {
  List<BoolExpr> constraints;
  Context ctx;
  DatatypeExpr n_WebClient;
  Network net;
  NetContext nctx;
  FuncDecl isInternal;
  public Rule_WebClient(  Context ctx,  Object[]... args){
    super(ctx,args);
  }
  @Override protected void init(  Context ctx,  Object[]... args){
    this.ctx=ctx;
    isEndHost=false;
    constraints=new ArrayList<BoolExpr>();
    z3Node=((NetworkObject)args[0][0]).getZ3Node();
    n_WebClient=z3Node;
    net=(Network)args[0][1];
    nctx=(NetContext)args[0][2];
    net.saneSend(this);
    isInternal=ctx.mkFuncDecl(n_WebClient + "_isInternal",nctx.address,ctx.mkBoolSort());
  }
  @Override public DatatypeExpr getZ3Node(){
    return n_WebClient;
  }
  @Override protected void addConstraints(  Solver solver){
    BoolExpr[] constr=new BoolExpr[constraints.size()];
    solver.add(constraints.toArray(constr));
  }
  public void setInternalAddress(  ArrayList<DatatypeExpr> internalAddress){
    List<BoolExpr> constr=new ArrayList<BoolExpr>();
    Expr in_0=ctx.mkConst(n_WebClient + "_internal_node",nctx.address);
    for (    DatatypeExpr n : internalAddress)     constr.add(ctx.mkEq(in_0,n));
    BoolExpr[] constrs=new BoolExpr[constr.size()];
    constraints.add(ctx.mkForall(new Expr[]{in_0},ctx.mkEq(isInternal.apply(in_0),ctx.mkOr(constr.toArray(constrs))),1,null,null,null,null));
  }
  public void installWebClient(  Expr ip_EndHost,  Expr ip_WebServer,  Expr REQUESTED_URL){
    Expr n_0=ctx.mkConst("n_WebClient_" + n_WebClient + "_n_0",nctx.node);
    Expr p_0=ctx.mkConst("n_WebClient_" + n_WebClient + "_p_0",nctx.packet);
    constraints.add(ctx.mkForall(new Expr[]{p_0,n_0},ctx.mkImplies((BoolExpr)nctx.send.apply(n_WebClient,n_0,p_0),ctx.mkAnd(ctx.mkEq(nctx.pf.get("src").apply(p_0),ip_EndHost),ctx.mkEq(nctx.pf.get("dest").apply(p_0),ip_WebServer),ctx.mkEq(nctx.pf.get("proto").apply(p_0),ctx.mkInt(nctx.HTTP_REQUEST)),ctx.mkEq(nctx.pf.get("url").apply(p_0),REQUESTED_URL))),1,null,null,null,null));
  }
}
