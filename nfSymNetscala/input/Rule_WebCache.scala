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

class Rule_WebCache {

  def generate_rules(params: Array[ConfigParameter]): InstructionBlock = {
    val code = InstructionBlock(Assign("flag", ConstantValue(0)), Constrain(Proto, postParsef(ConstantValue(HTTPREQUEST.value))), 
      InstructionBlock(checkstate(params)), If(Constrain("flag", postParsef(ConstantValue(1))), InstructionBlock(InstructionBlock(Assign(Proto, 
      ConstantValue(HTTPRESPONSE.value)), Allocate("tmp"), Assign("tmp", Fchiocciola(IPSrc)), Assign(IPSrc, 
      Fchiocciola(IPDst)), Assign(IPDst, Fchiocciola("tmp")), Deallocate("tmp"))), NoOp))
    code
  }

  def checkstate(p: Array[ConfigParameter]): Array[InstructionBlock] = {
    var rule: Array[InstructionBlock] = null
    var rules = Array(InstructionBlock(Nil))
    val limit = p.length - 1
    var i = 0
    while (i <= limit) {
      rule = Array(InstructionBlock(If(Constrain("flag", postParsef(ConstantValue(0))), InstructionBlock(If(Constrain(URL, 
        postParsef(ConstantValue(p(i + 0).value.toInt))), InstructionBlock(Assign("flag", ConstantValue(1))), 
        NoOp)), NoOp)))
      rules = Array.concat(rules, rule)
      i = i + 1
    }
    rules = Array.concat(rules, rule)
    rules
  }
}
