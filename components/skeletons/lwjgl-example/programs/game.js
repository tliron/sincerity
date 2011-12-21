
importClass(
	java.io.BufferedReader,
	java.io.InputStreamReader,
	java.lang.System)

// See: http://lwjgl.org/wiki/index.php?title=Downloading_and_Setting_Up_LWJGL
var examples = [
	'test.SysTest',
	'test.DisplayTest',
	
	'test.input.MouseCreationTest',
	'test.input.MouseTest',
	'test.input.HWCursorTest',
	'test.input.KeyboardTest',
	'test.input.TestControllers',
	
	'test.openal.ALCTest',
	'test.openal.OpenALCreationTest',
	'test.openal.MovingSoundTest',
	'test.openal.PlayTest',
	'test.openal.PlayTestMemory',
	'test.openal.SourceLimitTest',
	'test.openal.PositionTest',
	'test.openal.StressTest',
	'test.openal.SourceLimitTest',
	
	'test.opengl.FullScreenWindowedTest',
	'test.opengl.PbufferTest',
	'test.opengl.VBOIndexTest',
	'test.opengl.VBOTest',
	
	'test.opengl.pbuffers.PbufferTest',
	
	'test.opengl.shaders.ShadersTest',
	
	'examples.spaceinvaders.Game']

print('Choose an LWJGL example to run:\n\n')
for (var i in examples) {
	print('  ' + i + ') ' + examples[i] + '\n')
}

var reader = new BufferedReader(new InputStreamReader(System['in']))
var example = reader.readLine()
example = examples[parseInt(example)]

sincerity.run('delegate:main', ['org.lwjgl.' + example])
