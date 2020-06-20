package net.soundmining

import net.soundmining.Instrument.{setupNodes}
import scala.io.StdIn
import net.soundmining.Instrument.TAIL_ACTION
import net.soundmining.Utils.absoluteTimeToMillis
import net.soundmining.Instruments

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

        val playLeft = Instruments.left(Instruments.playBuffer(0, 0.5f, 0, 2))    
            .addAction(TAIL_ACTION)

        val playRight = Instruments.right(Instruments.playBuffer(0, 0.49f, 0, 2))
            .addAction(TAIL_ACTION)
        
        val expand = Instruments.expand(playLeft, playRight)
            .addAction(TAIL_ACTION)

        
        expand.getOutputBus.staticBus(0)
        val graph = expand.buildGraph(0, 4, expand.graph(Seq()))
        player.sendNew(absoluteTimeToMillis(0), graph)    

        

        Console.println("Print q to quit")
    
        val cmd = StdIn.readLine()
        Console.println(s"You typed $cmd, goodBye")
        player.stopPlay()
    }
}
