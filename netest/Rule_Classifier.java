package modify_it;
import org.change.v2.analysis.expression.concrete._;
import org.change.v2.analysis.memory.State;
import org.change.v2.analysis.memory.TagExp._;
import org.change.v2.analysis.memory.Tag;
import org.change.v2.analysis.processingmodels.instructions._;
import org.change.v2.util.conversion.RepresentationConversion._;
class SymRule_Classifier {
  public State[][] generate_rules(  String[][] params){
    InstructionBlock code=InstructionBlock(CreateTag("L3HeaderStart",0),CreateTag("IP_SRC",Tag("L3HeaderStart") + 96),Allocate(Tag("IP_SRC"),32),Assign(Tag("IP_SRC"),SymbolicValue()),CreateTag("IP_DST",Tag("L3HeaderStart") + 128),Allocate(Tag("IP_DST"),32),Assign(Tag("IP_DST"),SymbolicValue()),CreateTag("PORT_SRC",Tag("L3HeaderStart") + 160),Allocate(Tag("PORT_SRC"),32),Assign(Tag("PORT_SRC"),SymbolicValue()),CreateTag("PORT_DST",Tag("L3HeaderStart") + 192),Allocate(Tag("PORT_DST"),32),Assign(Tag("PORT_DST"),SymbolicValue()),CreateTag("PROTO",Tag("L3HeaderStart") + 64),Allocate(Tag("PROTO"),32),Assign(Tag("PROTO"),SymbolicValue()),CreateTag("ORIGIN",Tag("L3HeaderStart") + 224),Allocate(Tag("ORIGIN"),32),Assign(Tag("ORIGIN"),SymbolicValue()),CreateTag("ORIG_BODY",Tag("L3HeaderStart") + 256),Allocate(Tag("ORIG_BODY"),32),Assign(Tag("ORIG_BODY"),SymbolicValue()),CreateTag("BODY",Tag("L3HeaderStart") + 288),Allocate(Tag("BODY"),32),Assign(Tag("BODY"),SymbolicValue()),CreateTag("SEQUENCE",Tag("L3HeaderStart") + 320),Allocate(Tag("SEQUENCE"),32),Assign(Tag("SEQUENCE"),SymbolicValue()),CreateTag("EMAIL_FROM",Tag("L3HeaderStart") + 352),Allocate(Tag("EMAIL_FROM"),32),Assign(Tag("EMAIL_FROM"),SymbolicValue()),CreateTag("URL",Tag("L3HeaderStart") + 384),Allocate(Tag("URL"),32),Assign(Tag("URL"),SymbolicValue()),CreateTag("OPTIONS",Tag("L3HeaderStart") + 416),Allocate(Tag("OPTIONS"),32),Assign(Tag("OPTIONS"),SymbolicValue()),CreateTag("INNER_SRC",Tag("L3HeaderStart") + 448),Allocate(Tag("INNER_SRC"),32),Assign(Tag("INNER_SRC"),SymbolicValue()),CreateTag("INNER_DEST",Tag("L3HeaderStart") + 480),Allocate(Tag("INNER_DEST"),32),Assign(Tag("INNER_DEST"),SymbolicValue()),CreateTag("ENCRYPTED",Tag("L3HeaderStart") + 512),Allocate(Tag("ENCRYPTED"),32),Assign(Tag("ENCRYPTED"),SymbolicValue()),Assign("flag",ConstantValue(0)),InstructionBlock(addrule(params)),Forward("interfaceIdSend"));
    code(State.clean,true);
  }
  public InstructionBlock[] addrule(  String[][] p){
    InstructionBlock[] rule;
    InstructionBlock[] rules=Array();
    for (int i=0; i < p.length(); i++) {
      rule=Array(InstructionBlock(If(Constrain(Tag("flag"),ConstantValue(0)),InstructionBlock(If(Constrain(Tag("PROTO"),ConstantValue(p(i + 0))),Assign(Tag("flag"),ConstantValue(1)),NoOp)),NoOp)));
      rules=Array.concat(rules,rule);
    }
    rule=Array(InstructionBlock(If(Constrain(Tag("flag"),ConstantValue(0)),Fail("NoMatch"),NoOp)));
    rules=Array.concat(rules,rule);
    return rules;
  }
}
