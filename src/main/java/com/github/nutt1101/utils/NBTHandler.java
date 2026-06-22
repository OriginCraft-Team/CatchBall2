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
     * Tags that must NOT be merged back when releasing a captured entity:
     * - UUID tags: so Moonrise's EntityLookup keeps tracking the newly-spawned
     *   entity under its own UUID (merging the old UUID causes
     *   "Failed to remove entity by uuid").
     * - Position/movement tags: so the entity stays where it is placed instead
     *   of being teleported back to its original catch location (which made
     *   far-away captures "disappear" on release).
     */
    private static final String[] POSITION_AND_IDENTITY_TAGS = {
            "UUID", "UUIDMost", "UUIDLeast",
            "Pos", "Motion", "Rotation", "FallDistance", "OnGround",
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
