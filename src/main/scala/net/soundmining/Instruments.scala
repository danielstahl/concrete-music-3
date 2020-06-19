package net.soundmining

import java.{lang => jl}
import net.soundmining.Instrument.{buildFloat, buildInteger}
import net.soundmining.ModularInstrument.{AudioInstrument}

object Instruments {

  def playBuffer(bufNum: Integer, rate: Float, start: Float, end: Float): PlayBuffer =
    new PlayBuffer().playBuffer(bufNum, rate, start, end)

  def playLeftChannel(inBus: AudioInstrument, rate: Float, start: Float, end: Float): PlayLeft =
    new PlayLeft().playOneChannel(inBus, rate, start, end)

  def playRightChannel(inBus: AudioInstrument, rate: Float, start: Float, end: Float): PlayRight =
    new PlayRight().playOneChannel(inBus, rate, start, end)  

  def mix(inBus: AudioInstrument, rate: Float, start: Float, end: Float): Mix =
    new Mix().mix(inBus, rate, start, end)  

  def expand(leftInBus: AudioInstrument, rightInBus: AudioInstrument, rate: Float, start: Float, end: Float): Expand =
    new Expand().expand(leftInBus, rightInBus, rate, start, end)

  class PlayBuffer extends AudioInstrument  {
      type SelfType = PlayBuffer
      def self(): SelfType = this
      val instrumentName: String = "playBuffer"
    
      var bufNum: jl.Integer = _
      var rate: jl.Float = _
      var start: jl.Float = _
      var end: jl.Float = _

      def playBuffer(bufNum: Integer, rate: Float, start: Float, end: Float): SelfType = {
          this.bufNum = buildInteger(bufNum)
          this.rate = buildFloat(rate)
          this.start = buildFloat(start)
          this.end = buildFloat(end)
          self()
      }

      override def graph(parent: Seq[ModularInstrument.ModularInstrument]): Seq[ModularInstrument.ModularInstrument] = 
        appendToGraph(parent)

      override def internalBuild(startTime: Float, duration: Float): Seq[Object] =  
        Seq(
            "bufNum", bufNum,
            "rate", rate,
            "start", start,
            "end", end
        )  
  }

  abstract class PlayOneChannel extends AudioInstrument {
    var inBus: AudioInstrument = _
    var rate: jl.Float = _
    var start: jl.Float = _
    var end: jl.Float = _

      def playOneChannel(inBus: AudioInstrument, rate: Float, start: Float, end: Float): SelfType = {
        this.inBus = inBus
        this.rate = buildFloat(rate)
        this.start = buildFloat(start)
        this.end = buildFloat(end)
        self()
      }

      override def graph(parent: Seq[ModularInstrument.ModularInstrument]): Seq[ModularInstrument.ModularInstrument] = 
        appendToGraph(inBus.graph(parent))

      override def internalBuild(startTime: Float, duration: Float): Seq[Object] =  
        Seq(
           "in", buildInteger(
              inBus.getOutputBus.dynamicBus(startTime,
              startTime + inBus.optionalDur.getOrElse(duration))),
            "rate", rate,
            "start", start,
            "end", end
        )  
  }

  class PlayLeft extends PlayOneChannel {
    type SelfType = PlayLeft
      def self(): SelfType = this
      val instrumentName: String = "playLeft"
  }

  class PlayRight extends PlayOneChannel {
    type SelfType = PlayRight
      def self(): SelfType = this
      val instrumentName: String = "playRight"
  }

  class Mix extends AudioInstrument {
    var inBus: AudioInstrument = _
    var rate: jl.Float = _
    var start: jl.Float = _
    var end: jl.Float = _

    type SelfType = Mix
    def self(): SelfType = this
    val instrumentName: String = "mix"

    def mix(inBus: AudioInstrument, rate: Float, start: Float, end: Float): SelfType = {
      this.inBus = inBus
      this.rate = buildFloat(rate)
      this.start = buildFloat(start)
      this.end = buildFloat(end)
      self()
    }

    override def graph(parent: Seq[ModularInstrument.ModularInstrument]): Seq[ModularInstrument.ModularInstrument] = 
      appendToGraph(inBus.graph(parent))

    override def internalBuild(startTime: Float, duration: Float): Seq[Object] =  
      Seq(
          "in", buildInteger(
            inBus.getOutputBus.dynamicBus(startTime,
            startTime + inBus.optionalDur.getOrElse(duration))),
          "rate", rate,
          "start", start,
          "end", end
      )  
  }

  class Expand extends AudioInstrument {
    var leftInBus: AudioInstrument = _
    var rightInBus: AudioInstrument = _
    var rate: jl.Float = _
    var start: jl.Float = _
    var end: jl.Float = _

    type SelfType = Expand
    def self(): SelfType = this
    val instrumentName: String = "expand"

    def expand(leftInBus: AudioInstrument, rightInBus: AudioInstrument, rate: Float, start: Float, end: Float): SelfType = {
      this.leftInBus = leftInBus
      this.rightInBus = rightInBus
      this.rate = buildFloat(rate)
      this.start = buildFloat(start)
      this.end = buildFloat(end)
      self()
    }

    override def graph(parent: Seq[ModularInstrument.ModularInstrument]): Seq[ModularInstrument.ModularInstrument] = 
      appendToGraph(rightInBus.graph(leftInBus.graph(parent)))

    override def internalBuild(startTime: Float, duration: Float): Seq[Object] =  
      Seq(
          "leftIn", buildInteger(
            leftInBus.getOutputBus.dynamicBus(startTime,
            startTime + leftInBus.optionalDur.getOrElse(duration))),
          "rightIn", buildInteger(
            rightInBus.getOutputBus.dynamicBus(startTime,
            startTime + rightInBus.optionalDur.getOrElse(duration))),
          "rate", rate,
          "start", start,
          "end", end
      )  
  }
}
