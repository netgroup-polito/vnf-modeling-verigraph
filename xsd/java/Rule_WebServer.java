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
import it.polito.verigraph.mcnet.components.NetContext;
import it.polito.verigraph.mcnet.components.Network;
import it.polito.verigraph.mcnet.components.NetworkObject;
import it.polito.verigraph.mcnet.components.Tuple;
public class Rule_WebServer extends NetworkObject {
  List<BoolExpr> constraints;
  Context ctx;
  DatatypeExpr n_WebServer;
  Network net;
  NetContext nctx;
  FuncDecl isInternal;
  FuncDecl matchEntry;
  ArrayList<ArrayList<Expr>> entries;
  public Rule_WebServer(  Context ctx,  Object[]... args){
    super(ctx,args);
  }
  @Override protected void init(  Context ctx,  Object[]... args){
    this.ctx=ctx;
    isEndHost=false;
    constraints=new ArrayList<BoolExpr>();
    z3Node=((NetworkObject)args[0][0]).getZ3Node();
    n_WebServer=z3Node;
    net=(Network)args[0][1];
    nctx=(NetContext)args[0][2];
    net.saneSend(this);
    isInternal=ctx.mkFuncDecl(n_WebServer + "_isInternal",nctx.address,ctx.mkBoolSort());
    matchEntry=ctx.mkFuncDecl(n_WebServer + "_matchEntry",new Sort[]{ctx.mkIntSort()},ctx.mkBoolSort());
    entries=new ArrayList<ArrayList<Expr>>();
  }
  @Override public DatatypeExpr getZ3Node(){
    return n_WebServer;
  }
  @Override protected void addConstraints(  Solver solver){
    BoolExpr[] constr=new BoolExpr[constraints.size()];
    solver.add(constraints.toArray(constr));
    if (entries.size() == 0)     return;
    Expr e_0=ctx.mkIntConst(n_WebServer + "_entry_e_0");
    BoolExpr[] entry_map=new BoolExpr[entries.size()];
    for (int i=0; i < entries.size(); i++) {
      entry_map[i]=ctx.mkAnd(ctx.mkEq(e_0,entries.get(i).get(0)));
    }
    solver.add(ctx.mkForall(new Expr[]{e_0},ctx.mkEq(matchEntry.apply(e_0),ctx.mkOr(entry_map)),1,null,null,null,null));
  }
  public void setInternalAddress(  ArrayList<DatatypeExpr> internalAddress){
    List<BoolExpr> constr=new ArrayList<BoolExpr>();
    Expr in_0=ctx.mkConst(n_WebServer + "_internal_node",nctx.address);
    for (    DatatypeExpr n : internalAddress)     constr.add(ctx.mkEq(in_0,n));
    BoolExpr[] constrs=new BoolExpr[constr.size()];
    constraints.add(ctx.mkForall(new Expr[]{in_0},ctx.mkEq(isInternal.apply(in_0),ctx.mkOr(constr.toArray(constrs))),1,null,null,null,null));
  }
  public void addEntry(  Expr expr_0){
    if (expr_0 == null)     return;
    ArrayList<Expr> entry=new ArrayList<Expr>();
    entry.add(expr_0);
    entries.add(entry);
  }
  public void installWebServer(){
    Expr n_0=ctx.mkConst("n_WebServer_" + n_WebServer + "_n_0",nctx.node);
    Expr n_1=ctx.mkConst("n_WebServer_" + n_WebServer + "_n_1",nctx.node);
    Expr p_0=ctx.mkConst("n_WebServer_" + n_WebServer + "_p_0",nctx.packet);
    Expr p_1=ctx.mkConst("n_WebServer_" + n_WebServer + "_p_1",nctx.packet);
    IntExpr t_0=ctx.mkIntConst("n_WebServer_" + n_WebServer + "_t_0");
    IntExpr t_1=ctx.mkIntConst("n_WebServer_" + n_WebServer + "_t_1");
    constraints.add(ctx.mkForall(new Expr[]{t_0,p_0,n_0},ctx.mkImplies((BoolExpr)nctx.send.apply(n_WebServer,n_0,p_0,t_0),ctx.mkExists(new Expr[]{t_1,p_1,n_1},ctx.mkAnd((BoolExpr)nctx.recv.apply(n_1,n_WebServer,p_1,t_1),ctx.mkLt(t_1,t_0),ctx.mkEq(nctx.pf.get("proto").apply(p_1),ctx.mkInt(nctx.HTTP_REQUEST)),(BoolExpr)matchEntry.apply(nctx.pf.get("url").apply(p_1)),ctx.mkEq(nctx.pf.get("src").apply(p_0),nctx.pf.get("dest").apply(p_1)),ctx.mkEq(nctx.src_port.apply(p_0),nctx.dest_port.apply(p_1)),ctx.mkEq(nctx.pf.get("dest").apply(p_0),nctx.pf.get("src").apply(p_1)),ctx.mkEq(nctx.dest_port.apply(p_0),nctx.src_port.apply(p_1)),ctx.mkEq(nctx.pf.get("proto").apply(p_0),ctx.mkInt(nctx.HTTP_RESPONSE)),ctx.mkEq(nctx.pf.get("body").apply(p_0),nctx.pf.get("body").apply(p_1)),ctx.mkEq(nctx.pf.get("origin").apply(p_0),nctx.pf.get("origin").apply(p_1)),ctx.mkEq(nctx.pf.get("orig_body").apply(p_0),nctx.pf.get("orig_body").apply(p_1)),ctx.mkEq(nctx.pf.get("body").apply(p_0),nctx.pf.get("body").apply(p_1)),ctx.mkEq(nctx.pf.get("seq").apply(p_0),nctx.pf.get("seq").apply(p_1)),ctx.mkEq(nctx.pf.get("mailFrom").apply(p_0),nctx.pf.get("mailFrom").apply(p_1)),ctx.mkEq(nctx.pf.get("url").apply(p_0),nctx.pf.get("url").apply(p_1)),ctx.mkEq(nctx.pf.get("options").apply(p_0),nctx.pf.get("options").apply(p_1))),1,null,null,null,null)),1,null,null,null,null));
  }
}
