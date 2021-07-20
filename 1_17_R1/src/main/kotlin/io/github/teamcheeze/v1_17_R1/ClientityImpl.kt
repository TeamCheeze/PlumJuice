package io.github.teamcheeze.v1_17_R1

import com.mojang.datafixers.util.Pair
import io.github.teamcheeze.plum.api.PluginLoader
import io.github.teamcheeze.plum.api.core.bukkit.GBukkit
import io.github.teamcheeze.jaw.reflection.FieldAccessor
import io.github.teamcheeze.plumjuice.common.clientity.Clientity
import io.github.teamcheeze.plumjuice.common.clientity.ClientityEvents
import io.github.teamcheeze.plumjuice.common.clientity.ClientityWorld
import net.minecraft.network.protocol.game.*
import org.bukkit.Bukkit
import net.minecraft.world.entity.EquipmentSlot as MinecraftEquipmentSlot
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot as BukkitEquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*

class ClientityImpl(
    override val players: ArrayList<Player>,
    override val fakeWorld: ClientityWorld,
    override val bukkitEntity: Entity
) : Clientity {
    override val id: Int = bukkitEntity.entityId
    override val uniqueId: UUID = bukkitEntity.uniqueId
    override var location: Location
        get() = bukkitEntity.location
        set(value) {
            teleport(value)
        }
    override val entityType: EntityType = bukkitEntity.type
    override val isValid: Boolean = false
    init {
        FieldAccessor(this, "isValid").set(true)
        fakeWorld.entities.add(this)
        updateMetadata()
        GBukkit.pluginManager.callEvent(ClientityEvents.ClientitySpawnEvent(this, this.location))
    }
    override fun updateMetadata() {
        validChecker()
        Bukkit.getOnlinePlayers().forEach {
            (it as CraftPlayer).handle.connection.send(ClientboundSetEntityDataPacket(id, (bukkitEntity as CraftEntity).handle.entityData, false), null)
        }
    }
    override fun remove() {
        validChecker()
        FieldAccessor(this, "isValid").set(false)
        players.forEach {
            if (entityType == EntityType.PLAYER) {
                (it as CraftPlayer).handle.connection.send(
                    ClientboundPlayerInfoPacket(
                        ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER,
                        (bukkitEntity as CraftPlayer).handle
                    )
                )
            }
            (it as CraftPlayer).handle.connection.send(ClientboundRemoveEntitiesPacket(id))
            it.sendMessage("Removed!!")
        }
        GBukkit.pluginManager.callEvent(ClientityEvents.ClientityRemoveEvent(this))
    }

    override fun mount(passenger: Clientity): Clientity {
        validChecker()
        val packet = ClientboundSetPassengersPacket((bukkitEntity as CraftPlayer).handle)

        FieldAccessor(packet, "passengers").set(packet.passengers + intArrayOf(passenger.id))
        players.forEach {
            (it as CraftPlayer).handle.connection.send(packet)
        }
        GBukkit.pluginManager.callEvent(ClientityEvents.ClientityMountEvent(passenger, this))
        return this
    }

    override fun move(delta: Vector, tick: (Location) -> Unit): Clientity {
        return moveTo(location.clone().add(delta)) { tick.invoke(it) }
    }

    override fun moveTo(target: Location, tick: (Location) -> Unit): Clientity {
        validChecker()
        val delta: Vector = target.clone().toVector().subtract(location.toVector())
        val deltaXLength = delta.x
        val deltaYLength = delta.y
        val deltaZLength = delta.z
        val runtime = 2.0
        val oneSecondMoveXDistance = deltaXLength / runtime
        val oneSecondMoveYDistance = deltaYLength / runtime
        val oneSecondMoveZDistance = deltaZLength / runtime
        var runtimeCounter = runtime * 20
        object : BukkitRunnable() {
            override fun run() {
                if (
                    runtimeCounter == 0.0
                ) {
                    this.cancel()
                    return
                }
                tick.invoke(location)
                players.forEach {
                    (it as CraftPlayer).handle.connection.send(
                        ClientboundMoveEntityPacket.PosRot(
                            id,
                            (oneSecondMoveXDistance / (20 * players.size) * 4096).toInt().toShort(),
                            (oneSecondMoveYDistance / (20 * players.size) * 4096).toInt().toShort(),
                            (oneSecondMoveZDistance / (20 * players.size) * 4096).toInt().toShort(),
                            (target.yaw * 256.0F / 360.0F).toInt().toByte(),
                            (target.pitch * 256.0F / 360.0F).toInt().toByte(),
                            false
                        )
                    )
                }
                runtimeCounter--
                GBukkit.pluginManager.callEvent(ClientityEvents.ClientityMoveEvent(this@ClientityImpl, location, location.add(oneSecondMoveXDistance / 20, oneSecondMoveYDistance / 20, oneSecondMoveZDistance / 20)))
            }
        }.runTaskTimer(PluginLoader.plugin, 1, 0)
        return this
    }

    override fun teleport(location: Location): Clientity {
        validChecker()
        val packet = ClientboundTeleportEntityPacket((bukkitEntity as CraftEntity).handle)
        FieldAccessor(packet, "x").set(location.x)
        FieldAccessor(packet, "y").set(location.y)
        FieldAccessor(packet, "z").set(location.z)
        FieldAccessor(packet, "yRot").set((location.yaw * 256.0F / 360.0F).toInt().toByte())
        FieldAccessor(packet, "xRot").set((location.pitch * 256.0F / 360.0F).toInt().toByte())
        players.forEach {
            (it as CraftPlayer).handle.connection.send(packet)
        }
        GBukkit.pluginManager.callEvent(ClientityEvents.ClientityTeleportEvent(this@ClientityImpl, this.location, location))
        this.location = location
        return this
    }

    private fun convertEquipmentSlot(equipmentSlot: BukkitEquipmentSlot): MinecraftEquipmentSlot {
        return when(equipmentSlot) {
            BukkitEquipmentSlot.HEAD-> MinecraftEquipmentSlot.HEAD
            BukkitEquipmentSlot.CHEST-> MinecraftEquipmentSlot.CHEST
            BukkitEquipmentSlot.HAND-> MinecraftEquipmentSlot.MAINHAND
            BukkitEquipmentSlot.OFF_HAND-> MinecraftEquipmentSlot.OFFHAND
            BukkitEquipmentSlot.LEGS-> MinecraftEquipmentSlot.LEGS
            BukkitEquipmentSlot.FEET-> MinecraftEquipmentSlot.FEET
        }
    }

    override fun updateEquipment(map: Map<BukkitEquipmentSlot, ItemStack>): Clientity {
        validChecker()
        map.forEach { it ->
            val packet = ClientboundSetEquipmentPacket(
                this.id,
                listOf(Pair(convertEquipmentSlot(it.key), CraftItemStack.asNMSCopy(it.value)))
            )
            players.forEach {
                (it as CraftPlayer).handle.connection.send(packet)
            }
            GBukkit.pluginManager.callEvent(ClientityEvents.ClientityEquipmentUpdateEvent(this@ClientityImpl, it.key, it.value))
        }
        return this
    }

    override fun updateEquipmentMaterial(map: Map<BukkitEquipmentSlot, Material>): Clientity {
        validChecker()
        map.forEach { it ->
            val item = ItemStack(it.value)
            val packet = ClientboundSetEquipmentPacket(
                this.id,
                listOf(Pair(convertEquipmentSlot(it.key), CraftItemStack.asNMSCopy(item)))
            )
            players.forEach {
                (it as CraftPlayer).handle.connection.send(packet)
            }
            GBukkit.pluginManager.callEvent(ClientityEvents.ClientityEquipmentUpdateEvent(this@ClientityImpl, it.key, item))
        }
        return this
    }
}