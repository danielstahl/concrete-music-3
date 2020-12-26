package net.soundmining

/*
import scala.io.StdIn
import net.soundmining.Instrument._
import net.soundmining.modular.Instruments._
import com.illposed.osc.OSCPortIn
import com.illposed.osc.OSCPort
import com.illposed.osc.OSCListener
import com.illposed.osc.OSCMessage
import java.util.Date
*/
object MusiqueConcrete32 {
    /*
    val SOUND_BASE_DIR = "/Users/danielstahl/Documents/Projects/musique-concrete-iii/sounds/"

    object FilterType extends Enumeration {
        type FilterType = Value
        val HighPass, LowPass, NoFilter = Value
    }

    case class SoundPlay(bufNum: Integer, start: Float, end: Float, highPass: Option[Float] = None, lowPass: Option[Float] = None) {
        def duration(rate: Float) = math.abs((end - start) / rate)
    }

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

    def test1(implicit player: MusicPlayer): Unit = {
        playSoundLowpass("tiles-rattle-1", 0, pan = 0.5f)
        playSoundHighpass("tiles-scratch-1", 0, pan = -0.5f)
        playSoundHighpass("tiles-scratch-1", 0.3f, rate = 2.5f, pan = 0.5f)
        playSoundLowpass("tiles-rattle-1", 0.4f, rate = 3.5f, pan = -0.5f)
    }

    def test3(implicit player: MusicPlayer): Unit = {
        val scratchPlay = soundPlays("tiles-scratch-1")
        val overlaps = Melody.absolute(0.5f, Seq.fill(10)((1f - 0.5f) / 10))
        val rates = Melody.absolute(1f, Seq.fill(10)(0.005f))
        val pans = Melody.absolute(0.1f, Seq.fill(10)(0.05f))
        val times = Melody.absolute(0.0f, (rates zip overlaps).map{case (rate, overlap) => scratchPlay.duration(rate) * overlap})
        println(times)
        println(rates)
        (rates zip times zip pans).map{case ((rate, time), pan) => playSoundHighpass("tiles-scratch-1", time, rate=rate, pan = pan)}

        val rattles = Seq(0, 9)
        val rattleSounds = Seq("tiles-rattle-1", "tiles-rattle-2")
        (rattles zip rattleSounds).map{ case (rattle, sound) => 
            println(s"time ${times(rattle)}")
            playSoundHighpass(sound, times(rattle), rate=rates(rattle), pan=pans(rattle) * -1f)
        } 
    }

    def test4(implicit player: MusicPlayer): Unit = {
        val indices = (0 until 10)
        val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(3)("tiles-scratch-2"), Seq.fill(3)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2")).flatten
        val overlaps = Melody.absolute(0.33f, Seq.fill(10)((0.66f - 0.33f) / 10))
        val rates = Melody.absolute(0.8f, Seq.fill(10)(0.005f))
        val pans = Melody.absolute(-0.1f, Seq.fill(10)(0.05f))
        val times = Melody.absolute(0.0f, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        indices.foreach(i => playSoundHighpass(scratchPlays(i), times(i), rate=rates(i), pan = pans(i)))

        val rattles = Seq(0, 2, 5, 8, 9)
        val rattleSounds = Seq("tiles-rattle-1", "tiles-rattle-2", "tiles-rattle-1", "tiles-rattle-1", "tiles-rattle-2", "tiles-rattle-2")
        (0 until 5).foreach(i => playSoundHighpass(rattleSounds(i), times(rattles(i)), rate=rates(rattles(i)), pan=pans(rattles(i)) * -1))
    }

    def test2(implicit player: MusicPlayer): Unit = {
    
        val scratchPlay = soundPlays("tiles-scratch-1")
        val overlaps = Melody.absolute(0.33f, Seq.fill(20)((0.66f - 0.33f) / 20))
        val rates = Melody.absolute(0.5f, Seq.fill(20)(0.005f))

        val times = Melody.absolute(0.0f, (rates zip overlaps).map{case (rate, overlap) => scratchPlay.duration(rate) * overlap})

        (rates zip times).map{case (rate, time) => playSoundHighpass("tiles-scratch-1", time, rate=rate, pan = 0f)}

        //val length = soundPlays.get("tiles-scratch-1").map(soundPlay => soundPlay.duration(1.0f)).get
        //val overlapLen = length * 0.95f
        //println(s"len $length overlapLen $overlapLen")

        //Melody.absolute(2f, Seq.fill(2)(9f))
        //    .map(time => playSoundLowpass("tiles-rattle-1", time, rate = 0.05f, pan = 0.5f))
        //Melody.absolute(2f, Seq.fill(10)(overlapLen))
         //   .map(time => playSoundHighpass("tiles-scratch-1", time, rate = 1f, pan = -0.5f))

        // splitAt will split a list into two parts.
        // List(1, 2, 3).splitAt(2) -> (List(1, 2), List(3))
        // zip will join two lists
        //  List(1, 2, 3) zip List(2, 3, 4) -> List((1,2), (2,3), (3,4))
        
    }

    def theme1(implicit player: MusicPlayer): Unit = {
        // short
        playSoundHighpass("tiles-rattle-1", 0, pan = 0.5f)
        playSoundHighpass("tiles-scratch-1", 0, pan = -0.5f)

        // longer up from middle
        {
            val indices = (0 until 8)
            val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(1)("tiles-scratch-2"), Seq.fill(2)("tiles-scratch-1"), Seq.fill(3)("tiles-scratch-2")).flatten
            val overlaps = Melody.absolute(0.33f, Seq.fill(8)((0.66f - 0.33f) / 8))
            val rates = Melody.absolute(1.0f, Seq.fill(8)(0.01f))
            val pans = Melody.absolute(-0.4f, Seq.fill(8)(0.8f / 8))
            val times = Melody.absolute(3.0f, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
            indices.foreach(i => playSoundHighpass(scratchPlays(i), times(i), rate=rates(i), pan = pans(i)))
            val rattles = Seq(0, 7)
            val rattleSounds = Seq("tiles-rattle-1", "tiles-rattle-2")
            (0 until 2).foreach(i => playSoundHighpass(rattleSounds(i), times(rattles(i)), rate=rates(rattles(i)), pan=pans(rattles(i)) * -1))
        }
        // short up
        {
            val indices = (0 until 2)
            val scratchPlays = Seq(Seq.fill(3)("tiles-scratch-1")).flatten
            val overlaps = Seq.fill(3)(0.66f)
            val rates = Seq.fill(3)(1.0f + (8f * 0.01f))
            val pans = Seq.fill(3)(0)
            val times = Melody.absolute(6.0f, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
            indices.foreach(i => playSoundHighpass(scratchPlays(i), times(i), rate=rates(i), pan = pans(i)))
            val rattles = Seq(1)
            val rattleSounds = Seq("tiles-rattle-2")
            (0 until 1).foreach(i => playSoundHighpass(rattleSounds(i), times(rattles(i)), rate=rates(rattles(i)), pan=pans(rattles(i)) * -1))
        }
        // long down from up
        {
            val indices = (0 until 10)
            val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-2"), Seq.fill(3)("tiles-scratch-1"), Seq.fill(3)("tiles-scratch-2"), Seq.fill(2)("tiles-scratch-1")).flatten
            val overlaps = Melody.absolute(0.66f, Seq.fill(10)((0.33f - 0.66f) / 10))
            val top = 1.0f + (10f * 0.01f)
            val bottom = 1.0f - (10f * 0.01f)
            val rates = Melody.absolute(top, Seq.fill(10)((bottom - top) / 10f))
            val pans = Melody.absolute(0.5f, Seq.fill(10)(-0.5f / 10))
            val times = Melody.absolute(9.0f, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
            indices.foreach(i => playSoundHighpass(scratchPlays(i), times(i), rate=rates(i), pan = pans(i)))
            val rattles = Seq(0, 9)
            val rattleSounds = Seq("tiles-rattle-2", "tiles-rattle-1")
            (0 until 2).foreach(i => playSoundHighpass(rattleSounds(i), times(rattles(i)), rate=rates(rattles(i)), pan=pans(rattles(i)) * -1))
        }

        // short down
        {
            val bottom = 1.0f - (10f * 0.01f)
            playSoundHighpass("tiles-rattle-1", 15, pan = -0.5f, rate=bottom)
            playSoundHighpass("tiles-scratch-2", 15, pan = 0.5f, rate=bottom)
        }
        // longer up to start level
        // short up
        {
            val indices = (0 until 2)
            val scratchPlays = Seq(Seq.fill(3)("tiles-scratch-1")).flatten
            val overlaps = Seq.fill(3)(0.66f)
            val top = 1.0f
            val bottom = 1.0f - (10f * 0.01f)
            val rates = Melody.absolute(top, Seq.fill(2)((top - bottom) / 2f))
            val pans = Melody.absolute(-0.1f, Seq.fill(2)(0.1f / 2))

            val times = Melody.absolute(22.0f, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
            indices.foreach(i => playSoundHighpass(scratchPlays(i), times(i), rate=rates(i), pan = pans(i)))
            val rattles = Seq(0)
            val rattleSounds = Seq("tiles-rattle-1")
            (0 until 1).foreach(i => playSoundHighpass(rattleSounds(i), times(rattles(i)), rate=rates(rattles(i)), pan=pans(rattles(i)) * -1))
        }
        // This works. But I would like a more concentrated and shorter theme. More to the point.
    }

    def theme2(implicit player: MusicPlayer): Unit = {
        playSoundHighpass("tiles-rattle-1", 0, pan = 0.5f)
        playSoundHighpass("tiles-scratch-1", 0, pan = -0.5f)

        val start2 = Seq("tiles-rattle-2").map(soundPlays(_).duration(1.0f)).sum

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

    def main(args: Array[String]): Unit = {
        implicit val player: MusicPlayer = MusicPlayer()

        /*
        val receiver: OSCPortIn = new OSCPortIn(OSCPort.defaultSCOSCPort())
        val listener: OSCListener = new OSCListener() {
            def acceptMessage(time: Date, message: OSCMessage): Unit = 
                println(s"Received message at $time $message")
        }
        receiver.addListener("/done", listener)
        receiver.startListening()
*/ 
        player.startPlay()
        player.sendBundle(0, Seq(player.makeDumpOSC(true)))
        setupNodes(player)

        player.sendBundle(0, Seq(player.makeLoadDir("/Users/danielstahl/Documents/Projects/soundmining-modular/src/main/sc/synths")))
        player.sendBundle(0, Seq(player.makeAllocRead(0, s"${SOUND_BASE_DIR}tiles-rattle.flac")))
        player.sendBundle(0, Seq(player.makeAllocRead(1, s"${SOUND_BASE_DIR}tiles-scratch-1.flac")))
        
        //playTilesRattle()
        //playTilesScratch(9)
        //playTilesScratch(0)
        theme2

        Console.println("Print q to quit")
        val cmd = StdIn.readLine()
        Console.println(s"You typed $cmd, goodBye")
        player.stopPlay()
        //receiver.stopListening()
    }
    */
}
