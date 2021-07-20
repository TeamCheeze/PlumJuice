package io.github.teamcheeze.v1_17_R1

import com.mojang.authlib.GameProfile
import io.github.teamcheeze.plum.api.auth.GameProfileWrapper
import io.github.teamcheeze.plum.api.core.alert.BukkitAlert
import io.github.teamcheeze.plum.api.core.bukkit.GBukkit
import io.github.teamcheeze.plum.api.core.entity.EntityClass
import io.github.teamcheeze.plumjuice.common.clientity.Clientity
import io.github.teamcheeze.plumjuice.common.clientity.ClientityWorld
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.Entity as MinecraftEntity
import net.minecraft.world.entity.EntityType as MinecraftEntityType
import net.minecraft.world.entity.item.ItemEntity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_17_R1.CraftServer
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey
import org.bukkit.entity.Entity as BukkitEntity
import org.bukkit.entity.EntityType as BukkitEntityType
import org.bukkit.inventory.ItemStack

class ClientityWorldImpl(override val bukkitWorld: World): ClientityWorld {
    private fun toNMS(entityType: BukkitEntityType, world: World): MinecraftEntity {
        println("THIS IS UPDATED")
        BukkitAlert.danger("Updated!!")
        val minecraftEntityTypeIterator = Registry.ENTITY_TYPE.iterator()
        while(minecraftEntityTypeIterator.hasNext()) {
            val now = minecraftEntityTypeIterator.next()
            if(MinecraftEntityType.getKey(now) == CraftNamespacedKey.toMinecraft(entityType.key)) {
                return now.create((world as CraftWorld).handle)!!
            }
        }
        throw RuntimeException("NotFound")
    }

    override val entities = ArrayList<Clientity>()
    override fun spawnNpc(location: Location, profile: GameProfile): Clientity {
        val player = ServerPlayer(
            (Bukkit.getServer() as CraftServer).handle.server,
            (location.world!! as CraftWorld).handle,
            profile
        )
        Bukkit.getOnlinePlayers().forEach {
            (it as CraftPlayer).handle.connection.connection.send(
                ClientboundPlayerInfoPacket(
                    ClientboundPlayerInfoPacket.Action.ADD_PLAYER,
                    player
                ) as Packet<*>
            )
            val entity: net.minecraft.world.entity.Entity = player
            it.handle.connection.connection.send(entity.addEntityPacket as Packet<*>)
        }
        return ClientityImpl(
            ArrayList(Bukkit.getOnlinePlayers()),
            this,
            player.bukkitEntity,
        ).also {
            it.location = location
        }
    }

    override fun spawnEntity(location: Location, entityType: BukkitEntityType): Clientity {
        return spawn(location, entityType.entityClass!!)
    }

    override fun spawnNpc(location: Location, profile: GameProfileWrapper): Clientity {
        println("The wrapper method is called")
        return spawnNpc(location, profile.unwrap() as GameProfile)
    }

    override fun dropItem(location: Location, item: ItemStack): Clientity {
        val entity = ItemEntity(
            (this.bukkitWorld as CraftWorld).handle,
            location.x,
            location.y,
            location.z,
            CraftItemStack.asNMSCopy(item)
        )
        GBukkit.onlinePlayers.forEach {
            (it as CraftPlayer).handle.connection.connection.send(ClientboundAddEntityPacket(entity) as Packet<*>)
        }
        return ClientityImpl(
            ArrayList(GBukkit.onlinePlayers),
            this,
            entity.bukkitEntity
        ).also {
            it.location = location
        }
    }

    override fun <T : BukkitEntity> spawn(location: Location, clazz: Class<T>): Clientity {
        val entityType = EntityClass.fromClass(clazz)
        val entity = toNMS(entityType, location.world!!)
        Bukkit.getOnlinePlayers().forEach {
            (it as CraftPlayer).handle.connection.connection.send(entity.addEntityPacket as Packet<*>)
        }
        return ClientityImpl(
            ArrayList(Bukkit.getOnlinePlayers()),
            this,
            entity.bukkitEntity
        ).also {
            it.location = location
        }
    }

    override fun spawnFallingBlock(location: Location, blockData: BlockData): Clientity {
        val entity = toNMS(BukkitEntityType.FALLING_BLOCK, location.world!!)
        Bukkit.getOnlinePlayers().forEach {
            (it as CraftPlayer).handle.connection.connection.send(entity.addEntityPacket as Packet<*>)
        }
        return ClientityImpl(
            ArrayList(Bukkit.getOnlinePlayers()),
            this,
            entity.bukkitEntity
        ).also {
            it.location = location
        }
    }
}