package net.soundmining.synth

import net.soundmining.synth._
import SuperColliderClient._
import net.soundmining.modular.ModularSynth._
import net.soundmining.modular.ModularInstrument.ControlInstrument

case class SoundPlay(bufNum: Int, start: Double, end: Double, 
                     highPass: Option[Double] = None, lowPass: Option[Double] = None, 
                     amp: Double => ControlInstrument = amp => staticControl(amp),
                     spectrumFreqs: Seq[Double] = Seq.empty, peakTimes: Seq[Double] = Seq.empty) {
  def duration(rate: Double) = math.abs((end - start) / rate)
  def absoluteTime(time: Double, rate: Double) = math.abs((time - start) / rate)

}

final case class SoundPlays(soundPlays: Map[String, SoundPlay], 
                            masterVolume: Double = 1.0, numberOfOutputBuses: Int = 2) {

  def getRealOutputBus(outputBus: Int): Int =
    if(numberOfOutputBuses > 2) (outputBus % numberOfOutputBuses) + 2
    else outputBus % numberOfOutputBuses

  def playSound2(name: String, startTime: Double, volume: Double = 1.0, volumeControl: Double => ControlInstrument = amp => staticControl(amp), rate: Double = 1.0, pan: (Double, Double) = (0.0, 0.0),
                lowPass: Option[Double] = None, highPass: Option[Double] = None, bandPass: Option[(Double, Double)] = None, ringModulate: Option[Double] = None,
                outputBus: Int = 0)(implicit client: SuperColliderClient): Unit = {
    playSoundInternal(name, startTime, volume, volumeControl, rate, lineControl(pan._1, pan._2), lowPass, highPass, bandPass, ringModulate, outputBus)
  }

  def playSound(name: String, startTime: Double, volume: Double = 1.0, rate: Double = 1.0, pan: Double = 0.0,
                lowPass: Option[Double] = None, highPass: Option[Double] = None, bandPass: Option[(Double, Double)] = None, ringModulate: Option[Double] = None,
                outputBus: Int = 0)(implicit client: SuperColliderClient): Unit = {
    playSoundInternal(name, startTime, volume, vol => staticControl(vol), rate, staticControl(pan), lowPass, highPass, bandPass, ringModulate, outputBus)
  }

  def playSoundInternal(name: String, startTime: Double, volume: Double, volumeControl: Double => ControlInstrument = amp => staticControl(amp), rate: Double, pan: ControlInstrument,
                lowPass: Option[Double], highPass: Option[Double], bandPass: Option[(Double, Double)], ringModulate: Option[Double],
                outputBus: Int)(implicit client: SuperColliderClient): Unit = {
      val soundPlay = soundPlays(name)

      var note = StereoSoundNote(bufNum = soundPlay.bufNum, volume = volume * masterVolume)
          .left(_.playLeft(soundPlay.start, soundPlay.end, rate, volumeControl(volume)))
          .right(_.playRight(soundPlay.start, soundPlay.end, rate, volumeControl(volume)))
          .mixAudio(soundPlay.amp(volume * masterVolume))

      note = ringModulate.map(freq => note.ring(staticControl(filterFreq(rate, freq)))).getOrElse(note)
      note = lowPass.map(freq => note.lowPass(staticControl(filterFreq(rate, freq)))).getOrElse(note)
      note = highPass.map(freq => note.highPass(staticControl(filterFreq(rate, freq)))).getOrElse(note)
      note = bandPass.map {
        case (freq, rq) => note.bandPass(staticControl(filterFreq(rate, freq)), staticControl(rq))
      }.getOrElse(note)

      note.pan(pan)
          .play(startTime = startTime, outputBus = getRealOutputBus(outputBus))
  }

  def playSub(name: String, subFreq: Double = 30, startTime: Double, volume: Double = 1.0, attackTime: Double = 0.01, releaseTime: Double = 0.01, rate: Double = 1.0, pan: (Double, Double) = (0.0, 0.0), outputBus: Int = 0)(implicit client: SuperColliderClient) = {
    val soundPlay = soundPlays(name)

    StereoSoundNote(bufNum = soundPlay.bufNum, volume = volume * masterVolume)
      .left(_.playLeft(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
      .right(_.playRight(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
      .mixAudio(soundPlay.amp(volume * masterVolume))
      .sub(subFreq, attackTime, releaseTime)
      .pan(lineControl(pan._1, pan._2))
      .play(startTime = startTime, outputBus = getRealOutputBus(outputBus))
  }


    def filterFreq(rate: Double, freq: Double): Double = 
        rate * freq

    def apply(key: String): SoundPlay = soundPlays(key)
}

