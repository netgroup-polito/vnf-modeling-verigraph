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

class Rule_AclFirewall {

  def generate_rules(params:List[ConfigParameter]): InstructionBlock = {
    val code = InstructionBlock(Assign("flag", ConstantValue(0)), InstructionBlock(addrule(params)))
    code
  }

  def addrule(p:List[ConfigParameter]): Array[InstructionBlock] = {
    var rule: Array[InstructionBlock] = null
    var rules = Array(InstructionBlock(Nil))
    val limit = p.length - 2
    var i = 0
    while (i <= limit) {
      rule = Array(InstructionBlock(If(Constrain(IPSrc, :==:(ConstantValue(ipToNumber(p(i + 0).value)))), 
        InstructionBlock(If(Constrain(IPDst, :==:(ConstantValue(ipToNumber(p(i + 1).value)))), 
        Fail("Match-in-blacklist"), NoOp)), NoOp)))
      rules = Array.concat(rules, rule)
      i = i + 2
    }
    rules
  }
}
