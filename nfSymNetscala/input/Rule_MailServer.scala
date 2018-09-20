package org.change.v2.abstractnet.click.sefl

import org.change.v2.analysis.expression.concrete._
import org.change.v2.analysis.memory.State
import org.change.v2.analysis.memory.TagExp._
import org.change.v2.analysis.memory.Tag
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.util.conversion.RepresentationConversion._
import org.change.v2.util.canonicalnames._
import org.change.v2.analysis.memory.Value
import org.change.v2.abstractnet.generic._
//remove if not needed
import scala.collection.JavaConversions._

class Rule_MailServer {

  def generate_rules(params: Array[ConfigParameter]): InstructionBlock = {
    val code = InstructionBlock(Assign("flag", ConstantValue(0)), Constrain(Proto, postParsef(ConstantValue(POP3REQUEST.value))), 
      InstructionBlock(Assign(Proto, ConstantValue(POP3RESPONSE.value)), Assign("tmp"), Assign(IPSrc, 
      Fchiocciola(IP_DST)), Assign(IPDst, Fchiocciola("tmp")), Deallocate("tmp"), Assign(EmailFrom, ConstantValue(RESPONSE.value))))
    code
  }
}
