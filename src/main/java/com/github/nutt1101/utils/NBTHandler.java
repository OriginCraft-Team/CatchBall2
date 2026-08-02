package com.github.nutt1101.utils;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class NBTHandler {

    /**
     * Tags that describe an entity's location/identity. They must NOT be merged
     * back when releasing a captured entity, otherwise the entity would be moved
     * to its original catch position (or get an already-removed UUID), causing
     * far-away captures to vanish on release.
     */
    private static final String[] POSITION_AND_IDENTITY_TAGS = {
            "Pos", "Motion", "Rotation", "FallDistance", "OnGround",
            "UUID", "UUIDMost", "UUIDLeast",
            "WorldUUIDMost", "WorldUUIDLeast", "Dimension",
            "Paper.Origin", "Paper.OriginWorld"
    };

    public static ItemMeta saveEntityNBT(Plugin plugin, Entity hitEntity, ItemMeta headMeta) {
        NBTEntity nbtEntity = new NBTEntity(hitEntity);
        String nbtData = nbtEntity.toString();

        headMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "entity"),
                PersistentDataType.STRING,
                nbtData
        );

        headMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "entityType"),
                PersistentDataType.STRING,
                hitEntity.getType().toString()
        );

        return headMeta;
    }

    public static void loadEntityNBT(Plugin plugin, Entity entity, PersistentDataContainer data) {
        try {
            String nbtString = data.get(new NamespacedKey(plugin, "entity"), PersistentDataType.STRING);
            if (nbtString != null) {
                NBTContainer nbtContainer = new NBTContainer(nbtString);

                // Strip position/identity tags so the restored entity stays where it is
                // placed instead of being teleported back to its original catch location
                // (which made far-away captures "disappear" on release).
                for (String key : POSITION_AND_IDENTITY_TAGS) {
                    nbtContainer.removeKey(key);
                }

                NBTEntity nbtEntity = new NBTEntity(entity);
                nbtEntity.mergeCompound(nbtContainer);

                if (entity instanceof Ageable ageableEntity) {
                    if (!nbtContainer.hasTag("IsBaby") || !nbtContainer.getBoolean("IsBaby")) {
                        ageableEntity.setAdult();
                    } else {
                        ageableEntity.setBaby();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String isCustomEntity(Entity hitEntity) {
        NBTEntity nbtEntity = new NBTEntity(hitEntity);
        return nbtEntity.getString("Paper.SpawnReason");
    }
}
