package net.soundmining

import MusiqueConcrete3._

object MusiqueConcrete4 {
  
    def playClockSpring(start: Float = 0): Unit = {
        client.resetClock
        playSound("clock-spring-1", 0 + start)
        playSoundHighpass("clock-spring-1", 3 + start)
        playSoundLowpass("clock-spring-1", 6 + start)

        playSound("clock-spring-2", 9 + start)
        playSoundHighpass("clock-spring-2", 12 + start)
        playSoundLowpass("clock-spring-2", 15 + start)

        playSound("clock-spring-3", 18 + start)
        playSoundHighpass("clock-spring-3", 21 + start)
        playSoundLowpass("clock-spring-3", 24 + start)
    }

    def theme1(start: Double = 0): Unit = {
        client.resetClock
        playSound("clock-spring-2", 0, pan = -0.5f, highPass = soundPlays("clock-spring-2").highPass)
        playSound("clock-spring-1", 0, pan = 0.5f, lowPass = soundPlays("clock-spring-1").lowPass)
        // part of a development
        playSound("clock-spring-1", 0.6, pan = 0.2f, rate = 1.05, highPass = soundPlays("clock-spring-1").highPass)
    }

    def theme2(start: Double = 0): Unit = {
        client.resetClock

        playSound("clock-spring-1", 0, volume = 3f, rate = 0.5, pan = -0.5f, ringModulate = soundPlays("clock-spring-1").lowPass)
        playSound("clock-spring-1", 0.1, volume = 3f, rate = 0.5, pan = 0.5f, ringModulate = soundPlays("clock-spring-1").highPass)

    }
}
