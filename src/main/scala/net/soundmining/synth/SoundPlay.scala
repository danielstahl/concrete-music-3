package net.soundmining.synth

import net.soundmining.synth._
import SuperColliderClient._
import net.soundmining.modular.ModularSynth._
import net.soundmining.modular.ModularInstrument.ControlInstrument

case class SoundPlay(bufNum: Int, start: Double, end: Double, 
                     highPass: Option[Double] = None, lowPass: Option[Double] = None, 
                     amp: Double => ControlInstrument = amp => staticControl(amp),
                     spectrumFreqs: Seq[Double] = Seq.empty) {
        def duration(rate: Double) = math.abs((end - start) / rate)
}

final case class SoundPlays(soundPlays: Map[String, SoundPlay], 
                            masterVolume: Double = 1.0) {
    def playSound(name: String, startTime: Double, volume: Double = 1.0, rate: Double = 1.0, pan: Double = 0.0, 
                  lowPass: Option[Double] = None, highPass: Option[Double] = None, ringModulate: Option[Double] = None)(implicit client: SuperColliderClient): Unit = {
        val soundPlay = soundPlays(name)
        
        var note = StereoSoundNote(bufNum = soundPlay.bufNum, volume = volume * masterVolume)
            .left(_.playLeft(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .right(_.playRight(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .mixAudio(soundPlay.amp(volume * masterVolume))

        note = ringModulate.map(freq => note.ring(staticControl(filterFreq(rate, freq)))).getOrElse(note)
        note = lowPass.map(freq => note.lowPass(staticControl(filterFreq(rate, freq)))).getOrElse(note)
        note = highPass.map(freq => note.highPass(staticControl(filterFreq(rate, freq)))).getOrElse(note)
        
        note.pan(staticControl(pan))
            .play(startTime = startTime)
    }

    def filterFreq(rate: Double, freq: Double): Double = 
        rate * freq

    def apply(key: String): SoundPlay = soundPlays(key)
}

