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
import org.change.v2.analysis.expression.concrete.nonprimitive._
//remove if not needed
import scala.collection.JavaConversions._

class Rule_WebServer {

  def generate_rules(params:List[ConfigParameter]): InstructionBlock = {
    val code = InstructionBlock(Assign("flag", ConstantValue(0)), Constrain(Proto, :==:(ConstantValue(HTTPREQUEST.value))), 
      InstructionBlock(Assign(Proto, ConstantValue(HTTPRESPONSE.value)), Allocate("tmp"), Assign("tmp", 
      :@(IPSrc)), Assign(IPSrc, :@(IPDst)), Assign(IPDst, :@("tmp")), Deallocate("tmp")))
    code
  }
}
