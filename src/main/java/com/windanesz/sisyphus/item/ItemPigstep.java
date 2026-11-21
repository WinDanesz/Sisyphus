package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemRecord;

public class ItemPigstep extends ItemRecord {

    public ItemPigstep() {
        super("pigstep", ModSounds.RECORD_PIGSTEP);
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem item) {
        NetheriteItems.keepFloating(item);
        return false;
    }
}
