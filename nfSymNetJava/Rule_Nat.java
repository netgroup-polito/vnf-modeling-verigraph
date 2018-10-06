package org.change.v2.abstractnet.click.sefl;
import org.change.v2.analysis.expression.concrete._;
import org.change.v2.analysis.memory.State;
import org.change.v2.analysis.memory.TagExp._;
import org.change.v2.analysis.memory.Tag;
import org.change.v2.analysis.processingmodels.instructions._;
import org.change.v2.util.conversion.RepresentationConversion._;
import org.change.v2.util.canonicalnames._;
import org.change.v2.analysis.memory.Value;
import org.change.v2.abstractnet.generic._;
import org.change.v2.analysis.expression.concrete.nonprimitive._;
class Rule_Nat {
  public InstructionBlock generate_rules(  ConfigParameter[] params){
    InstructionBlock code=InstructionBlock(Assign("flag",ConstantValue(0)),InstructionBlock(checkstate(params)),If(Constrain("flag",postParsef(ConstantValue(1))),InstructionBlock(InstructionBlock(Assign(IPSrc,ConstantValue(natIp.value)))),NoOp));
    return code;
  }
  public InstructionBlock[] checkstate(  ConfigParameter[] p){
    InstructionBlock[] rule;
    InstructionBlock[] rules=Array(InstructionBlock(Nil));
    int limit=p.length() - 1;
    for (int i=0; i <= limit; i=i + 1) {
      rule=Array(InstructionBlock(If(Constrain("flag",postParsef(ConstantValue(0))),InstructionBlock(If(Constrain(IPSrc,postParsef(ConstantValue(p(i + 0).value.toInt))),InstructionBlock(Assign("flag",ConstantValue(1))),NoOp)),NoOp)));
      rules=Array.concat(rules,rule);
    }
    rules=Array.concat(rules,rule);
    return rules;
  }
}
