package io.github.teamcheeze.plugin

import io.github.teamcheeze.plum.api.auth.GameProfileWrapper
import io.github.teamcheeze.plum.api.config.LibConfig
import io.github.teamcheeze.plum.api.core.command.CommandRegistry
import io.github.teamcheeze.jaw.reflection.ReflectionException
import io.github.teamcheeze.plumjuice.common.clientity.ClientityServer
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class PlumJuicePlugin : JavaPlugin() {
    override fun onEnable() {

        try {
            CommandRegistry.register("clientity") {
                try {
                    when (args[0]) {
                        "npc" -> {
                            (sender as Player).let {
                                ClientityServer.getWorld(it.world)
                                    .spawnNpc(it.location, GameProfileWrapper(UUID.randomUUID(), "MEME!!"))
                            }
                        }
                        "entity" -> {
                            (sender as Player).let {
                                ClientityServer.getWorld(it.world).spawn(it.location, ArmorStand::class.java)
                            }
                        }
                        "item" -> {
                            (sender as Player).let {
                                ClientityServer.getWorld(it.world)
                                    .spawnNpc(it.location, GameProfileWrapper(UUID.randomUUID(), "MEME!!"))
                            }
                        }
                        "fallingBlock" -> {
                            (sender as Player).let {
                                ClientityServer.getWorld(it.world)
                                    .spawnFallingBlock(it.location, Material.SPONGE.createBlockData())
                            }
                        }
                    }
                } catch (e: Exception) {
                    sender.sendMessage("ERROR")
                    sender.sendMessage(e.stackTraceToString())
                }
            }
        } catch (e: ReflectionException) {
            e.raw.printStackTrace()
        }
    }
}