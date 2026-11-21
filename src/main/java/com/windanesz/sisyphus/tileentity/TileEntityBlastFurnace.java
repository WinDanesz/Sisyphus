package com.windanesz.sisyphus.tileentity;

import com.windanesz.sisyphus.recipe.CookingRecipes;


public class TileEntityBlastFurnace extends TileEntityFurnaceVariant {

    @Override
    protected String getBaseName() {
        return "container.blast_furnace";
    }

    @Override
    protected CookingRecipes recipes() {
        return CookingRecipes.BLAST_FURNACE;
    }
}
