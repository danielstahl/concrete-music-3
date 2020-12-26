package net.soundmining

import net.soundmining.synth._
import SuperColliderClient._
import net.soundmining.synth.SoundNote._
import net.soundmining.modular.ModularSynth._

object  MusiqueConcrete3 {
    implicit val client: SuperColliderClient = SuperColliderClient()
    val SOUND_BASE_DIR = "/Users/danielstahl/Documents/Projects/musique-concrete-iii/sounds/"
    val SYNTH_DIR = "/Users/danielstahl/Documents/Projects/soundmining-modular/src/main/sc/synths"

    case class SoundPlay(bufNum: Int, start: Double, end: Double, highPass: Option[Double] = None, lowPass: Option[Double] = None) {
        def duration(rate: Double) = math.abs((end - start) / rate)
    }

    val soundPlays = Map(
        "tiles-rattle-1" -> SoundPlay(0, 0.35, 0.6, highPass = Some(5000), lowPass = Some(3500)),
        "tiles-rattle-2" -> SoundPlay(0, 0.65, 1.2, highPass = Some(5000), lowPass = Some(3500)),
        "tiles-rattle-3" -> SoundPlay(0, 1.25, 1.4, highPass = Some(5000), lowPass = Some(3500)),
        "tiles-scratch-1" -> SoundPlay(1, 0.471, 0.874, highPass = Some(5625), lowPass = Some(3375)),
        "tiles-scratch-2" -> SoundPlay(1, 1.382, 2.247, highPass = Some(5625), lowPass = Some(3375)),
        "tiles-scratch-3" -> SoundPlay(1, 2.660, 3.638, highPass = Some(5625), lowPass = Some(3375))
    )

    def playSound(name: String, startTime: Double, volume: Double = 1.0f, rate: Double = 1.0f, pan: Double = 0.0f): Unit = {
        val soundPlay = soundPlays(name)
        
        StereoSoundNote(bufNum = soundPlay.bufNum, volume = volume)
            .left(_.playLeft(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .right(_.playRight(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .mixAudio(staticControl(volume))
            .pan(staticControl(pan))
            .play(startTime = startTime)
    }

    def filterFreq(rate: Double, freq: Double): Double = 
        rate * freq

    def playSoundLowpass(name: String, startTime: Double, volume: Double = 1.0f, rate: Double = 1.0f, pan: Double = 0.0f): Unit = {
        val soundPlay = soundPlays(name)
        val lowPassFreq = filterFreq(rate, soundPlay.lowPass.get)

        StereoSoundNote(bufNum = soundPlay.bufNum, volume = volume)
            .left(_.playLeft(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .right(_.playRight(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .mixAudio(staticControl(volume))
            .lowPass(staticControl(lowPassFreq))
            .pan(staticControl(pan))
            .play(startTime = startTime)
    }

    def playSoundHighpass(name: String, startTime: Double, volume: Double = 1.0f, rate: Double = 1.0f, pan: Double = 0.0f): Unit = {
        val soundPlay = soundPlays(name)
        val highPassFreq = filterFreq(rate, soundPlay.highPass.get)
        
        StereoSoundNote(bufNum = soundPlay.bufNum, volume = volume)
            .left(_.playLeft(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .right(_.playRight(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .mixAudio(staticControl(volume))
            .highPass(staticControl(highPassFreq))
            .pan(staticControl(pan))
            .play(startTime = startTime)
    }

    def playTilesRattle(start: Double = 0): Unit = {
        client.resetClock
        playSound("tiles-rattle-1", 0 + start)
    }

    def theme1(start: Double = 0): Unit = {
        client.resetClock
        playSoundHighpass("tiles-rattle-1", 0, pan = 0.5f)
        playSoundHighpass("tiles-scratch-1", 0, pan = -0.5f)

        val start2 = Seq("tiles-rattle-2").map(soundPlays(_).duration(1.0)).sum

        {
            val indices = (0 until 5)
            val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2"), Seq.fill(1)("tiles-scratch-1")).flatten
            val overlaps = Melody.absolute(0.66f, Seq.fill(5)((0.66f - 0.33f) / 5))
            val rates = Melody.absolute(1.0f, Seq.fill(5)(0.01f))
            val pans = Melody.absolute(-0.2f, Seq.fill(5)((0.2f / 5) * -1))
            val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
            indices.foreach(i => playSoundHighpass(scratchPlays(i), times(i), rate=rates(i), pan = pans(i)))
            val rattles = Seq(1, 4)
            val rattleSounds = Seq("tiles-rattle-1", "tiles-rattle-2")
            (0 until 2).foreach(i => playSoundHighpass(rattleSounds(i), times(rattles(i)), rate=rates(rattles(i)), pan=pans(rattles(i)) * -1))
        }
    }

    def init(): Unit = {
        println("Starting up SupderCollider client")
        client.start
        Instrument.setupNodes(client)
        client.send(loadDir(SYNTH_DIR))
        client.send(allocRead(0, s"${SOUND_BASE_DIR}tiles-rattle.flac"))
        client.send(allocRead(1, s"${SOUND_BASE_DIR}tiles-scratch-1.flac"))
    }

    def stop(): Unit = {
        println("Stopping Supercollider client")
        client.stop
    }
}
