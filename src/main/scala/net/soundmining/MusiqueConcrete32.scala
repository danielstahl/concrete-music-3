package net.soundmining

import scala.io.StdIn
import net.soundmining.Instrument._
import net.soundmining.modular.Instruments._

object MusiqueConcrete32 {
    val SOUND_BASE_DIR = "/Users/danielstahl/Documents/Projects/musique-concrete-iii/sounds/"

    object FilterType extends Enumeration {
        type FilterType = Value
        val HighPass, LowPass, NoFilter = Value
    }

    case class SoundPlay(bufNum: Integer, start: Float, end: Float, highPass: Option[Float] = None, lowPass: Option[Float] = None) 

    val soundPlays = Map(
        "tiles-rattle-1" -> SoundPlay(0, 0.35f, 0.6f, highPass = Some(5000f), lowPass = Some(3500f)),
        "tiles-rattle-2" -> SoundPlay(0, 0.65f, 1.2f, highPass = Some(5000f), lowPass = Some(3500f)),
        "tiles-rattle-3" -> SoundPlay(0, 1.25f, 1.4f, highPass = Some(5000f), lowPass = Some(3500f)),
        "tiles-scratch-1" -> SoundPlay(1, 0.471f, 0.874f, highPass = Some(5625f), lowPass = Some(3375f)),
        "tiles-scratch-2" -> SoundPlay(1, 1.382f, 2.247f, highPass = Some(5625f), lowPass = Some(3375f)),
        "tiles-scratch-3" -> SoundPlay(1, 2.660f, 3.638f, highPass = Some(5625f), lowPass = Some(3375f))
    )


    def playSound(name: String, startTime: Float, volume: Float = 1.0f, rate: Float = 1.0f, pan: Float = 0.0f)(implicit player: MusicPlayer): Unit = {
        val soundPlay = soundPlays(name)
        
        StereoSoundNote(bufNum = soundPlay.bufNum, volume = volume)
            .left(_.playLeft(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .right(_.playRight(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .mixAudio(staticControl(volume))
            .pan(staticControl(pan))
            .play(startTime = startTime)
    }

    def filterFreq(rate: Float, freq: Float): Float = 
        rate * freq

    def playSoundLowpass(name: String, startTime: Float, volume: Float = 1.0f, rate: Float = 1.0f, pan: Float = 0.0f)(implicit player: MusicPlayer): Unit = {
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

    def playSoundHighpass(name: String, startTime: Float, volume: Float = 1.0f, rate: Float = 1.0f, pan: Float = 0.0f)(implicit player: MusicPlayer): Unit = {
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

    def playTilesRattle(start: Float = 0)(implicit player: MusicPlayer): Unit = {
        playSound("tiles-rattle-1", 0 + start)
        playSoundLowpass("tiles-rattle-1", 1 + start)
        playSoundHighpass("tiles-rattle-1", 2 + start)
    
        playSound("tiles-rattle-2", 3 + start)
        playSoundLowpass("tiles-rattle-2", 4 + start)
        playSoundHighpass("tiles-rattle-2", 5 + start)

        playSound("tiles-rattle-3", 6 + start)
        playSoundLowpass("tiles-rattle-3", 7 + start)
        playSoundHighpass("tiles-rattle-3", 8 + start)
    }

    def playTilesScratch(start: Float = 0)(implicit player: MusicPlayer): Unit = {
        playSound("tiles-scratch-1", 0 + start)
        playSoundHighpass("tiles-scratch-1", 1 + start)
        playSoundLowpass("tiles-scratch-1", 2 + start)

        playSound("tiles-scratch-2", 3 + start)
        playSoundHighpass("tiles-scratch-2", 4 + start)
        playSoundLowpass("tiles-scratch-2", 5 + start)

        playSound("tiles-scratch-3", 6 + start)
        playSoundHighpass("tiles-scratch-3", 7 + start)
        playSoundLowpass("tiles-scratch-3", 8 + start)
    }

    def test(implicit player: MusicPlayer): Unit = {
        playSoundLowpass("tiles-rattle-1", 0, pan = 0.5f)
        playSoundHighpass("tiles-scratch-1", 0, pan = -0.5f)
        playSoundHighpass("tiles-scratch-1", 0.3f, rate = 2.5f, pan = 0.5f)
        playSoundLowpass("tiles-rattle-1", 0.4f, rate = 3.5f, pan = -0.5f)
    }

    def main(args: Array[String]): Unit = {
        implicit val player: MusicPlayer = MusicPlayer()
        player.startPlay()
        setupNodes(player)

        player.sendBundle(0, Seq(player.makeAllocRead(0, s"${SOUND_BASE_DIR}tiles-rattle.flac")))
        player.sendBundle(0, Seq(player.makeAllocRead(1, s"${SOUND_BASE_DIR}tiles-scratch-1.flac")))

        //playTilesRattle()
        //playTilesScratch(9)
        test

        Console.println("Print q to quit")
        val cmd = StdIn.readLine()
        Console.println(s"You typed $cmd, goodBye")
        player.stopPlay()
    }
}
