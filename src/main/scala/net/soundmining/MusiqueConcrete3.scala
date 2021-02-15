package net.soundmining

import net.soundmining.synth._
import SuperColliderClient._
import net.soundmining.modular.ModularSynth._
import net.soundmining.modular.ModularInstrument.ControlInstrument
import scala.annotation.switch
import net.soundmining.synth.SoundNote._
import net.soundmining.synth.SoundPlays._
import net.soundmining.synth.SoundPlay


/*
For ideas. Hav 3-4 "theames" that have more and more complex variants by
adding componentns. We then play the themes by removing more and less of the
compoments. play in order is one idea but not necessarily true for all. 
*/
object  MusiqueConcrete3 {
    implicit val client: SuperColliderClient = SuperColliderClient()
    //val SOUND_BASE_DIR = "/Users/danielstahl/Documents/Projects/musique-concrete-iii/sounds/"
    val SOUND_BASE_DIR = "/Users/danielstahl/Documents/Music/sounds/"
    val SYNTH_DIR = "/Users/danielstahl/Documents/Projects/soundmining-modular/src/main/sc/synths"
    
    val TILES_SCRATCH_FREQS = Seq(3440.32, 5898.29, 1241.66, 1669.74)
    val TILES_RATTLE_FREQS = Seq(3305.35, 5813.23, 6377.62)


    // 3305.35, 
    val sounds = Map(
        "tiles-rattle-1" -> SoundPlay(0, 0.35, 0.6, highPass = Some(5000), lowPass = Some(3500), spectrumFreqs = TILES_RATTLE_FREQS),
        "tiles-rattle-2" -> SoundPlay(0, 0.65, 1.2, highPass = Some(5000), lowPass = Some(3500), spectrumFreqs = TILES_RATTLE_FREQS),
        "tiles-rattle-3" -> SoundPlay(0, 1.25, 1.4, highPass = Some(5000), lowPass = Some(3500), spectrumFreqs = TILES_RATTLE_FREQS),
        "tiles-scratch-1" -> SoundPlay(1, 0.471, 0.874, highPass = Some(5625), lowPass = Some(3375), spectrumFreqs = TILES_SCRATCH_FREQS),
        "tiles-scratch-2" -> SoundPlay(1, 1.382, 2.247, highPass = Some(5625), lowPass = Some(3375), spectrumFreqs = TILES_SCRATCH_FREQS),
        "tiles-scratch-3" -> SoundPlay(1, 2.660, 3.638, highPass = Some(5625), lowPass = Some(3375), spectrumFreqs = TILES_SCRATCH_FREQS)
    )

    val soundPlays = SoundPlays(sounds, numberOfOutputBuses = 2)

    import soundPlays._

    def play(f: () => Unit): Unit = {
        client.resetClock
        f()
    }

