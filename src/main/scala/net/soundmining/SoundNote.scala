package net.soundmining

import net.soundmining.Instrument.{setupNodes}
import scala.io.StdIn
import net.soundmining.Instrument.TAIL_ACTION
import net.soundmining.Utils.absoluteTimeToMillis
import net.soundmining.modular.Instruments._
import net.soundmining.Instrument.SINE
import net.soundmining.modular.ModularInstrument.AudioInstrument
import net.soundmining.modular.ModularInstrument.ControlInstrument
import net.soundmining.Instrument.EXPONENTIAL

case class Audio(audio: AudioInstrument, dur: Float)

abstract class SoundNote(bufNum: Integer = 0, volume: Float = 1.0f) {
    type SelfType <: SoundNote
    def self(): SelfType
    var audio: Option[Audio] = None

    def playLeft(start: Float, end: Float, rate: Float, amp: ControlInstrument): SelfType = {
        audio = Some(
            Audio(
                audio = left(playBuffer(bufNum, rate, start, end, staticControl(volume)).withNrOfChannels(2).addAction(TAIL_ACTION), amp).addAction(TAIL_ACTION), 
                dur = math.abs((end - start) / rate)))
        self()    
    }

    def playRight(start: Float, end: Float, rate: Float, amp: ControlInstrument): SelfType = {
        audio = Some(
            Audio(
                audio = right(playBuffer(bufNum, rate, start, end, staticControl(volume)).withNrOfChannels(2).addAction(TAIL_ACTION), amp).addAction(TAIL_ACTION),
                dur = math.abs((end - start) / rate)))
        self()    
    }

    def highPass(filterFreq: ControlInstrument): SelfType = {
        audio = audio.map {
            case Audio(audioInstrument, dur) => 
                Audio(highPassFilter(audioInstrument, filterFreq).addAction(TAIL_ACTION), dur)
        }
        self()
    }

    def lowPass(filterFreq: ControlInstrument): SelfType = {
        audio = audio.map {
            case Audio(audioInstrument, dur) => 
                Audio(lowPassFilter(audioInstrument, filterFreq).addAction(TAIL_ACTION), dur)
        }
        self()
    }

    def ring(modularFreq: ControlInstrument): SelfType = {
        audio = audio.map {
            case Audio(audioInstrument, dur) => 
                Audio(ringModulate(audioInstrument, modularFreq).addAction(TAIL_ACTION), dur)
        }
        self()
    }

    def pan(panPosition: ControlInstrument): SelfType = {
        audio = audio.map {
            case Audio(audioInstrument, dur) =>
                Audio(panning(audioInstrument, panPosition).addAction(TAIL_ACTION).withNrOfChannels(2), dur)
        }
        self()
    }

    def play(startTime: Float, outputBus: Integer = 0)(implicit player: MusicPlayer) = {
        audio.foreach {
            case Audio(audioInstrument, dur) =>
                audioInstrument.getOutputBus.staticBus(outputBus)
                val graph = audioInstrument.buildGraph(startTime, dur, audioInstrument.graph(Seq()))
                player.sendNew(absoluteTimeToMillis(startTime), graph)
        }
    }
}

case class MonoSoundNote(bufNum: Integer = 0, volume: Float = 1.0f) extends SoundNote(bufNum, volume) {
    override type SelfType = MonoSoundNote
    override def self(): SelfType = this
}

case class StereoSoundNote(bufNum: Integer = 0, volume: Float = 1.0f) extends SoundNote(bufNum, volume) {
    override type SelfType = StereoSoundNote
    override def self(): SelfType = this
    var leftNote: MonoSoundNote = MonoSoundNote(bufNum, volume)
    var rightNote: MonoSoundNote = MonoSoundNote(bufNum, volume)
    
    def left(func: (MonoSoundNote) => Unit): SelfType = {
        func(leftNote)
        self()
    }
        
    def right(func: (MonoSoundNote) => Unit): SelfType = {
        func(rightNote)  
        self()
    }
        
    def expandAudio(): SelfType = {
        (leftNote.audio, rightNote.audio) match {
            case (Some(left), Some(right)) => 
            this.audio = Some(Audio(
                audio = expand(left.audio, right.audio).addAction(TAIL_ACTION).withNrOfChannels(2),
                dur = math.max(left.dur, right.dur)))
            case _ => 
        }
        self()
    }  

    def mixAudio(amp: ControlInstrument): SelfType = {
        (leftNote.audio, rightNote.audio) match {
            case (Some(left), Some(right)) => 
                this.audio = Some(Audio(
                    audio = mix(left.audio, right.audio, amp).addAction(TAIL_ACTION),
                    dur = math.max(left.dur, right.dur)))
            case (Some(left), None) => this.audio = Some(left)
            case (None, Some(right)) => this.audio = Some(right)
            case _ => 
        }
        self()
    }
        
}
