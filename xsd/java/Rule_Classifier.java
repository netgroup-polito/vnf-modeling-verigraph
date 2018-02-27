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
public class Rule_Classifier extends NetworkObject {
  List<BoolExpr> constraints;
  Context ctx;
  DatatypeExpr n_Classifier;
  Network net;
  NetContext nctx;
  FuncDecl isInternal;
  FuncDecl matchEntry;
  ArrayList<ArrayList<Expr>> entries;
  public Rule_Classifier(  Context ctx,  Object[]... args){
    super(ctx,args);
  }
  @Override protected void init(  Context ctx,  Object[]... args){
    this.ctx=ctx;
    isEndHost=false;
    constraints=new ArrayList<BoolExpr>();
    z3Node=((NetworkObject)args[0][0]).getZ3Node();
    n_Classifier=z3Node;
    net=(Network)args[0][1];
    nctx=(NetContext)args[0][2];
    net.saneSend(this);
    isInternal=ctx.mkFuncDecl(n_Classifier + "_isInternal",nctx.address,ctx.mkBoolSort());
    matchEntry=ctx.mkFuncDecl(n_Classifier + "_matchEntry",new Sort[]{ctx.mkIntSort()},ctx.mkBoolSort());
    entries=new ArrayList<ArrayList<Expr>>();
  }
  @Override public DatatypeExpr getZ3Node(){
    return n_Classifier;
  }
  @Override protected void addConstraints(  Solver solver){
    BoolExpr[] constr=new BoolExpr[constraints.size()];
    solver.add(constraints.toArray(constr));
    if (entries.size() == 0)     return;
    Expr e_0=ctx.mkIntConst(n_Classifier + "_entry_e_0");
    BoolExpr[] entry_map=new BoolExpr[entries.size()];
    for (int i=0; i < entries.size(); i++) {
      entry_map[i]=ctx.mkAnd(ctx.mkEq(e_0,entries.get(i).get(0)));
    }
    solver.add(ctx.mkForall(new Expr[]{e_0},ctx.mkEq(matchEntry.apply(e_0),ctx.mkOr(entry_map)),1,null,null,null,null));
  }
  public void setInternalAddress(  ArrayList<DatatypeExpr> internalAddress){
    List<BoolExpr> constr=new ArrayList<BoolExpr>();
    Expr in_0=ctx.mkConst(n_Classifier + "_internal_node",nctx.address);
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
  public void installClassifier(){
    Expr n_0=ctx.mkConst("n_Classifier_" + n_Classifier + "_n_0",nctx.node);
    Expr n_1=ctx.mkConst("n_Classifier_" + n_Classifier + "_n_1",nctx.node);
    Expr p_0=ctx.mkConst("n_Classifier_" + n_Classifier + "_p_0",nctx.packet);
    Expr p_1=ctx.mkConst("n_Classifier_" + n_Classifier + "_p_1",nctx.packet);
    IntExpr t_0=ctx.mkIntConst("n_Classifier_" + n_Classifier + "_t_0");
    IntExpr t_1=ctx.mkIntConst("n_Classifier_" + n_Classifier + "_t_1");
    constraints.add(ctx.mkForall(new Expr[]{t_0,p_0,n_0},ctx.mkImplies((BoolExpr)nctx.send.apply(n_Classifier,n_0,p_0,t_0),ctx.mkExists(new Expr[]{t_1,p_1,n_1},ctx.mkAnd((BoolExpr)nctx.recv.apply(n_1,n_Classifier,p_1,t_1),ctx.mkLt(t_1,t_0),(BoolExpr)matchEntry.apply(nctx.pf.get("proto").apply(p_1)),ctx.mkEq(nctx.pf.get("src").apply(p_0),nctx.pf.get("src").apply(p_1)),ctx.mkEq(nctx.pf.get("dest").apply(p_0),nctx.pf.get("dest").apply(p_1)),ctx.mkEq(nctx.pf.get("src_port").apply(p_0),nctx.pf.get("src_port").apply(p_1)),ctx.mkEq(nctx.pf.get("dst_port").apply(p_0),nctx.pf.get("dst_port").apply(p_1)),ctx.mkEq(nctx.pf.get("transport_protocol").apply(p_0),nctx.pf.get("transport_protocol").apply(p_1)),ctx.mkEq(nctx.pf.get("proto").apply(p_0),nctx.pf.get("proto").apply(p_1)),ctx.mkEq(nctx.pf.get("application_data").apply(p_0),nctx.pf.get("application_data").apply(p_1))),1,null,null,null,null)),1,null,null,null,null));
  }
}
