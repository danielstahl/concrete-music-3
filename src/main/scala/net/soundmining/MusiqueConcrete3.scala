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

object MusiqueConcrete3 {
  
    val SOUND_BASE_DIR = "/Users/danielstahl/Documents/Projects/musique-concrete-iii/sounds/"

    case class SoundPlay(bufNum: Integer, start: Float, end: Float) 

    val soundPlays = Map(
        "short" -> SoundPlay(0, 0.450f, 0.550f),
        "long" -> SoundPlay(0, 0.55f, 1.30f)
    )
    
    // Set up the instruments like we did in Modular3. 
    // That way we can choose what and how to combine. 
    // have a var audio: Option[AudioInstrument]
    // https://github.com/danielstahl/arpeggio-i/blob/master/src/main/scala/net/soundmining/Modular3.scala
    // We want to test to only take the left channel and pan it to stereo to the left. Same thing with right. 
    // And also filter, for instance, the left or the right channel

    def testFilter()(implicit player: MusicPlayer) {
        val start = 0.4f
        val end = 2.0f
        val rate = 0.5f
        val dur = math.abs((end - start) / rate)

        val playLeft = lowPassFilter(
                left(playBuffer(0, rate, start, end, staticControl(1f)).withNrOfChannels(2), staticControl(1f)).addAction(TAIL_ACTION), 
                staticControl(200))
            .addAction(TAIL_ACTION)

        val playRight = highPassFilter(
                right(playBuffer(0, rate + 0.01f, start, end, staticControl(1.0f)).withNrOfChannels(2), staticControl(1f)).addAction(TAIL_ACTION), 
                staticControl(1000))
            .addAction(TAIL_ACTION)
        
        val expanded = expand(playLeft, playRight)
            .addAction(TAIL_ACTION)

        
        expanded.getOutputBus.staticBus(0)
        val graph = expanded.buildGraph(0, dur, expanded.graph(Seq()))
        player.sendNew(absoluteTimeToMillis(0), graph)
    }

    def testShort(startTime: Float = 0f, rate: Float = 1.0f, amp: Float = 1.0f)(implicit player: MusicPlayer) {
        val start = 0.450f
        val end = 0.550f
        val dur = math.abs((end - start) / rate)

        val playLeft = left(playBuffer(0, rate, start, end, staticControl(amp)).withNrOfChannels(2), relativePercControl(0, amp, 0.1f, Right(SINE)))
            .addAction(TAIL_ACTION)

        val playRight = right(playBuffer(0, rate - 0.01f, start, end, staticControl(amp)).withNrOfChannels(2), relativePercControl(0, amp, 0.1f, Right(SINE)))
            .addAction(TAIL_ACTION)
        
        val expanded = expand(playLeft, playRight)
            .addAction(TAIL_ACTION)

        
        expanded.getOutputBus.staticBus(0)
        val graph = expanded.buildGraph(startTime, dur, expanded.graph(Seq()))
        player.sendNew(absoluteTimeToMillis(startTime), graph)
    }

    def shortNote(startTime: Float = 0f, rate: Float = 1.0f, amp: Float = 1.0f, rateDiff: Float = 0.01f)(implicit player: MusicPlayer) {
        val start = 0.450f
        val end = 0.550f
        StereoSoundNote(bufNum = 0, volume = 1.0f)
            .left(_.playLeft(start, end, rate + rateDiff, relativePercControl(0, amp, 0.1f, Right(EXPONENTIAL))))
            .right(_.playRight(start, end, rate - rateDiff, relativePercControl(0, amp, 0.1f, Right(EXPONENTIAL))))
            .expandAudio()
            .play(startTime = startTime)
    }

    def testLong(startTime: Float = 0f, rate: Float = 1.0f, amp: Float = 1.0f)(implicit player: MusicPlayer) {
        val start = 0.55f
        val end = 1.30f
        val dur = math.abs((end - start) / rate)

        val playLeft = left(playBuffer(0, rate, start, end, staticControl(amp)).withNrOfChannels(2), relativePercControl(0, amp, 0.5f, Right(SINE)))
            .addAction(TAIL_ACTION)

        val playRight = right(playBuffer(0, rate - 0.01f, start, end, staticControl(amp)).withNrOfChannels(2), relativePercControl(0, amp, 0.5f, Right(SINE)))
            .addAction(TAIL_ACTION)
        
        val expanded = expand(playLeft, playRight)
            .addAction(TAIL_ACTION)

        
        expanded.getOutputBus.staticBus(0)
        val graph = expanded.buildGraph(startTime, dur, expanded.graph(Seq()))
        player.sendNew(absoluteTimeToMillis(startTime), graph)
    }

    def longNote(startTime: Float = 0f, rate: Float = 1.0f, amp: Float = 1.0f, rateDiff: Float = 0.01f)(implicit player: MusicPlayer) {
        val start = 0.55f
        val end = 1.30f
        StereoSoundNote(bufNum = 0, volume = 1.0f)
            .left(_.playLeft(start, end, rate + rateDiff, relativePercControl(0, amp, 0.5f, Right(SINE))))
            .right(_.playRight(start, end, rate - rateDiff, relativePercControl(0, amp, 0.5f, Right(SINE))))
            .expandAudio()
            .play(startTime = startTime)
    }

