package io.github.teamcheeze.plumjuice.common.clientity

import io.github.teamcheeze.plum.api.core.entity.Movable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*

interface Clientity: Movable<Clientity> {
    class InvalidClientityAccessException(msg: String): Exception(msg)
    var location: Location
    val players: ArrayList<Player>
    val fakeWorld: ClientityWorld
    val entityType: EntityType
    val isValid: Boolean
    fun validChecker() {
        if(!isValid) {
            throw InvalidClientityAccessException("Trying to access an invalid clientity")
        }
    }
    fun mount(passenger: Clientity): Clientity
    fun updateEquipment(map: Map<EquipmentSlot, ItemStack>): Clientity
    fun updateEquipmentMaterial(map: Map<EquipmentSlot, Material>): Clientity
}