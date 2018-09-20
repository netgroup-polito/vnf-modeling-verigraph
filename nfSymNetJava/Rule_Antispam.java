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
class Rule_Antispam {
  public InstructionBlock generate_rules(  ConfigParameter[] params){
    InstructionBlock code=InstructionBlock(Assign("flag",ConstantValue(0)),If(Constrain(Proto,postParsef(ConstantValue(POP3REQUEST.value))),NoOp,Constrain(Proto,postParsef(ConstantValue(POP3RESPONSE.value)))),InstructionBlock(addrule(params)));
    return code;
  }
  public InstructionBlock[] addrule(  ConfigParameter[] p){
    InstructionBlock[] rule;
    InstructionBlock[] rules=Array(InstructionBlock(Nil));
    int limit=p.length() / 1;
    for (int i=0; i < limit; i=i + 1) {
      rule=Array(InstructionBlock(If(Constrain(EmailFrom,postParsef(ConstantValue(p(i + 0).value.toInt))),Fail("Match-in-blacklist"),NoOp)));
      rules=Array.concat(rules,rule);
    }
    return rules;
  }
}