    /**
      * Guitar harmonics 1 harmonics
      * major: 481.383
      * down 1: 211.851
      * down 2: 399.366
      * up 1: 571.586
      * up 2: 714.072
      * up 3: 888.232
      */
    def main(args: Array[String]): Unit = {
        implicit val player: MusicPlayer = MusicPlayer()
        player.startPlay()
        setupNodes(player)

        player.sendBundle(0, Seq(player.makeAllocRead(0, s"${SOUND_BASE_DIR}guitar-harmonics-1.flac")))
        player.sendBundle(0, Seq(player.makeAllocRead(1, s"${SOUND_BASE_DIR}guitar-bridge-1.flac")))
        player.sendBundle(0, Seq(player.makeAllocRead(2, s"${SOUND_BASE_DIR}guitar-bridge-2.flac")))

        println("Hello world")

        //testFilter()
        /*testLong(0f, 2.2f, 2)
        testLong(0.05f, 1.9f, 2)
        testLong(0.15f, 1.6f, 2)

        testLong(0.5f, 0.5f, 0.7f)
        testLong(1.05f, 0.6f, 0.5f)
*/
        //testLong(0, 481.383f / 211.851f, 2)
        //testLong(0.5f, 481.383f / 571.586f, 0.5f)
        //testLong(1.5f, 1f, 0.7f)
        /*
        val playLeft = highPassFilter(left(playBuffer(0, 0.5f, 0, 2, staticControl(1f)), staticControl(1f)), staticControl(2000))
            .addAction(TAIL_ACTION)

        val playRight = highPassFilter(right(playBuffer(0, 0.49f, 0, 2, staticControl(1.0f)), staticControl(1f)), staticControl(2000))
            .addAction(TAIL_ACTION)
        
        val expanded = expand(playLeft, playRight)
            .addAction(TAIL_ACTION)

        
        expanded.getOutputBus.staticBus(0)
        val graph = expanded.buildGraph(0, 4, expanded.graph(Seq()))
        player.sendNew(absoluteTimeToMillis(0), graph)
        */
/*
        val firstRate = 571.586f / 481.383f
        val secondRate = 211.851f / 481.383f

*/
        //testShort(startTime = 0f, rate = 1.0f)
        //testLong(startTime = 0f, rate = 1.0f)

        //shortNote(startTime = 0f, rate = 1.0f, rateDiff = 0.01f)
        //longNote(startTime = 0f, rate = 1.0f, rateDiff = 0.01f)


        val start = 0.55f
        val end = 1.30f
        val rate = 1f
        val amp = 1f
      
        val firstRate = 571.586f / 481.383f // 1.187383
        val secondRate = 211.851f / 481.383f // 0.44008824
        println(firstRate)
        println(secondRate)
/*
        soundPlays("short") match {
            case SoundPlay(bufNum, start, end) =>
                StereoSoundNote(bufNum = bufNum, volume = 1.0f)
                    .left(_.playLeft(start, end, 1.0f - 0.01f, staticControl(1.0f)))
                    .right(_.playRight(start, end, 1.0f - 0.01f, staticControl(1.0f)))
                    .mixAudio(relativePercControl(0.001f, 1.0f, 0.5f, Right(SINE)))
                    .ring(staticControl(211.851f))
                    .pan(lineControl(0.5f, 0.9f))
                    .play(startTime = 0)
                StereoSoundNote(bufNum = bufNum, volume = 1.0f)
                    .left(_.playLeft(start, end, 1.0f - 0.01f, staticControl(1.0f)))
                    .right(_.playRight(start, end, 1.0f - 0.01f, staticControl(1.0f)))
                    .mixAudio(relativePercControl(0.001f, 1.0f, 0.5f, Right(SINE)))
                    .ring(staticControl(571.586f))
                    .pan(lineControl(-0.5f, -0.9f))
                    .play(startTime = 0.03f)    
        }
        
*/

        StereoSoundNote(bufNum = 0, volume = 1.0f)
            .left(_.playLeft(start, end, rate - 0.01f, staticControl(1.0f)))
            .right(_.playRight(start, end, rate - 0.01f, staticControl(1.0f)))
            .mixAudio(relativePercControl(0.001f, 1.0f, 0.5f, Right(SINE)))
            .pan(lineControl(0.5f, 0.9f))
            .play(startTime = 0)

        StereoSoundNote(bufNum = 0, volume = 1.0f)
            .left(_.playLeft(start, end, rate + 0.01f, staticControl(1.0f)))
            .right(_.playRight(start, end, rate + 0.01f, staticControl(1.0f)))
            .mixAudio(relativePercControl(0.001f, 1.0f, 0.5f, Right(SINE)))
            .pan(lineControl(-0.5f, -0.9f))
            .play(startTime = 0.15f)    
            
        longNote(startTime = 0.55f, rate = firstRate, rateDiff = 0.01f)

        StereoSoundNote(bufNum = 0, volume = 1.0f)
            .left(_.playLeft(start, end, secondRate - 0.01f, staticControl(1.0f)))
            .right(_.playRight(start, end, secondRate - 0.01f, staticControl(1.0f)))
            .mixAudio(relativePercControl(0.001f, 1.0f, 0.5f, Right(SINE)))
            .pan(lineControl(0.5f, 0.9f))
            .play(startTime = 2)

        StereoSoundNote(bufNum = 0, volume = 1.0f)
            .left(_.playLeft(start, end, secondRate + 0.01f, staticControl(1.0f)))
            .right(_.playRight(start, end, secondRate + 0.01f, staticControl(1.0f)))
            .mixAudio(relativePercControl(0.001f, 1.0f, 0.5f, Right(SINE)))
            .pan(lineControl(-0.5f, -0.9f))
            .play(startTime = 2.25f)    

        longNote(startTime = 3.0f, rate = 1.0f, rateDiff = 0.01f)

        Console.println("Print q to quit")
    
        val cmd = StdIn.readLine()
        Console.println(s"You typed $cmd, goodBye")
        player.stopPlay()
    }
}
