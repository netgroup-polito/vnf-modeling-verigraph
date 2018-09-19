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
class Rule_Classifier {
  public InstructionBlock generate_rules(  ConfigParameter[] params){
    InstructionBlock code=InstructionBlock(Assign("flag",ConstantValue(0)),InstructionBlock(),InstructionBlock(addrule(params)));
    return code;
  }
  public InstructionBlock[] addrule(  ConfigParameter[] p){
    InstructionBlock[] rule;
    InstructionBlock[] rules=Array(InstructionBlock(Nil));
    int limit=p.length() / 2;
    for (int i=0; i < limit; i=i + 2) {
      rule=Array(InstructionBlock(If(Constrain("flag",postParsef(ConstantValue(0))),InstructionBlock(If(Constrain(Proto,postParsef(ConstantValue(p(i + 0).value.toInt))),InstructionBlock(Assign("flag",ConstantValue(1)),Assign("idIfSend",ConstantValue(p(i + 1).value.toInt))),NoOp)),NoOp)));
      rules=Array.concat(rules,rule);
    }
    rule=Array(InstructionBlock(If(Constrain("flag",postParsef(ConstantValue(0))),Fail("No-Match"),NoOp)));
    rules=Array.concat(rules,rule);
    return rules;
  }
}
