package me.hydos.rosella.render.shader

import me.hydos.rosella.LegacyRosella

class ShaderManager(val rosella: LegacyRosella) {

    var cachedShaders = HashMap<RawShaderProgram, ShaderProgram>()

    fun getOrCreateShader(rawShader: RawShaderProgram): ShaderProgram? {
        if (!cachedShaders.containsKey(rawShader)) {
            cachedShaders[rawShader] = ShaderProgram(rawShader, rosella, rawShader.maxObjCount)
        }

        return cachedShaders[rawShader]
    }

    fun free() {
        for (program in cachedShaders.values) {
            program.free()
        }
    }
}
