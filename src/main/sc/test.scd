(
s = Server.local;
s.options.outDevice = "Built-in Output";
//s.options.outDevice = "Scarlett 2i2 USB";
//s.options.outDevice = "MOMENTUM M2 AEBT";
//s.options.outDevice = "Soundflower (2ch)";
//s.options.outDevice = "Soundflower Aggregate";
//s.options.numOutputBusChannels = 66;
s.options.blockSize = 128;
s.options.sampleRate = 96000;
s.options.numBuffers = 2056;
s.options.memSize = 2.pow(20);
s.dumpOSC(1);
)

(
b = Buffer.read(s, "/Users/danielstahl/Documents/Projects/musique-concrete-iii/sounds/guitar-harmonics-1.flac");
c = Buffer.read(s, "/Users/danielstahl/Documents/Projects/musique-concrete-iii/sounds/guitar-bridge-1.flac");
d = Buffer.read(s, "/Users/danielstahl/Documents/Projects/musique-concrete-iii/sounds/guitar-bridge-2.flac");
)

b.play;
c.play;
d.play;


{ BufRd.ar(2, b, Line.ar(0, BufFrames.kr(b) - 1, BufDur.kr(b))) }.play;


{ BufRd.ar(2, b, Phasor.ar(0, BufRateScale.kr(b) * 1, 0, BufFrames.kr(b) - 1)) }.play;
{ BufRd.ar(2, b, Phasor.ar(0, BufRateScale.kr(b) * 1.5, 0, BufFrames.kr(b) - 1)) }.play;

{ BufRd.ar(2, b, Phasor.ar(0, BufRateScale.kr(b) * 0.5, 30000, BufFrames.kr(b) - 100000)) }.play;

{ BufRd.ar(2, b, Phasor.ar(0, BufRateScale.kr(b) * 0.1, 22000, 23000)) }.play;

{ BufRd.ar(2, b, Line.ar(0, BufFrames.kr(b) - 1, BufDur.kr(b))) }.play;

{ BufRd.ar(2, b, Line.ar(0, BufFrames.kr(b) - 1, BufDur.kr(b) * 2)) }.play;
{ BufRd.ar(2, b, Line.ar(BufFrames.kr(b) - 1, 0, BufDur.kr(b) * 0.5)) }.play;

{ BufRd.ar(2, b, Line.ar(BufFrames.kr(b) - 1, 1, BufDur.kr(b) * 0.5)) }.play;

0.1

bufDur / bufFrames



/*
Open questions

How are we going to work with duration. I think it needs to be
relative which means that we need to implement all modular instruments
with relative duration.
How should we work with stereo vs mono. We might need different ways
to reduce stereo to mono and different ways to mix them together again
For instance
Take left channel in one instrumet and modify it
Take right channel in another instrument and modify it
Take the two channels and mix them together
Perhaps also mix it with the original stereo sound

This means that we can have different parts from the sounds in different
channels for a phasor effect.
*/
(
SynthDef(\playBuffer, {
	arg bufNum, rate = 1, start = 0, end = 0, out = 0;
	var totalDur, startFrame, endFrame;

	totalDur = ((end - start) / rate).abs;

	startFrame = (start / BufDur.kr(bufNum)) * BufFrames.kr(bufNum);
	endFrame = (end / BufDur.kr(bufNum)) * BufFrames.kr(bufNum);
	Line.kr(dur:totalDur, doneAction:2);

	Out.ar(out,
		BufRd.ar(2, bufNum,
			Line.ar(startFrame, endFrame, totalDur, doneAction:2)));
}).add;

SynthDef(\playLeft, {
	arg rate = 1, start = 0, end = 0, in = 0, out = 0;
	var input, dur;

	dur = ((end - start) / rate).abs;
	Line.kr(dur:dur, doneAction:2);
	input = In.ar(in, 2);

	Out.ar(out, input[0]);
}).add;

SynthDef(\playRight, {
	arg rate = 1, start = 0, end = 0, in = 0, out = 0;
	var input, dur;

	dur = ((end - start) / rate).abs;
	Line.kr(dur:dur, doneAction:2);
	input = In.ar(in, 2);

	Out.ar(out, input[1]);
}).add;

SynthDef(\mix, {
	arg rate = 1, start = 0, end = 0, in = 0, out = 0;
	var input, dur;

	dur = ((end - start) / rate).abs;
	Line.kr(dur:dur, doneAction:2);
	input = In.ar(in, 2);

	Out.ar(out, Mix.new(input));
	}).add;

SynthDef(\expand, {
	arg rate = 1, start = 0, end = 0, leftIn = 0, rightIn = 0, out = 0;
	var leftInput, rightInput, dur;

	dur = ((end - start) / rate).abs;
	Line.kr(dur:dur, doneAction:2);
	leftInput = In.ar(leftIn, 1);
	rightInput = In.ar(rightIn, 1);

	Out.ar(out, [leftInput, rightInput]);
}).add;

)


Synth(\playBuffer, ["bufNum", b, "rate", 1, "start", 0, "end", 2]);
Synth(\playBuffer, ["bufNum", b, "rate", 0.6, "start", 0.7, "end", 1.5]);
Synth(\playBuffer, ["bufNum", b, "rate", 0.6, "start", 1.5, "end", 0.7]);

Synth(\playBuffer, ["bufNum", b, "rate", 0.1, "start", 0, "end", 2]);

Synth(\playBuffer, ["bufNum", c, "rate", 0.06, "start", 0, "end", 3]);

Synth(\playBuffer, ["bufNum", d, "rate", 0.06, "start", 0, "end", 3]);

