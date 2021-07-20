package io.github.teamcheeze.plumjuice.common.clientity

import io.github.teamcheeze.plum.api.core.events.manager.SimpleCancellable
import org.bukkit.Location
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * A list of events that clientity will call
 * @author dolphin2410
 */
class ClientityEvents {
    /**
     * Event called when the entity is spawned
     * @param clientity The entity that is spawned
     * @param location The location where the entity spawned
     */
    data class ClientitySpawnEvent(val clientity: Clientity, val location: Location): Event(), SimpleCancellable {
        companion object {
            @JvmStatic
            val HANDLERS = HandlerList()
            @JvmStatic
            fun getHandlerList(): HandlerList {
                return HANDLERS
            }
        }
        override fun getHandlers(): HandlerList {
            return HANDLERS
        }
    }

    /**
     * Event called when clientity is removed
     * @param clientity The entity that is removed. This entity has an invalid state, so you won't be able to call the methods
     */
    data class ClientityRemoveEvent(val clientity: Clientity): Event(), SimpleCancellable {
        companion object {
            @JvmStatic
            val HANDLERS = HandlerList()
            @JvmStatic
            fun getHandlerList(): HandlerList {
                return HANDLERS
            }
        }
        override fun getHandlers(): HandlerList {
            return HANDLERS
        }
    }

    /**
     * Called when clientity mounted on another clientity
     * @param whoRode The entity that rode
     * @param vehicle The entity that the entity rode
     */
    data class ClientityMountEvent(val whoRode: Clientity, val vehicle: Clientity): Event(), SimpleCancellable {
        companion object {
            @JvmStatic
            val HANDLERS = HandlerList()
            @JvmStatic
            fun getHandlerList(): HandlerList {
                return HANDLERS
            }
        }

        override fun getHandlers(): HandlerList {
            return HANDLERS
        }
    }

    /**
     * Event called when a clientity moved
     * @param clientity The entity that moved
     * @param from The from location
     * @param to The to location
     */
    data class ClientityMoveEvent(val clientity: Clientity, val from: Location, val to: Location): Event(), SimpleCancellable {
        companion object {
            @JvmStatic
            val HANDLERS = HandlerList()
            @JvmStatic
            fun getHandlerList(): HandlerList {
                return HANDLERS
            }
        }
        override fun getHandlers(): HandlerList {
            return HANDLERS
        }
    }

    /**
     * Called when a clientity teleported
     * @param clientity The clientity that teleported
     * @param from The from location
     * @param to The to location
     */
    data class ClientityTeleportEvent(val clientity: Clientity, val from: Location, val to: Location): Event(), SimpleCancellable {
        companion object {
            @JvmStatic
            val HANDLERS = HandlerList()
            @JvmStatic
            fun getHandlerList(): HandlerList {
                return HANDLERS
            }
        }
        override fun getHandlers(): HandlerList {
            return HANDLERS
        }
    }

    /**
     * Called when equipment updated
     * @param clientity The entity whose equipment got updated
     * @param slot The slot that changed
     * @param item The new item
     */
    class ClientityEquipmentUpdateEvent(val clientity: Clientity, val slot: EquipmentSlot, val item: ItemStack): Event(), SimpleCancellable {
        companion object {
            @JvmStatic
            val HANDLERS = HandlerList()
            @JvmStatic
            fun getHandlerList(): HandlerList {
                return HANDLERS
            }
        }
        override fun getHandlers(): HandlerList {
            return HANDLERS
        }
    }
}