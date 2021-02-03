package net.soundmining

import MusiqueConcrete3.client
import net.soundmining.synth._
import net.soundmining.modular.ModularSynth._
import net.soundmining.synth.SoundNote._
import net.soundmining.synth.SoundPlays._
import net.soundmining.synth.SoundPlay


object MusiqueConcrete4 {

    val CLOCK_SPRING_SPECTRUM_FREQS = Seq(206.504, 160.774, 380.784, 448.902, 1051.13, 1267.64, 1520.84, 1630.4)

    val sounds = Map(
        "clock-spring-1" -> SoundPlay(2, 2.613, 4.200, highPass = Some(1760), lowPass = Some(1625), 
            amp = volume => relativeThreeBlockcontrol(0, 0.3, volume * 2, volume * 2, 0.2, 0, Right(Instrument.LINEAR)),
            spectrumFreqs = CLOCK_SPRING_SPECTRUM_FREQS),
        "clock-spring-2" -> SoundPlay(2, 0.068, 1.079, highPass = Some(1760), lowPass = Some(1625), 
            amp = volume => relativeThreeBlockcontrol(0, 0.01, volume * 2, volume * 2, 0.3, 0, Right(Instrument.LINEAR)),
            spectrumFreqs = CLOCK_SPRING_SPECTRUM_FREQS),
        "clock-spring-3" -> SoundPlay(2, 1.077, 2.445, highPass = Some(1760), lowPass = Some(1625), 
            amp = volume => relativeThreeBlockcontrol(0, 0.01, volume * 2, volume * 2, 0.15, 0, Right(Instrument.LINEAR)),
            spectrumFreqs = CLOCK_SPRING_SPECTRUM_FREQS)
    )

    val soundPlays = SoundPlays(sounds)

    import soundPlays._

    def playClockSpring(start: Float = 0): Unit = {
        client.resetClock
        playSound("clock-spring-1", 0 + start)
        //playSoundHighpass("clock-spring-1", 3 + start)
        //playSoundLowpass("clock-spring-1", 6 + start)

        playSound("clock-spring-2", 9 + start)
        //playSoundHighpass("clock-spring-2", 12 + start)
        //playSoundLowpass("clock-spring-2", 15 + start)

        playSound("clock-spring-3", 18 + start)
        //playSoundHighpass("clock-spring-3", 21 + start)
        //playSoundLowpass("clock-spring-3", 24 + start)
    }

    def theme1(start: Double = 0): Unit = {
        client.resetClock
        playSound("clock-spring-2", 0, pan = -0.5f, highPass = soundPlays("clock-spring-2").highPass)
        playSound("clock-spring-1", 0, pan = 0.5f, lowPass = soundPlays("clock-spring-1").lowPass)
        // part of a development
        playSound("clock-spring-1", 0.6, pan = 0.2f, rate = 1.05, highPass = soundPlays("clock-spring-1").highPass)
    }

    def theme2(start: Double = 0, m1: Option[Double] = None, m2: Option[Double] = None): Unit = {
        client.resetClock

        val modFreq1 = m1.orElse(soundPlays("clock-spring-1").spectrumFreqs.lift(6))
        val modFreq2 = m2.orElse(soundPlays("clock-spring-1").spectrumFreqs.lift(7))

        playSound("clock-spring-1", 0, volume = 2f, rate = 0.5, pan = -0.5f, ringModulate = modFreq1)
        playSound("clock-spring-1", 0.1, volume = 2f, rate = 0.5, pan = 0.5f, ringModulate = modFreq2)

    }
}
