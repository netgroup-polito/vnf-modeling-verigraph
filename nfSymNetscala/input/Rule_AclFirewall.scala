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

class Rule_AclFirewall {

  def generate_rules(params: Array[ConfigParameter]): InstructionBlock = {
    val code = InstructionBlock(Assign("flag", ConstantValue(0)), InstructionBlock(), InstructionBlock(addrule(params)))
    code
  }

  def addrule(p: Array[ConfigParameter]): Array[InstructionBlock] = {
    var rule: Array[InstructionBlock] = null
    var rules = Array(InstructionBlock(Nil))
    val limit = p.length / 2
    var i = 0
    while (i < limit) {
      rule = Array(InstructionBlock(If(Constrain(IPSrc, postParsef(ConstantValue(ipToNumber(p(i + 0).value)))), 
        InstructionBlock(If(Constrain(IPDst, postParsef(ConstantValue(ipToNumber(p(i + 1).value)))), 
        Fail("Match-in-blacklist"), NoOp)), NoOp)))
      rules = Array.concat(rules, rule)
      i = i + 2
    }
    rules
  }
}
