package net.soundmining

import net.soundmining.Instrument.{setupNodes}
import scala.io.StdIn
import net.soundmining.Instrument.TAIL_ACTION
import net.soundmining.Utils.absoluteTimeToMillis
import net.soundmining.modular.Instruments._

object MusiqueConcrete3 {
  
    val SOUND_BASE_DIR = "/Users/danielstahl/Documents/Projects/musique-concrete-iii/sounds/"

    def main(args: Array[String]): Unit = {
        implicit val player: MusicPlayer = MusicPlayer()
        player.startPlay()
        setupNodes(player)

        player.sendBundle(0, Seq(player.makeAllocRead(0, s"${SOUND_BASE_DIR}guitar-harmonics-1.flac")))
        player.sendBundle(0, Seq(player.makeAllocRead(1, s"${SOUND_BASE_DIR}guitar-bridge-1.flac")))
        player.sendBundle(0, Seq(player.makeAllocRead(2, s"${SOUND_BASE_DIR}guitar-bridge-2.flac")))

        println("Hello world")

        
        val playLeft = highPassFilter(left(playBuffer(0, 0.5f, 0, 2, staticControl(1f)), staticControl(1f)), staticControl(2000))
            .addAction(TAIL_ACTION)

        val playRight = highPassFilter(right(playBuffer(0, 0.49f, 0, 2, staticControl(1.0f)), staticControl(1f)), staticControl(2000))
            .addAction(TAIL_ACTION)
        
        val expanded = expand(playLeft, playRight)
            .addAction(TAIL_ACTION)

        
        expanded.getOutputBus.staticBus(0)
        val graph = expanded.buildGraph(0, 4, expanded.graph(Seq()))
        player.sendNew(absoluteTimeToMillis(0), graph)
        

        Console.println("Print q to quit")
    
        val cmd = StdIn.readLine()
        Console.println(s"You typed $cmd, goodBye")
        player.stopPlay()
    }
}
