package com.windanesz.sisyphus.recipe;

import com.google.gson.JsonObject;
import com.windanesz.sisyphus.Settings;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class ContentEnabledCondition implements IConditionFactory {

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        String set = JsonUtils.getString(json, "set");
        // Fail early on a typo in a recipe file instead of silently disabling the recipe
        Settings.isContentEnabled(set);
        return () -> Settings.isContentEnabled(set);
    }
}
