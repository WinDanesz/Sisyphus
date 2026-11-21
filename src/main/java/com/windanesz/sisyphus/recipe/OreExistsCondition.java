package com.windanesz.sisyphus.recipe;

import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreDictionary;

import java.util.function.BooleanSupplier;

/**
 * <pre>"conditions": [{ "type": "sisyphus:ore_exists", "ore": "ingotCopper" }]</pre> Wrapped in {@code forge:not}, a recipe takes over when
 * there is no such item.
 */
@SuppressWarnings("unused")
public class OreExistsCondition implements IConditionFactory {

    public static boolean exists(String ore) {
        return OreDictionary.doesOreNameExist(ore) && !OreDictionary.getOres(ore, false).isEmpty();
    }

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        String ore = JsonUtils.getString(json, "ore");
        return () -> exists(ore);
    }
}
