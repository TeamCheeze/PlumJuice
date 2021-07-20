package io.github.teamcheeze.plumjuice.common.clientity

import io.github.teamcheeze.plum.api.config.LibConfig
import io.github.teamcheeze.plum.api.modules.core.Module
import org.bukkit.World

class ClientityServer {
    companion object {
        private val worlds = ArrayList<Pair<World, ClientityWorld>>()
        fun getWorld(bukkitWorld: World): ClientityWorld {
            LibConfig.nmsPath = "io.github.teamcheeze"
            return worlds.find { it.first == bukkitWorld }?.second ?:Module<ClientityWorld>(LibConfig.getNmsClassname() + ".ClientityWorldImpl", bukkitWorld).getNmsInstance().also {
                    worlds.add(Pair(bukkitWorld, it))
                }
        }
    }
}