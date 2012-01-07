
importClass(
	java.io.BufferedReader,
	java.io.InputStreamReader,
	java.lang.System)

// See: http://lwjgl.org/wiki/index.php?title=Downloading_and_Setting_Up_LWJGL
var EXAMPLES = [
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

println('Available LWJGL examples:')
println()
for (var i in EXAMPLES) {
	println('  ' + i + ') ' + EXAMPLES[i])
}
println()
print('Choose an LWJGL example to run: ')

var reader = new BufferedReader(new InputStreamReader(System['in']))
var example = reader.readLine()
example = EXAMPLES[parseInt(example)]

var arguments = ['org.lwjgl.' + example]
for (var i = 1, length = application.arguments.length; i < length; i++) {
	arguments.push(application.arguments[i])
}
sincerity.run('delegate:main', arguments)