    def playSoundLowpass(name: String, startTime: Double, volume: Double = 1.0f, rate: Double = 1.0f, pan: Double = 0.0f): Unit = {
        val soundPlay = soundPlays(name)
        val lowPassFreq = filterFreq(rate, soundPlay.lowPass.get)

        StereoSoundNote(bufNum = soundPlay.bufNum, volume = volume)
            .left(_.playLeft(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .right(_.playRight(soundPlay.start, soundPlay.end, rate, staticControl(volume)))
            .mixAudio(soundPlay.amp(volume))
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
            .mixAudio(soundPlay.amp(volume))
            .highPass(staticControl(highPassFreq))
            .pan(staticControl(pan))
            .play(startTime = startTime)
    }

    def playTilesRattle(start: Float = 0): Unit = {
        client.resetClock
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

    def playTilesScratch(start: Float = 0): Unit = {
        client.resetClock
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

    def theme1v1(start: Double = 0, 
                v1: Option[Double] = None, p1: Option[Double] = None, 
                v2: Option[Double] = None, p2: Option[Double] = None): Unit = {

        val volume1 = v1.getOrElse(1.0)
        val pans1 = p1.getOrElse(-0.5)
        playSound("tiles-scratch-1", start, volume = volume1, pan = pans1, highPass = soundPlays("tiles-scratch-1").highPass)
        val start2 = start + (soundPlays("tiles-rattle-2").duration(1.0))

        val scratchPlays = "tiles-scratch-1"
        val overlaps = 0.66f
        val rates = 1.0f
        val volume2 = v2.getOrElse(1.0)
        val pans2 = p2.getOrElse(0.1)
        val times = start2
        val rattleSounds = "tiles-rattle-1"
        playSound(rattleSounds, times, volume = volume2, rate=rates, pan=pans2, highPass = soundPlays(rattleSounds).highPass)
    }

    def theme1v2(start: Double = 0,
                v1: Option[(Double, Double)] = None, p1: Option[(Double, Double)] = None, 
                v2: Option[(Double, Double)] = None, p2: Option[(Double, Double)] = None): Unit = {

        val (volumeRattle1, volumeScratch1) = v1.getOrElse(1.0, 1.0)
        val (panRattle1, panScratch1) = p1.getOrElse(0.5, -0.5)
        playSound("tiles-rattle-1", start, volume = volumeRattle1, rate=1.01, pan = panRattle1, highPass = soundPlays("tiles-rattle-1").highPass)
        playSound("tiles-scratch-1", start, volume = volumeScratch1, rate=1.01, pan = panScratch1, highPass = soundPlays("tiles-scratch-1").highPass)

        val start2 = start + soundPlays("tiles-rattle-2").duration(1.0)

        val scratchPlays = "tiles-scratch-1"
        val overlaps = 0.66f
        val rates = 1.01f
        val (volumeScratch2, volumeTiles2) = v2.getOrElse(1.0, 1.0)
        val (panScratch2, panTiles2)  = p2.getOrElse(-0.1, 0.1)
        val times = start2
        playSound(scratchPlays, times, volume = volumeScratch2, rate=rates, pan = panScratch2, highPass = soundPlays(scratchPlays).highPass)
        val rattleSounds = "tiles-rattle-2"
        playSound(rattleSounds, times, volume = volumeTiles2, rate=rates, pan = panTiles2, highPass = soundPlays(rattleSounds).highPass)
    }

    def theme1v3(start: Double = 0,
                 v1: Option[(Double, Double)] = None, p1: Option[(Double, Double)] = None,
                 v2: Option[Seq[Double]] = None, p2: Option[Seq[Double]] = None,
                 v3: Option[Double] = None, p3: Option[Seq[Double]] = None): Unit = {

        val (volumeRattle1, volumeScratch1) = v1.getOrElse(1.0, 1.0)
        val (panRattle1, panScratch1) = p1.getOrElse(0.5, -0.5)
        playSound("tiles-rattle-1", start, volume = volumeRattle1, rate=1.02, pan = panRattle1, highPass = soundPlays("tiles-rattle-1").highPass)
        playSound("tiles-scratch-1", start, volume = volumeScratch1, rate=1.02, pan = panScratch1, highPass = soundPlays("tiles-scratch-1").highPass)

        val start2 = start + soundPlays("tiles-rattle-2").duration(1.0)

        val indices = (0 until 2)
        val scratchPlays = Seq(Seq.fill(1)("tiles-scratch-1"), Seq.fill(1)("tiles-scratch-2")).flatten
        val overlaps = Melody.absolute(0.5f, Seq.fill(2)((0.5f - 0.5f) / 2))
        val rates = Melody.absolute(1.02f, Seq.fill(2)(0.01f))
        val volumes2 = v2.getOrElse(Seq(1.0, 0.7))
        val pans2 = p2.getOrElse(Melody.absolute(-0.15f, Seq.fill(2)((0.15f / 2) * -1)))
        val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        indices.foreach(i => playSound(scratchPlays(i), times(i), volume=volumes2(i), rate=rates(i), pan = pans2(i), highPass = soundPlays(scratchPlays(i)).highPass))
        val rattles = 1
        val rattleSounds = "tiles-rattle-2"
        val rattlesVolume = v3.getOrElse(1.0)
        val rattlesPans = p3.getOrElse(pans2.map(_ * -1))

        playSound(rattleSounds, times(rattles), volume=rattlesVolume, rate=rates(rattles), pan=rattlesPans(rattles), highPass = soundPlays(rattleSounds).highPass)
    }

    def theme1v4(start: Double = 0,
                v1: Option[(Double, Double)] = None, p1: Option[(Double, Double)] = None,
                v2: Option[Seq[Double]] = None, p2: Option[Seq[Double]] = None,
                v3: Option[Seq[Double]] = None, p3: Option[Seq[Double]] = None): Unit = {

        val (volumeRattle1, volumeScratch1) = v1.getOrElse(1.0, 1.0)
        val (panRattle1, panScratch1) = p1.getOrElse(0.5, -0.5)
        playSound("tiles-rattle-1", start, volume = volumeRattle1, rate=1.03, pan = panRattle1, highPass = soundPlays("tiles-rattle-1").highPass)
        playSound("tiles-scratch-1", start, volume = volumeScratch1, rate=1.03, pan = panScratch1, highPass = soundPlays("tiles-scratch-1").highPass)

        val start2 = start + soundPlays("tiles-rattle-2").duration(1.0)

        val indices = (0 until 5)
        val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2"), Seq.fill(1)("tiles-scratch-1")).flatten
        val overlaps = Melody.absolute(0.66f, Seq.fill(5)((0.66f - 0.33f) / 5))
        val rates = Melody.absolute(1.03f, Seq.fill(5)(0.01f))
        val volumes2 = v2.getOrElse(Seq(1.0, 0.7, 0.8, 0.7, 0.6))
        val pans2 = p2.getOrElse(Melody.absolute(-0.2f, Seq.fill(5)((0.2f / 5) * -1)))
        val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        indices.foreach(i => playSound(scratchPlays(i), times(i), volume = volumes2(i), rate=rates(i), pan = pans2(i), highPass = soundPlays(scratchPlays(i)).highPass))
        val rattles = Seq(1, 4)
        val rattleSounds = Seq("tiles-rattle-1", "tiles-rattle-2")
        val rattlesVolume = v3.getOrElse(Seq(1.0, 1.0))
        val rattlesPans = p3.getOrElse(pans2.map(_ * -1))
        (0 until 2).foreach(i => playSound(rattleSounds(i), times(rattles(i)), volume = rattlesVolume(i), rate=rates(rattles(i)), pan=rattlesPans(i), highPass = soundPlays(rattleSounds(i)).highPass))
    }

    def theme1v5(start: Double = 0,
                v1: Option[(Double, Double)] = None, p1: Option[(Double, Double)] = None,
                v2: Option[Seq[Double]] = None, p2: Option[Seq[Double]] = None,
                v3: Option[Seq[Double]] = None, p3: Option[Seq[Double]] = None,
                v4: Option[Double] = None, p4: Option[Double] = None,
                v5: Option[Double] = None, p5: Option[Double] = None): Unit = {

        val (volumeRattle1, volumeScratch1) = v1.getOrElse(1.0, 1.0)
        val (panRattle1, panScratch1) = p1.getOrElse(0.5, -0.5)
        playSound("tiles-rattle-1", start, volume = volumeRattle1, rate=1.04, pan = panRattle1, highPass = soundPlays("tiles-rattle-1").highPass)
        playSound("tiles-scratch-1", start, volume = volumeScratch1, rate=1.04, pan = panScratch1, highPass = soundPlays("tiles-scratch-1").highPass)

        val start2 = start + soundPlays("tiles-rattle-2").duration(1.0)

        val indices = (0 until 5)
        val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2"), Seq.fill(1)("tiles-scratch-1")).flatten
        val overlaps = Melody.absolute(0.66f, Seq.fill(5)((0.66f - 0.33f) / 5))
        val rates = Melody.absolute(1.04f, Seq.fill(5)(0.01f))
        val volumes2 = v2.getOrElse(Seq(1.0, 0.7, 0.8, 0.7, 0.6))
        val pans2 = p2.getOrElse(Melody.absolute(-0.2f, Seq.fill(5)((0.2f / 5) * -1)))
        val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        indices.foreach(i => playSound(scratchPlays(i), times(i), volume = volumes2(i), rate=rates(i), pan = pans2(i), highPass = soundPlays(scratchPlays(i)).highPass))
        val rattles = Seq(1, 4)
        val rattleSounds = Seq("tiles-rattle-1", "tiles-rattle-2")
        val rattlesVolume = v3.getOrElse(Seq(1.0, 1.0))
        val rattlesPans = p3.getOrElse(pans2.map(_ * -1))
        (0 until 2).foreach(i => playSound(rattleSounds(i), times(rattles(i)), volume = rattlesVolume(i), rate=rates(rattles(i)), pan=rattlesPans(i), highPass = soundPlays(rattleSounds(i)).highPass))    
        
        val highScratch = 3
        val highScratchSounds = "tiles-scratch-2"
        val highScratchVolume = v4.getOrElse(0.7)
        val highScratchPan = p4.getOrElse(0.7)
        playSound(highScratchSounds, times(highScratch), volume=highScratchVolume, rate=rates(highScratch) * 1.9, pan=highScratchPan, highPass = soundPlays(highScratchSounds).highPass)

        val highRattles = 3
        val highRattleSounds = "tiles-rattle-2"
        val highRattleVolume = v5.getOrElse(1.0)
        val highRattlePan = p5.getOrElse(-0.7)
        playSound(highRattleSounds, times(highRattles), volume = highRattleVolume, rate=rates(highRattles) * 1.8, pan=highRattlePan, highPass = soundPlays(highRattleSounds).highPass)
    }

    def theme1v6(start: Double = 0,
                 v1: Option[(Double, Double)] = None, p1: Option[(Double, Double)] = None,
                 v2: Option[Seq[Double]] = None, p2: Option[Seq[Double]] = None,
                 v3: Option[Seq[Double]] = None, p3: Option[Seq[Double]] = None,
                 v4: Option[Seq[Double]] = None, p4: Option[Seq[Double]] = None,
                 v5: Option[Seq[Double]] = None, p5: Option[Seq[Double]] = None,
                 v6: Option[Double] = None, p6: Option[Double] = None,
                 v7: Option[Double] = None, p7: Option[Double] = None): Unit = {

        val (volumeRattle1, volumeScratch1) = v1.getOrElse(1.0, 1.0)
        val (panRattle1, panScratch1) = p1.getOrElse(0.5, -0.5)
        playSound("tiles-rattle-1", start, volume = volumeRattle1, rate=1.05, pan = panRattle1, highPass = soundPlays("tiles-rattle-1").highPass)
        playSound("tiles-scratch-1", start, volume = volumeScratch1, rate=1.05, pan = panScratch1, highPass = soundPlays("tiles-scratch-1").highPass)

        val start2 = start + soundPlays("tiles-rattle-2").duration(1.0)

        val indices = (0 until 5)
        val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2"), Seq.fill(1)("tiles-scratch-1")).flatten
        val overlaps = Melody.absolute(0.66f, Seq.fill(5)((0.66f - 0.33f) / 5))
        val rates = Melody.absolute(1.05f, Seq.fill(5)(0.01f))
        val volumes2 = v2.getOrElse(Seq(1.0, 0.7, 0.8, 0.7, 0.6))
        val pans2 = p2.getOrElse(Melody.absolute(-0.2f, Seq.fill(5)((0.2f / 5) * -1)))
        val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        indices.foreach(i => playSound(scratchPlays(i), times(i), volume = volumes2(i), rate=rates(i), pan = pans2(i), highPass = soundPlays(scratchPlays(i)).highPass))
        val rattles = Seq(1, 4)
        val rattleSounds = Seq("tiles-rattle-1", "tiles-rattle-2")
        val rattlesVolume = v3.getOrElse(Seq(1.0, 1.0))
        val rattlesPans = p3.getOrElse(pans2.map(_ * -1))
        (0 until 2).foreach(i => playSound(rattleSounds(i), times(rattles(i)), volume = rattlesVolume(i), rate=rates(rattles(i)), pan=rattlesPans(i), highPass = soundPlays(rattleSounds(i)).highPass))  
        
        val highScratch = Seq(0, 3)
        val highScratchSounds = Seq("tiles-scratch-2", "tiles-scratch-1")
        val highScratchVolumes = v4.getOrElse(Seq(0.7, 0.7))
        val highScratchPans = p4.getOrElse(Seq(0.7, 0.7))
        (0 until 2).foreach(i => playSound(highScratchSounds(i), times(highScratch(i)), volume=highScratchVolumes(i), rate=rates(highScratch(i)) * 1.9, pan=highScratchPans(i), highPass = soundPlays(highScratchSounds(i)).highPass))

        val highRattles = Seq(2, 4)
        val highRattleSounds = Seq("tiles-rattle-1", "tiles-rattle-2")
        val highRattlesVolumes = v5.getOrElse(Seq(1.0, 1.0))
        val highRattlesPans = p5.getOrElse(Seq(-0.7, -0.7))
        (0 until 2).foreach(i => playSound(highRattleSounds(i), times(highRattles(i)), volume=highRattlesVolumes(i), rate=rates(highRattles(i)) * 1.8, pan=highRattlesPans(i), highPass = soundPlays(highRattleSounds(i)).highPass))

        val lowScratch = 4
        val lowScratchSounds = "tiles-scratch-1"
        val lowScratchVolume = v6.getOrElse(0.5)
        val lowScratchPan = p6.getOrElse(0.8)
        playSound(lowScratchSounds, times(lowScratch), volume=lowScratchVolume, rate=rates(lowScratch) * 0.2, pan=lowScratchPan, lowPass = soundPlays(lowScratchSounds).lowPass)

        val lowRattles = 1
        val lowRattleSounds = "tiles-rattle-2"
        val lowRattleVolume = v7.getOrElse(0.7)
        val lowRattlePan = p7.getOrElse(-0.8)
        playSound(lowRattleSounds, times(lowRattles), volume=lowRattleVolume, rate=rates(lowRattles) * 0.1, pan=lowRattlePan, lowPass = soundPlays(lowScratchSounds).lowPass)
    }


    def theme1v7(start: Double = 0,
                 v1: Option[(Double, Double)] = None, p1: Option[(Double, Double)] = None,
                 v2: Option[Seq[Double]] = None, p2: Option[Seq[Double]] = None,
                 v3: Option[Seq[Double]] = None, p3: Option[Seq[Double]] = None,
                 v4: Option[Seq[Double]] = None, p4: Option[Seq[Double]] = None,
                 v5: Option[Seq[Double]] = None, p5: Option[Seq[Double]] = None,
                 v6: Option[Double] = None, p6: Option[Double] = None,
                 v7: Option[Double] = None, p7: Option[Double] = None): Unit = {

        val (volumeRattle1, volumeScratch1) = v1.getOrElse(1.0, 1.0)
        val (panRattle1, panScratch1) = p1.getOrElse(0.5, -0.5)
        playSound("tiles-rattle-1", start, volume = volumeRattle1, rate=1.05, pan = panRattle1, highPass = soundPlays("tiles-rattle-1").highPass)

        val start2 = start + soundPlays("tiles-rattle-2").duration(1.0)

        val indices = (0 until 5)
        val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2"), Seq.fill(1)("tiles-scratch-1")).flatten
        val overlaps = Melody.absolute(0.66f, Seq.fill(5)((0.66f - 0.33f) / 5))
        val rates = Melody.absolute(1.05f, Seq.fill(5)(0.01f))
        val volumes2 = v2.getOrElse(Seq(1.0, 0.7, 0.8, 0.7, 0.6))
        val pans2 = p2.getOrElse(Melody.absolute(-0.2f, Seq.fill(5)((0.2f / 5) * -1)))
        val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        
        val rattles = Seq(1, 4)
        val rattleSounds = Seq("tiles-rattle-1", "tiles-rattle-2")
        val rattlesVolume = v3.getOrElse(Seq(1.0, 1.0))
        val rattlesPans = p3.getOrElse(pans2.map(_ * -1))
        
        val highScratch = Seq(0, 3)
        val highScratchSounds = Seq("tiles-scratch-2", "tiles-scratch-1")
        val highScratchVolumes = v4.getOrElse(Seq(0.7, 0.7))
        val highScratchPans = p4.getOrElse(Seq(0.7, 0.7))
        (0 until 2).foreach(i => playSound(highScratchSounds(i), times(highScratch(i)), volume=highScratchVolumes(i), rate=rates(highScratch(i)) * 1.9, pan=highScratchPans(i), highPass = soundPlays(highScratchSounds(i)).highPass))

        val highRattles = Seq(2, 4)
        val highRattleSounds = Seq("tiles-rattle-1", "tiles-rattle-2")
        val highRattlesVolumes = v5.getOrElse(Seq(1.0, 1.0))
        val highRattlesPans = p5.getOrElse(Seq(-0.7, -0.7))
        (0 until 2).foreach(i => playSound(highRattleSounds(i), times(highRattles(i)), volume=highRattlesVolumes(i), rate=rates(highRattles(i)) * 1.8, pan=highRattlesPans(i), highPass = soundPlays(highRattleSounds(i)).highPass))

        val lowScratch = 4
        val lowScratchSounds = "tiles-scratch-1"
        val lowScratchVolume = v6.getOrElse(0.5)
        val lowScratchPan = p6.getOrElse(0.8)
        playSound(lowScratchSounds, times(lowScratch), volume=lowScratchVolume, rate=rates(lowScratch) * 0.2, pan=lowScratchPan, lowPass = soundPlays(lowScratchSounds).lowPass)

        val lowRattles = 1
        val lowRattleSounds = "tiles-rattle-2"
        val lowRattleVolume = v7.getOrElse(0.7)
        val lowRattlePan = p7.getOrElse(-0.8)
        playSound(lowRattleSounds, times(lowRattles), volume=lowRattleVolume, rate=rates(lowRattles) * 0.1, pan=lowRattlePan, lowPass = soundPlays(lowScratchSounds).lowPass)
    }

    def theme2v1(start: Double = 0, 
                v1: Option[Double] = None, p1: Option[Double] = None, 
                v2: Option[Double] = None, p2: Option[Double] = None): Unit = {
        val dur = MusiqueConcrete3.soundPlays("tiles-scratch-3").duration(1.0)
        playSound("tiles-rattle-3", start, volume=v1.getOrElse(2.0), pan = p1.getOrElse(0.5))
        playSound("tiles-scratch-3", start + (dur / 2), volume=v2.getOrElse(0.5), pan = p2.getOrElse(-0.5), lowPass = soundPlays("tiles-scratch-3").lowPass)
    }

    def theme2v2(start: Double = 0, 
                v1: Option[Double] = None, p1: Option[Double] = None, 
                v2: Option[Double] = None, p2: Option[Double] = None, 
                v3: Option[Double] = None, p3: Option[Double] = None): Unit = {
        val dur = MusiqueConcrete3.soundPlays("tiles-scratch-3").duration(1.0)
        playSound("tiles-rattle-3", start, volume=v1.getOrElse(2.0), pan = p1.getOrElse(0.5))
        playSound("tiles-scratch-3", start + (dur / 2), volume=v2.getOrElse(0.5), pan = p2.getOrElse(-0.5), lowPass = soundPlays("tiles-scratch-3").lowPass)
        playSound("tiles-rattle-3", start + dur, volume=v3.getOrElse(2.0), pan = p3.getOrElse(0.3))
    }

    def theme2v3(start: Double = 0, 
                v1: Option[Double] = None, p1: Option[Double] = None,
                v2: Option[Double] = None, p2: Option[Double] = None, 
                v3: Option[Double] = None, p3: Option[Double] = None,
                v4: Option[Double] = None, p4: Option[Double] = None, r4: Option[Double] = None): Unit = {
        val dur = MusiqueConcrete3.soundPlays("tiles-scratch-3").duration(1.0)            
        playSound("tiles-rattle-3", start, volume=v1.getOrElse(2.0), pan = p1.getOrElse(0.5))
        playSound("tiles-scratch-3", start + (dur / 2), volume=v2.getOrElse(0.5), pan = p2.getOrElse(-0.5), lowPass = soundPlays("tiles-scratch-3").lowPass)
        playSound("tiles-rattle-3", start + dur, volume=v3.getOrElse(2.0), pan = p3.getOrElse(0.3))
        playSound("tiles-scratch-3", start + dur, volume=v4.getOrElse(0.5), rate=r4.getOrElse(1.8), pan = p4.getOrElse(0.5), lowPass = soundPlays("tiles-scratch-3").lowPass)
    }

    def theme2v4(start: Double = 0, 
                v3: Option[Double] = None, p3: Option[Double] = None,
                v4: Option[Double] = None, p4: Option[Double] = None, r4: Option[Double] = None): Unit = {
        playSound("tiles-rattle-3", start + 0, volume=v3.getOrElse(2.0), pan = p3.getOrElse(0.3))
        playSound("tiles-scratch-3", start + 0, volume=v4.getOrElse(0.5), rate=r4.getOrElse(1.8), pan = p4.getOrElse(0.5), lowPass = soundPlays("tiles-scratch-3").lowPass)
    }

    def theme4v1(start: Double = 0,
                v1: Option[(Double, Double)] = None, p1: Option[(Double, Double)] = None,
                v2: Option[Seq[Double]] = None, p2: Option[Seq[Double]] = None,
                v3: Option[Seq[Double]] = None, p3: Option[Seq[Double]] = None): Unit = {

        val start2 = start

        val indices = (0 until 5)
        val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2"), Seq.fill(1)("tiles-scratch-1")).flatten
        val overlaps = Melody.absolute(0.33f, Seq.fill(5)((0.66f - 0.33f) / 5))
        val rates = Melody.absolute(0.1f, Seq.fill(5)(-0.01f))
        val volumes2 = v2.getOrElse(Seq(0.6, 0.7, 0.8, 0.7, 0.6))
        val pans2 = p2.getOrElse(Melody.absolute(-0.5f, Seq.fill(5)(1.0 / 5.0)))
        val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        indices.foreach(i => playSound(scratchPlays(i), times(i), volume = volumes2(i), rate=rates(i), pan = pans2(i), lowPass = soundPlays(scratchPlays(i)).lowPass))
    }

    def theme5v1(start: Double = 0, 
                v2: Option[Double] = None, p2: Option[Double] = None, r2: Option[Double] = None, 
                v4: Option[Double] = None, p4: Option[Double] = None, r4: Option[Double] = None): Unit = {
        val dur = MusiqueConcrete3.soundPlays("tiles-scratch-3").duration(0.05) / 5        
        playSound("tiles-scratch-3", start, volume=v2.getOrElse(0.5), rate=r2.getOrElse(0.05), pan = p2.getOrElse(-0.5), highPass = soundPlays("tiles-scratch-3").highPass)
        playSound("tiles-scratch-3", start + dur, volume=v4.getOrElse(0.5), rate=r4.getOrElse(0.06), pan = p4.getOrElse(0.5), highPass = soundPlays("tiles-scratch-3").highPass)
    }

    def theme6v1(start: Double = 0,
                v1: Option[(Double, Double)] = None, p1: Option[(Double, Double)] = None,
                v2: Option[Seq[Double]] = None, p2: Option[Seq[Double]] = None,
                v3: Option[Seq[Double]] = None, p3: Option[Seq[Double]] = None,
                rs: Option[Double] = None, rc: Option[Double] = None): Unit = {

        val start2 = start

        val indices = (0 until 5)
        val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2"), Seq.fill(1)("tiles-scratch-1")).flatten
        val overlaps = Melody.absolute(0.33f, Seq.fill(5)((0.66f - 0.33f) / 5))
        val rateStart = rs.getOrElse(1.8)
        val rateChange = rc.getOrElse(-0.03)
        val rates = Melody.absolute(rateStart, Seq.fill(5)(rateChange))
        val volumes2 = v2.getOrElse(Seq(0.6, 0.7, 0.8, 0.7, 0.6))
        val pans2 = p2.getOrElse(Melody.absolute(-0.5f, Seq.fill(5)(1.0 / 5.0)))
        val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        indices.foreach(i => playSound(scratchPlays(i), times(i), volume = volumes2(i), rate=rates(i), pan = pans2(i), lowPass = soundPlays(scratchPlays(i)).lowPass))
    }

    def theme6v2(start: Double = 0,
                v1: Option[(Double, Double)] = None, p1: Option[(Double, Double)] = None,
                v2: Option[Seq[Double]] = None, p2: Option[Seq[Double]] = None,
                v3: Option[Seq[Double]] = None, p3: Option[Seq[Double]] = None,
                rs: Option[Double] = None, rc: Option[Double] = None): Unit = {

        val start2 = start

        val indices = (0 until 5)
        val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2"), Seq.fill(1)("tiles-scratch-1")).flatten
        val overlaps = Melody.absolute(0.33f, Seq.fill(5)((0.66f - 0.33f) / 5))
        val rateStart = rs.getOrElse(1.7)
        val rateChange = rc.getOrElse(0.03)
        val rates = Melody.absolute(rateStart, Seq.fill(5)(rateChange))
        val volumes2 = v2.getOrElse(Seq(0.6, 0.7, 0.8, 0.7, 0.6))
        val pans2 = p2.getOrElse(Melody.absolute(-0.5f, Seq.fill(5)(1.0 / 5.0)))
        val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        indices.foreach(i => playSound(scratchPlays(i), times(i), volume = volumes2(i), rate=rates(i), pan = pans2(i), lowPass = soundPlays(scratchPlays(i)).lowPass))
    }


    def theme6v3(start: Double = 0,
                v1: Option[Double] = None, p1: Option[Double] = None, r1: Option[Double] = None,
                v2: Option[Seq[Double]] = None, p2: Option[Seq[Double]] = None,
                v3: Option[Seq[Double]] = None, p3: Option[Seq[Double]] = None,
                rs: Option[Double] = None, rc: Option[Double] = None): Unit = {

        val rate1 = r1.getOrElse(0.5)
        val volume1 = v1.getOrElse(1.0)
        val pans1 = p1.getOrElse(-0.5)
        playSound("tiles-rattle-1", start, volume = volume1, rate = rate1, pan = pans1, lowPass = soundPlays("tiles-rattle-1").lowPass)
        val start2 = start + (soundPlays("tiles-rattle-2").duration(1))

        val indices = (0 until 5)
        val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2"), Seq.fill(1)("tiles-scratch-1")).flatten
        val overlaps = Melody.absolute(0.33f, Seq.fill(5)((0.66f - 0.33f) / 5))
        val rateStart = rs.getOrElse(1.8)
        val rateChange = rc.getOrElse(-0.05)
        val rates = Melody.absolute(rateStart, Seq.fill(5)(rateChange))
        val volumes2 = v2.getOrElse(Seq(0.6, 0.7, 0.8, 0.7, 0.6))
        val pans2 = p2.getOrElse(Melody.absolute(-0.5f, Seq.fill(5)(1.0 / 5.0)))
        val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        indices.foreach(i => playSound(scratchPlays(i), times(i), volume = volumes2(i), rate=rates(i), pan = pans2(i), lowPass = soundPlays(scratchPlays(i)).lowPass))
    }

    def theme6v4(start: Double = 0,
                v1: Option[(Double, Double)] = None, p1: Option[(Double, Double)] = None,
                v2: Option[Seq[Double]] = None, p2: Option[Seq[Double]] = None,
                v3: Option[Seq[Double]] = None, p3: Option[Seq[Double]] = None,
                rs: Option[Double] = None, rc: Option[Double] = None): Unit = {

        val start2 = start

        val indices = (0 until 5)
        val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2"), Seq.fill(1)("tiles-scratch-1")).flatten
        val overlaps = Melody.absolute(0.33f, Seq.fill(5)((0.66f - 0.33f) / 5))
        val rateStart = rs.getOrElse(1.8)
        val rateChange = rc.getOrElse(-0.03)
        val rates = Melody.absolute(rateStart, Seq.fill(5)(rateChange))
        val volumes2 = v2.getOrElse(Seq(0.6, 0.7, 0.8, 0.7, 0.6))
        val pans2 = p2.getOrElse(Melody.absolute(-0.5f, Seq.fill(5)(1.0 / 5.0)))
        val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        indices.foreach(i => playSound(scratchPlays(i), times(i), volume = volumes2(i), rate=rates(i), pan = pans2(i), lowPass = soundPlays(scratchPlays(i)).lowPass))

        val rateStart2 = rs.getOrElse(1.7)
        val rateChange2 = rc.getOrElse(0.05)
        val rates2 = Melody.absolute(rateStart2, Seq.fill(5)(rateChange2))
        val start3 = start2 + (soundPlays("tiles-rattle-2").duration(1.7))
        val times2 = Melody.absolute(start3, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))

        val pans3 = pans2.map(_ * -1.0)
        indices.foreach(i => playSound(scratchPlays(i), times2(i), volume = volumes2(i), rate=rates2(i), pan = pans3(i), highPass = soundPlays(scratchPlays(i)).highPass))
    }

    def theme6v5(start: Double = 0,
                v0: Option[Double] = None, r0: Option[Double] = None, p0: Option[Double] = None,
                v1: Option[(Double, Double)] = None, p1: Option[(Double, Double)] = None, 
                v2: Option[Seq[Double]] = None, p2: Option[Seq[Double]] = None,
                v3: Option[Seq[Double]] = None, p3: Option[Seq[Double]] = None,
                rs: Option[Double] = None, rc: Option[Double] = None): Unit = {

        val rate1 = r0.getOrElse(0.5)
        val volume1 = v0.getOrElse(1.0)
        val pans1 = p0.getOrElse(-0.5)
        playSound("tiles-rattle-1", start, volume = volume1, rate = rate1, pan = pans1, lowPass = soundPlays("tiles-scratch-1").lowPass)
        val start2 = start + (soundPlays("tiles-rattle-2").duration(1.0))

        val indices = (0 until 5)
        val scratchPlays = Seq(Seq.fill(2)("tiles-scratch-1"), Seq.fill(2)("tiles-scratch-2"), Seq.fill(1)("tiles-scratch-1")).flatten
        val overlaps = Melody.absolute(0.33f, Seq.fill(5)((0.66f - 0.33f) / 5))
        val rateStart = rs.getOrElse(1.7)
        val rateChange = rc.getOrElse(0.05)
        val rates = Melody.absolute(rateStart, Seq.fill(5)(rateChange))
        val volumes2 = v2.getOrElse(Seq(0.6, 0.7, 0.8, 0.7, 0.6))
        val pans2 = p2.getOrElse(Melody.absolute(-0.5f, Seq.fill(5)(1.0 / 5.0)))
        val times = Melody.absolute(start2, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))
        indices.foreach(i => playSound(scratchPlays(i), times(i), volume = volumes2(i), rate=rates(i), pan = pans2(i), lowPass = soundPlays(scratchPlays(i)).lowPass))

        val rateStart2 = rs.getOrElse(1.8)
        val rateChange2 = rc.getOrElse(-0.03)
        val rates2 = Melody.absolute(rateStart2, Seq.fill(5)(rateChange2))
        val start3 = start2 + (soundPlays("tiles-rattle-2").duration(1.8))
        val times2 = Melody.absolute(start3, indices.map(i => soundPlays(scratchPlays(i)).duration(rates(i)) * overlaps(i)))

        val pans3 = pans2.map(_ * -1.0)
        indices.foreach(i => playSound(scratchPlays(i), times2(i), volume = volumes2(i), rate=rates2(i), pan = pans3(i), highPass = soundPlays(scratchPlays(i)).highPass))

    }

    def theme7v1(start: Double = 0, 
                v2: Option[Double] = None, p2: Option[Double] = None, r2: Option[Double] = None, 
                v4: Option[Double] = None, p4: Option[Double] = None, r4: Option[Double] = None): Unit = {
                  
        val timeOne = MusiqueConcrete3.soundPlays("tiles-scratch-3").duration(1.0)
        val timeTwo = timeOne * 2
        val timeThree = timeOne * 3
        val timeFive = timeOne * 5

        val times = Melody.absolute(start, Seq(timeThree, timeThree, timeTwo, timeFive, timeThree, timeTwo, timeTwo, timeFive))

        playSound("tiles-scratch-3", times(0), volume=v2.getOrElse(0.5), rate=r2.getOrElse(0.10), pan = p2.getOrElse(-0.5), lowPass = soundPlays("tiles-scratch-3").lowPass)
        playSound("tiles-scratch-3", times(1), volume=v4.getOrElse(0.5), rate=r4.getOrElse(0.12), pan = p4.getOrElse(0.5), lowPass = soundPlays("tiles-scratch-3").lowPass)

        playSound("tiles-scratch-3", times(2), volume=v2.getOrElse(0.5), rate=r2.getOrElse(0.2), pan = p2.getOrElse(0.8), highPass = soundPlays("tiles-scratch-3").highPass)
        playSound("tiles-scratch-3", times(3), volume=v4.getOrElse(0.5), rate=r4.getOrElse(0.25), pan = p4.getOrElse(-0.8), highPass = soundPlays("tiles-scratch-3").highPass)

        playSound("tiles-scratch-3", times(4), volume=v2.getOrElse(0.5), rate=r2.getOrElse(0.15), pan = p2.getOrElse(-0.3), lowPass = soundPlays("tiles-scratch-3").lowPass)
        playSound("tiles-scratch-3", times(5), volume=v4.getOrElse(0.5), rate=r4.getOrElse(0.18), pan = p4.getOrElse(0.3), lowPass = soundPlays("tiles-scratch-3").lowPass)

        playSound("tiles-scratch-3", times(6), volume=v2.getOrElse(0.5), rate=r2.getOrElse(0.29), pan = p2.getOrElse(0.4), highPass = soundPlays("tiles-scratch-3").highPass)
        playSound("tiles-scratch-3", times(7), volume=v4.getOrElse(0.5), rate=r4.getOrElse(0.35), pan = p4.getOrElse(-0.4), highPass = soundPlays("tiles-scratch-3").highPass)
    }

    def play1v1(): Unit = {
        val shortDur = soundPlays("tiles-rattle-2").duration(1) * 8
        val longDur = soundPlays("tiles-rattle-2").duration(1) * 13

        val times = Melody.absolute(0, Seq(shortDur, shortDur, longDur, shortDur, shortDur, longDur, shortDur))
        
        client.resetClock

        theme4v1(start = longDur)

        theme1v1(start = times(0))
        theme1v2(start = times(1))
        theme1v3(start = times(2))
        theme1v4(start = times(3))
        theme1v3(start = times(4))
        theme1v2(start = times(5))
        theme1v1(start = times(6))
    }

    def play1v2(): Unit = {
        client.resetClock

        val dur = MusiqueConcrete3.soundPlays("tiles-scratch-3").duration(1.0) * 5
        val shortDur = MusiqueConcrete3.soundPlays("tiles-scratch-3").duration(1.0) * 3

        val times = Melody.absolute(0, Seq(dur, dur, dur, dur))

        theme5v1(start = shortDur)
        
        theme2v1(start = times(0))
        theme2v2(start = times(1))
        theme2v3(start = times(2))
        theme2v4(start = times(3))
    }

    def play1v3(): Unit = {
        client.resetClock

        val shortDur = soundPlays("tiles-rattle-2").duration(1) * 8
        val longDur = soundPlays("tiles-rattle-2").duration(1) * 13

        val times = Melody.absolute(0, Seq(shortDur, shortDur, shortDur, shortDur,  shortDur))

        theme6v1(start = times(0))
        theme6v2(start = times(1))
        theme6v3(start = times(2))
        theme6v4(start = times(3))
        theme6v5(start = times(4))
    }

    def play1v4(): Unit = {
        client.resetClock

        theme7v1(start = 0)
    }

    def play1v5(): Unit = {
        client.resetClock

        val shortDur = soundPlays("tiles-rattle-2").duration(1) * 8
        val longDur = soundPlays("tiles-rattle-2").duration(1) * 13

        val times = Melody.absolute(0, Seq(shortDur, shortDur, longDur, shortDur))

        theme1v4(start = times(0))
        theme1v3(start = times(1))
        theme1v2(start = times(2))
        theme1v1(start = times(3))
    }

    def init(): Unit = {
        println("Starting up SupderCollider client")
        client.start
        Instrument.setupNodes(client)
        client.send(loadDir(SYNTH_DIR))

        client.send(allocRead(0, s"${SOUND_BASE_DIR}tiles-rattle.flac"))
        client.send(allocRead(1, s"${SOUND_BASE_DIR}tiles-scratch-1.flac"))
        client.send(allocRead(2, s"${SOUND_BASE_DIR}clock-spring-1.flac"))

        // Look at the synth def here https://github.com/supercollider/supercollider/blob/3.11/SCClassLibrary/Common/GUI/tools/ServerMeter.sc
        // https://www.scala-lang.org/api/current/scala/Console$.html
    }

    def stop(): Unit = {
        println("Stopping Supercollider client")
        client.stop
    }
}
