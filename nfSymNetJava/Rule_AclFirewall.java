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
class Rule_AclFirewall {
  public InstructionBlock generate_rules(  ConfigParameter[] params){
    InstructionBlock code=InstructionBlock(Assign("flag",ConstantValue(0)),InstructionBlock(),InstructionBlock(addrule(params)));
    return code;
  }
  public InstructionBlock[] addrule(  ConfigParameter[] p){
    InstructionBlock[] rule;
    InstructionBlock[] rules=Array(InstructionBlock(Nil));
    int limit=p.length() / 2;
    for (int i=0; i < limit; i=i + 2) {
      rule=Array(InstructionBlock(If(Constrain(IPSrc,postParsef(ConstantValue(ipToNumber(p(i + 0).value)))),InstructionBlock(If(Constrain(IPDst,postParsef(ConstantValue(ipToNumber(p(i + 1).value)))),Fail("Match-in-blacklist"),NoOp)),NoOp)));
      rules=Array.concat(rules,rule);
    }
    return rules;
  }
}
