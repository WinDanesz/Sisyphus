package com.windanesz.sisyphus.tileentity;

import com.windanesz.sisyphus.recipe.CookingRecipes;


public class TileEntitySmoker extends TileEntityFurnaceVariant {

    @Override
    protected String getBaseName() {
        return "container.smoker";
    }

    @Override
    protected CookingRecipes recipes() {
        return CookingRecipes.SMOKER;
    }
}
