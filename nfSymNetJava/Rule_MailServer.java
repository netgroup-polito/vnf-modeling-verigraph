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
class Rule_MailServer {
  public InstructionBlock generate_rules(  ConfigParameter[] params){
    InstructionBlock code=InstructionBlock(Assign("flag",ConstantValue(0)),Constrain(Proto,postParsef(ConstantValue(POP3REQUEST.value))),InstructionBlock(Assign(Proto,ConstantValue(POP3RESPONSE.value)),Allocate("tmp"),Assign("tmp",Fchiocciola(IPSrc)),Assign(IPSrc,Fchiocciola(IPDst)),Assign(IPDst,Fchiocciola("tmp")),Deallocate("tmp"),Assign(EmailFrom,ConstantValue(RESPONSE.value))));
    return code;
  }
}
